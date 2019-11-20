package com.lenovo.repository.data.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lenovo.repository.data.pojo.RepositoryInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.lenovo.repository.data.enums.ErrorCodeEnum.*;

@Component
public class ProcessorService implements PageProcessor {
  private static final Logger logger = LoggerFactory.getLogger(ProcessorService.class);

  private static final String URL_LIST =
      "http://servicekb\\.lenovo\\.com\\.cn/CN/searchcenter/Pages/results\\.aspx\\?AllRange=1&start1=\\d*1";

  private static final String SERVICE_URL_PREFIX = "http://servicekb.lenovo.com.cn";

  private static final String URL_POST =
      "http://servicekb\\.lenovo\\.com\\.cn/CN/KnowledgeBase/Lists/.*\\.aspx\\?ID=\\d+";

  private static final String TOKEN_KEY = "FedAuth";

  private static final String TOKEN =
      "77u/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz48U1A+MCMuZnxmYmFtZW1iZXJzaGlwfGEwMTc0NywwIy5mfGZiYW1lbWJlcnNoaXB8YTAxNzQ3LDEzMjE4NjQwMjM2MTg5NzE4NCxUcnVlLG03c1E0d2taZ2poNGNKcjJVZ2E2M0MxdlNvb1FFdkdaZzE4NGE5V0RZT0FaTTBteW5taExSVTN3RUtpa201SmZ2U3AyTm9vM01vdjZLSkl0UlF6QTUrT3hXU0IvSVRwWE5rZFdNMWR1RWMzd1dJOWZNa1lhVmRxMlh0YW5mTFp1S0lsR3Qzd1JXdU9zRWJqR1h3a0FFQ0dubDFVbzh4R0l3eEgvYjJKWnJuOGRrWHZCNmVWTTdPR0Y4RnRsVmpZekc2OHJycXRjVUxKRjFNcWN2U1FrMXpnM0lCZUYzZDlINythN0J4VWRrR0ZQVy9kZGR4ZCtxZFhzVm1YKzRZcC9BTTRubzVqaS85RitzbmFwdzAxblpZNlJZTEdBM2Y2OWNkN21tbE9WRXNycG5ybUZzVDFOc0YvQmtVYXYyMzdaNDM1ZzB2NFJTb1ZpQjVuaFpLc2hBUT09LGh0dHA6Ly9zZXJ2aWNla2IubGVub3ZvLmNvbS5jbi9FTi9fbGF5b3V0cy9BdXRoZW50aWNhdGUuYXNweD9Tb3VyY2U9L0NOL3BhZ2VzL0hvbWUuYXNweDwvU1A+";

  private static final Map<String, String> COOKIE_MAP = new HashMap<>();

  static {
    COOKIE_MAP.put(TOKEN_KEY, TOKEN);
  }

  @Autowired private SaveDataPipelineService saveDataPipelineService;

  public void start(String startUrl) {
    Spider.create(this).addPipeline(saveDataPipelineService).addUrl(startUrl).run();
  }

  private Site site =
      Site.me()
          .setDomain("servicekb.lenovo.com.cn")
          .setSleepTime(3000)
          .addCookie(TOKEN_KEY, TOKEN)
          .setUserAgent(
              "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");

  @Override
  public void process(Page page) {
    try {
      Selectable url = page.getUrl();
      if (url.regex(URL_LIST).match()) {
        Html html = page.getHtml();
        // 添加所有详情页
        Selectable divSelectable = html.xpath("//div[@class=\"srch-results\"]");
        Selectable links = divSelectable.links();
        List<String> allOkLinks = links.regex(URL_POST).all();
        page.addTargetRequests(allOkLinks);
        // 获取下一页链接
        Selectable nextPageSelectable = html.xpath("//a[@id=\"SRP_NextImg\"]/@href");
        if (StringUtils.isNotBlank(nextPageSelectable.get())) {
          page.addTargetRequest(SERVICE_URL_PREFIX + nextPageSelectable.get());
        }
        // 设置skip之后，这个页面的结果不会被Pipeline处理
        page.setSkip(true);
      } else { // 文章页
        // 解析详情页
        parseDetailsPage(page);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public Site getSite() {
    return site;
  }

  /**
   * 解析详情页
   *
   * @param page
   */
  private void parseDetailsPage(Page page) {
    RepositoryInfo info = new RepositoryInfo();
    // 是否正常
    Boolean isError = Boolean.TRUE;
    List<Integer> errorList = new ArrayList<>();
    try {
      Html html = page.getHtml();
      Selectable idSelectable = html.xpath("//span[@class='textCNQM']");
      String itemId = getItemId(idSelectable, errorList);
      if (StringUtils.isBlank(itemId)) return;
      String webId = getWebId(idSelectable, itemId, errorList);
      if (StringUtils.isBlank(webId)) return;
      String listId = getListId(idSelectable, itemId, errorList);
      if (StringUtils.isBlank(listId)) return;
      JSONObject contentJson = getContentKeyWordFileJson(webId, listId, itemId, errorList);
      if (Objects.isNull(contentJson)) return;

      // 内容
      String content = getContent(contentJson, itemId, errorList);
      if (StringUtils.isBlank(content)) return;

      isError = Boolean.FALSE;

      info.setContent(content);
      // 附件列表
      info.setFileList(getFileList(contentJson, itemId, errorList));
      // 关键字
      info.setKeyWord(getKeyWordList(contentJson, itemId, errorList));

      Optional<Selectable> topMsgSelectable = getTopMsgSelectable(html, itemId, errorList);
      if (topMsgSelectable.isPresent()) {
        Selectable topSelectable = topMsgSelectable.get();
        // 编号
        String num = getSerialNumber(topSelectable, itemId, errorList);
        if (StringUtils.isNotBlank(num)) {
          info.setSerialNumber(num);
          // 评分
          info.setGrade(getGrade(num));
        }
        // 最后贡献人
        info.setLastContributory(getLastContributory(topSelectable, itemId, errorList));
        // 作者
        info.setAuthor(getAuthor(topSelectable, itemId, errorList));
        // 发布时间
        info.setReleaseTime(getReleaseTime(topSelectable, itemId, errorList));
      }
      // 获取标题
      info.setTitle(getTitle(html, itemId, errorList));

      Optional<Selectable> detailSelectableOptional = getDetailSelectable(html, itemId, errorList);
      if (detailSelectableOptional.isPresent()) {
        Selectable detailMsgSelectable = detailSelectableOptional.get();
        // 创建时间
        info.setCreatedTime(getCreatedTime(detailMsgSelectable, itemId, errorList));
        // 内容类型
        info.setContentType(getContentType(detailMsgSelectable, itemId, errorList));
        // 安全级别
        info.setSecurityLevel(getSecurityLevel(detailMsgSelectable, itemId, errorList));
        // 点击量
        info.setHits(getHits(detailMsgSelectable, itemId, errorList));
        // 版本
        info.setVersions(getVersions(detailMsgSelectable, itemId, errorList));
        // 来源
        info.setSource(getSource(detailMsgSelectable, itemId, errorList));
      }
      // 撰写人
      info.setCopywriter(getCopywriter(html, itemId, errorList));
      // 审核人
      info.setAuditor(getAuditor(html, itemId, errorList));
      // 发布人
      info.setPublisher(getPublisher(html, itemId, errorList));
      // 知识分类
      info.setKnowledgeType(getKnowledgeType(html, itemId, errorList));
      // 最近更新-标题
      info.setLastUpdateTitle(getLastUpdateTitle(html, itemId, errorList));
      // 最近更新-内容
      info.setLastUpdateContent(getLastUpdateContent(html, itemId, errorList));
    } catch (Exception e) {
      errorList.add(UNKNOWN_EXCEPTION.getCode());
      e.printStackTrace();
    } finally {
      page.putField("data", info);
      page.putField("isError", isError);
      page.putField("errorList", errorList);
    }
  }

  private Optional<Selectable> getTopMsgSelectable(
      Html html, String itemId, List<Integer> errorList) {
    try {
      return Optional.ofNullable(
          html.xpath(
              "//*[@id='ctl00_PlaceHolderMain_watermark']/table/tbody/tr[2]/td/table/tbody/tr/td[1]"));
    } catch (Exception e) {
      errorList.add(TOP_DETAIL_HTML.getCode());
      error("顶部HTML段", itemId, e);
    }
    return Optional.empty();
  }

  /**
   * 详情HTML段
   *
   * @param html
   * @param itemId
   * @param errorList
   * @return
   */
  private Optional<Selectable> getDetailSelectable(
      Html html, String itemId, List<Integer> errorList) {
    try {
      return Optional.ofNullable(
          html.xpath("//*[@id='ctl00_PlaceHolderMain_test']/table[1]/tbody/tr[2]/td/table"));
    } catch (Exception e) {
      errorList.add(CONTENT_DETAIL_HTML.getCode());
      error("详情HTML段", itemId, e);
    }
    return Optional.empty();
  }

  private static final String GET_GRADE_URL =
      "http://servicekb.lenovo.com.cn/CN/_layouts/AvgStar.aspx?&KCode=%s";

  private String getGrade(String num) {
    try {
      String url = String.format(GET_GRADE_URL, num);
      return get(url, COOKIE_MAP);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private String getKeyWordList(JSONObject contentJson, String itemId, List<Integer> errorList) {
    try {
      String keyWorkListHtml = contentJson.getString("keylistHtml");
      if (StringUtils.isBlank(keyWorkListHtml)) {
        return null;
      }
      keyWorkListHtml = URLDecoder2(keyWorkListHtml);
      String keyWorkListStr = "<span>" + StringEscapeUtils.unescapeXml(keyWorkListHtml) + "</span>";
      Html html = new Html(keyWorkListStr);
      return String.join(",", html.xpath("//span/a/font/text()").all());
    } catch (Exception e) {
      error("关键字", itemId, e);
    }
    return null;
  }

  private String getItemId(Selectable idSelectable, List<Integer> errorList) {
    try {
      return idSelectable.regex("var itemid = '(\\d+)';").get();
    } catch (Exception e) {
      errorList.add(ITEM_ID.getCode());
      error("itemId", "", e);
    }
    return null;
  }

  /**
   * 获取附件列表
   * @param contentJson
   * @param itemId
   * @param errorList
   * @return
   */
  private String getFileList(JSONObject contentJson, String itemId, List<Integer> errorList) {
    try {
      String fileListStr = contentJson.getString("attachmentlistHtml");
      if (StringUtils.isNotBlank(fileListStr)) {
        fileListStr = StringEscapeUtils.unescapeXml(URLDecoder2(fileListStr));
        Html html = new Html(fileListStr);
        List<String> nameList = html.xpath("//a/text()").all();
        if (!nameList.isEmpty()) {
          List<String> urlList = html.xpath("//a/@href").all();
          JSONArray jsonArray = new JSONArray();
          for (int i = 0; i < nameList.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", nameList.get(i));
            jsonObject.put("url", SERVICE_URL_PREFIX + urlList.get(i).replaceAll("&quott;",""));
            jsonArray.add(jsonObject);
          }
          return jsonArray.toJSONString();
        }
      }
    } catch (Exception e) {
      errorList.add(FILE_LIST.getCode());
      error("附件列表", itemId, e);
    }
    return null;
  }

  /**
   * 获取内容
   *
   * @param contentJson
   * @param itemId
   * @return
   */
  private String getContent(JSONObject contentJson, String itemId, List<Integer> errorList) {
    try {
      String mcHtml = contentJson.getString("mcHtml");
      return StringEscapeUtils.unescapeXml(URLDecoder2(mcHtml));
    } catch (Exception e) {
      errorList.add(CONTENT.getCode());
      error("内容", itemId, e);
    }
    return null;
  }

  private String getListId(Selectable idSelectable, String itemId, List<Integer> errorList) {
    try {
      return idSelectable.regex("var listid = '(\\S+)';").get();
    } catch (Exception e) {
      errorList.add(LIST_ID.getCode());
      error("listId", itemId, e);
    }
    return null;
  }

  private String getWebId(Selectable idSelectable, String itemId, List<Integer> errorList) {
    try {
      return idSelectable.regex("var webid = '(\\S+)';").get();
    } catch (Exception e) {
      errorList.add(WEB_ID.getCode());
      error("webId", itemId, e);
    }
    return null;
  }

  private static final String CONTENT_URL =
      "http://servicekb.lenovo.com.cn/CN/_Layouts/KnowledgeDispFormNew.ashx?webid=%s&list=%s&itemid=%s";

  private JSONObject getContentKeyWordFileJson(
      String webId, String list, String itemId, List<Integer> errorList) {
    try {
      String url = String.format(CONTENT_URL, webId, list, itemId);
      String result = get(url, COOKIE_MAP);
      if (StringUtils.isNotBlank(result)) {
        return JSON.parseObject(result);
      }
    } catch (Exception e) {
      errorList.add(CONTENT_DETAIL_INTERFACE.getCode());
      error("调用接口获取内容/关键字/附件列表", itemId, e);
    }
    return null;
  }

  /**
   * 获取最近更新-内容
   *
   * @param html
   */
  private String getLastUpdateContent(Html html, String itemId, List<Integer> errorList) {
    try {
      return html.xpath(
              "//*[@id=\"ctl00_PlaceHolderMain_watermark\"]/table/tbody/tr[4]/td/div/text()")
          .get();
    } catch (Exception e) {
      errorList.add(LAST_UPDATE_CONTENT.getCode());
      error("最近更新-内容", itemId, e);
    }
    return null;
  }

  /**
   * 获取最近更新-标题
   *
   * @param html
   */
  private String getLastUpdateTitle(Html html, String itemId, List<Integer> errorList) {
    try {
      return html.xpath("//*[@id='ctl00_PlaceHolderMain_labChangeReason']/text()").get();
    } catch (Exception e) {
      errorList.add(LAST_UPDATE_TITLE.getCode());
      error("最近更新-标题", itemId, e);
    }
    return null;
  }

  /**
   * 获取来源
   *
   * @param detailMsgSelectable
   */
  private String getSource(Selectable detailMsgSelectable, String itemId, List<Integer> errorList) {
    try {
      return detailMsgSelectable.regex("来源：</font>\\s*</th>\\s*<td>\\s*(\\S+)\\s*</td>").get();
    } catch (Exception e) {
      errorList.add(SOURCE.getCode());
      error("来源", itemId, e);
    }
    return null;
  }

  /**
   * 获取版本
   *
   * @param detailMsgSelectable
   */
  private String getVersions(
      Selectable detailMsgSelectable, String itemId, List<Integer> errorList) {
    try {
      return detailMsgSelectable
          .regex("版本：</font>\\s*</th>\\s*<td>\\s*(\\d+\\.*\\d*)\\s*</td>")
          .get();
    } catch (Exception e) {
      errorList.add(VERSIONS.getCode());
      error("版本", itemId, e);
    }
    return null;
  }

  /**
   * 点击量
   *
   * @param detailMsgSelectable
   */
  private Integer getHits(Selectable detailMsgSelectable, String itemId, List<Integer> errorList) {
    try {
      String hitsStr =
          detailMsgSelectable.regex("点击量：</font>\\s*</th>\\s*<td>\\s*(\\d+)\\s*</td>").get();
      if (StringUtils.isBlank(hitsStr)) {
        return Integer.valueOf(hitsStr);
      }
    } catch (Exception e) {
      errorList.add(HITS.getCode());
      error("点击量", itemId, e);
    }
    return null;
  }

  /**
   * 获取安全级别
   *
   * @param detailMsgSelectable
   */
  private String getSecurityLevel(
      Selectable detailMsgSelectable, String itemId, List<Integer> errorList) {
    try {
      return detailMsgSelectable.regex("安全级别：</font>\\s*</th>\\s*<td>\\s*(\\S+)\\s*</td>").get();
    } catch (Exception e) {
      errorList.add(SECURITY_LEVEL.getCode());
      error("安全级别", itemId, e);
    }
    return null;
  }

  /**
   * 获取知识分类
   *
   * @param html
   */
  private String getKnowledgeType(Html html, String itemId, List<Integer> errorList) {
    try {
      return html.xpath("//div[@class='aboutktype']/text()").get();
    } catch (Exception e) {
      errorList.add(KNOWLEDGE_TYPE.getCode());
      error("知识分类", itemId, e);
    }
    return null;
  }

  /**
   * 内容类型
   *
   * @param detailMsgSelectable
   */
  private String getContentType(
      Selectable detailMsgSelectable, String itemId, List<Integer> errorList) {
    try {
      return detailMsgSelectable.regex("内容类型：</font>\\s*</th>\\s*<td>\\s*-(\\S+)\\s*</td>").get();
    } catch (Exception e) {
      errorList.add(CONTENT_TYPE.getCode());
      error("内容类型", itemId, e);
    }
    return null;
  }

  /**
   * 获取发布人
   *
   * @param html
   */
  private String getPublisher(Html html, String itemId, List<Integer> errorList) {
    try {
      return html.xpath("//*[@id='ctl00_PlaceHolderMain_KnowledgeProperty1_Label3']/text()").get();
    } catch (Exception e) {
      errorList.add(PUBLISHER.getCode());
      error("发布人", itemId, e);
    }
    return null;
  }

  /**
   * 获取审核人
   *
   * @param html
   */
  private String getAuditor(Html html, String itemId, List<Integer> errorList) {
    try {
      return html.xpath("//*[@id='ctl00_PlaceHolderMain_KnowledgeProperty1_Label2']/text()").get();
    } catch (Exception e) {
      errorList.add(AUDITOR.getCode());
      error("审核人", itemId, e);
    }
    return null;
  }

  /**
   * 获取创建时间
   *
   * @param detailMsgSelectable
   */
  private Date getCreatedTime(
      Selectable detailMsgSelectable, String itemId, List<Integer> errorList) {
    try {
      String dateStr = detailMsgSelectable.regex("创建时间：[\\s\\S]+(\\d{4}/\\d{1,2}/\\d{1,2})").get();
      return DateUtils.parseDate(dateStr, "yyyy/MM/dd");
    } catch (Exception e) {
      errorList.add(CREATED_TIME.getCode());
      error("创建时间", itemId, e);
    }
    return null;
  }

  /**
   * 获取撰写人
   *
   * @param html
   */
  private String getCopywriter(Html html, String itemId, List<Integer> errorList) {
    try {
      return html.xpath("//*[@id='ctl00_PlaceHolderMain_KnowledgeProperty1_labCreator']/text()")
          .get();
    } catch (Exception e) {
      errorList.add(COPYWRITER.getCode());
      error("撰写人", itemId, e);
    }
    return null;
  }

  /**
   * 获取标题
   *
   * @param html
   */
  private String getTitle(Html html, String itemId, List<Integer> errorList) {
    try {
      String title =
          html.xpath(
                  "//*[@id=\"ctl00_PlaceHolderMain_watermark\"]/table/tbody/tr[1]/td/table/tbody/tr/td[1]/text()")
              .get();
      if (StringUtils.isNotBlank(title)) {
        title.replaceAll("  ", "");
      }
      return title;
    } catch (Exception e) {
      errorList.add(TITLE.getCode());
      error("标题", itemId, e);
    }
    return null;
  }

  /**
   * 获取发布时间
   *
   * @param topSelectable
   */
  private Date getReleaseTime(Selectable topSelectable, String itemId, List<Integer> errorList) {
    try {
      String dateStr =
          topSelectable.regex("发布时间：.*(\\d{4}/\\d{1,2}/\\d{1,2} \\d{1,2}:\\d{1,2})").get();
      if (StringUtils.isNotBlank(dateStr)) {
        return DateUtils.parseDate(dateStr, "yyyy/MM/dd HH:mm");
      }
    } catch (Exception e) {
      errorList.add(RELEASE_TIME.getCode());
      error("发布时间", itemId, e);
    }
    return null;
  }

  /**
   * 获取作者
   *
   * @param topSelectable
   */
  private String getAuthor(Selectable topSelectable, String itemId, List<Integer> errorList) {
    try {
      return new Html("<span>" + topSelectable.regex("作者：([\\s\\S]+)发布时间").get() + "</span>")
          .xpath("//span/text()").all().stream()
              .filter(s -> StringUtils.isNotBlank(s.trim()))
              .map(s -> s.trim().replaceAll("/", "").replaceAll("  ", ""))
              .collect(Collectors.joining(","));
    } catch (Exception e) {
      errorList.add(AUTHOR.getCode());
      error("作者", itemId, e);
    }
    return null;
  }

  /**
   * 编号
   *
   * @param topSelectable
   */
  private String getSerialNumber(Selectable topSelectable, String itemId, List<Integer> errorList) {
    try {
      return topSelectable.regex("编号：(\\d+)").get();
    } catch (Exception e) {
      errorList.add(SERIAL_NUMBER.getCode());
      error("编号", itemId, e);
    }
    return null;
  }

  /**
   * 最后贡献人
   *
   * @param topSelectable
   */
  private String getLastContributory(
      Selectable topSelectable, String itemId, List<Integer> errorList) {
    try {
      return new Html(topSelectable.regex("最后贡献人：.+(<b>.+</b>)</span>").get())
          .xpath("//span/text() | //b/text()").all().stream()
              .filter(s -> StringUtils.isNotBlank(s.trim()))
              .map(s -> s.trim().replaceAll("/", "").replaceAll("  ", ""))
              .collect(Collectors.joining(","));
    } catch (Exception e) {
      errorList.add(LAST_CONTRIBUTORY.getCode());
      error("最后贡献人", itemId, e);
    }
    return null;
  }

  private static String URLDecoder2(String str) throws UnsupportedEncodingException {
    String str1 = URLDecoder(str);
    if (StringUtils.isNotBlank(str1)) {
      str1 = str1.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
      str1 = str1.replaceAll("\\+", "%2B");
      return URLDecoder(str1);
    }
    return str1;
  }

  private static String URLDecoder(String str) throws UnsupportedEncodingException {
    String result = null;
    if (StringUtils.isNotBlank(str)) {
      result = URLDecoder.decode(str, "UTF-8");
    }
    return result;
  }

  private static String get(String url, Map<String, String> cookies) {
    try {
      // 创建get访问对象
      HttpGet get = new HttpGet(url);
      // 创建CookieStore对象用来管理cookie
      HttpClientBuilder httpClientBuilder = HttpClients.custom();
      if (Objects.nonNull(cookies)) {
        CookieStore cookieStore = new BasicCookieStore();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
          // new BasicClientCookie对象 用来注入cookie
          BasicClientCookie cookie = new BasicClientCookie(entry.getKey(), entry.getValue());
          cookie.setDomain(getParentUrl(url)); // 设置cookie的作用域
          cookieStore.addCookie(cookie); // 将cookie添加到cookieStore中
        }
        httpClientBuilder.setDefaultCookieStore(cookieStore);
      }
      HttpClient httpClient = httpClientBuilder.build();
      HttpResponse response = httpClient.execute(get);
      // 将response对象转换成String类型
      return EntityUtils.toString(response.getEntity(), "utf-8");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static String getParentUrl(String url) {
    Pattern pattern = Pattern.compile("://([a-z|.]+)/");
    Matcher matcher = pattern.matcher(url);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return "/";
  }

  private static void error(String type, String itemId, Exception e) {
    logger.error("获取知识库的【{}】时出错,itemId:[{}]-{}", type, itemId, e.getMessage());
    e.printStackTrace();
  }
}
