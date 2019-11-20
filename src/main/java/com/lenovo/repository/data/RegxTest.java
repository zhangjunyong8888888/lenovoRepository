package com.lenovo.repository.data;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.text.ParseException;

public class RegxTest {

  private static final String reg =
      "http://servicekb\\.lenovo\\.com\\.cn/CN/searchcenter/Pages/results.aspx\\?AllRange=1&start1=\\d*1";

  public static void main(String[] args) {
    /*      String str = "http://servicekb.lenovo.com.cn/CN/searchcenter/Pages/results.aspx?AllRange=1&start1=1";
    System.out.println(
    Pattern.matches(reg, str));*/

      /*Processor processor
              = new Processor();
      Map<String,String> result = new HashMap<>();
      result.put("FedAuth","77u/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz48U1A+MCMuZnxmYmFtZW1iZXJzaGlwfGEwMTc0NywwIy5mfGZiYW1lbWJlcnNoaXB8YTAxNzQ3LDEzMjE4Mjk4MjgzMDY0Nzc1OCxUcnVlLG96bjlwR3o3aWFMemo5SVFLMFBzVHBpdnpWRHFDdkZnTmdQMEQvTlJrV1BOU3lQak1vZTdHTVJrTU4xb25aNUxzOHFybjRrbzg5QTd5U1hrako3cjhyNFlvdGNRRU45dU5BL2N5bGcvRkg5VFdtRUFIMzBVaXRoVXlobmR0WFFhUVR0dmFtM2lQWVFaS3Y2YzFHUWhpaDgxS2V3b2d4MnlYdG9pN0wrU284VklUcDU3aHVDV2ZiYmpoY0ttTWtUdHBuSUwrQWEyMVM4MlJSd1Zza25rMWZnMHZIcmtkNTJ5Um5qV0VCM1lmV3d5RGtCL2U4QWlWVStZdG9jN1hTSTN4a0VuOGY5NUlUNURVYjFRbElTYWdKUk5zWGNMVGszT1pKcVNRYlcyM3hSbXI4bmR3L2grRUZKV2ZvNHlvalhkL25OcnBYWDN6alg3Y215ODVpck9hdz09LGh0dHA6Ly9zZXJ2aWNla2IubGVub3ZvLmNvbS5jbi9FTi9fbGF5b3V0cy9BdXRoZW50aWNhdGUuYXNweD9Tb3VyY2U9L0NOL3BhZ2VzL0hvbWUuYXNweDwvU1A+");
      String res = processor.get("http://servicekb.lenovo.com.cn/CN/_Layouts/KnowledgeDispFormNew.ashx?webid=c14f6af1-09ae-4878-93a7-4868cccf17ff&list=30b15849-ce45-4b81-86bc-c4626fa67aa0&itemid=31184",result);
    System.out.println(res);*/

      String str = "%26lt%3ba%2520href%3d%26quott%3b%2fCN%2fsearchcenter%2fPages%2fresults.aspx%3fk%3dWin8%26send%3dWin8%26quott%3b%2520target%3d%26quott%3b_blank%26quott%3b%26gt%3b%26lt%3bFONT%2520style%3d%26quot%3bCOLOR%3a%231196ee%26quot%3b%26gt%3bWin8%26lt%3b%2ffont%26gt%3b%26lt%3b%2fa%26gt%3b%2520%26nbsp%3b%26nbsp%3b%26lt%3ba%2520href%3d%26quott%3b%2fCN%2fsearchcenter%2fPages%2fresults.aspx%3fk%3dWin8%25e4%25b8%2580%25e9%2594%25ae%25e6%2581%25a2%25e5%25a4%258d%26send%3dWin8%25e4%25b8%2580%25e9%2594%25ae%25e6%2581%25a2%25e5%25a4%258d%26quott%3b%2520target%3d%26quott%3b_blank%26quott%3b%26gt%3b%26lt%3bFONT%2520style%3d%26quot%3bCOLOR%3a%231196ee%26quot%3b%26gt%3bWin8%e4%b8%80%e9%94%ae%e6%81%a2%e5%a4%8d%26lt%3b%2ffont%26gt%3b%26lt%3b%2fa%26gt%3b%2520%26nbsp%3b%26nbsp%3b%26lt%3ba%2520href%3d%26quott%3b%2fCN%2fsearchcenter%2fPages%2fresults.aspx%3fk%3d%25e4%25b8%2580%25e9%2594%25ae%25e6%2581%25a2%25e5%25a4%258d%26send%3d%25e4%25b8%2580%25e9%2594%25ae%25e6%2581%25a2%25e5%25a4%258d%26quott%3b%2520target%3d%26quott%3b_blank%26quott%3b%26gt%3b%26lt%3bFONT%2520style%3d%26quot%3bCOLOR%3a%231196ee%26quot%3b%26gt%3b%e4%b8%80%e9%94%ae%e6%81%a2%e5%a4%8d%26lt%3b%2ffont%26gt%3b%26lt%3b%2fa%26gt%3b%2520%26nbsp%3b%26nbsp%3b%26lt%3ba%2520href%3d%26quott%3b%2fCN%2fsearchcenter%2fPages%2fresults.aspx%3fk%3d%25e6%25ad%25a5%25e9%25aa%25a4%26send%3d%25e6%25ad%25a5%25e9%25aa%25a4%26quott%3b%2520target%3d%26quott%3b_blank%26quott%3b%26gt%3b%26lt%3bFONT%2520style%3d%26quot%3bCOLOR%3a%231196ee%26quot%3b%26gt%3b%e6%ad%a5%e9%aa%a4%26lt%3b%2ffont%26gt%3b%26lt%3b%2fa%26gt%3b%2520%26nbsp%3b%26nbsp%3b%26lt%3ba%2520href%3d%26quott%3b%2fCN%2fsearchcenter%2fPages%2fresults.aspx%3fk%3d%25e8%25ae%25be%25e7%25bd%25ae%26send%3d%25e8%25ae%25be%25e7%25bd%25ae%26quott%3b%2520target%3d%26quott%3b_blank%26quott%3b%26gt%3b%26lt%3bFONT%2520style%3d%26quot%3bCOLOR%3a%231196ee%26quot%3b%26gt%3b%e8%ae%be%e7%bd%ae%26lt%3b%2ffont%26gt%3b%26lt%3b%2fa%26gt%3b%2520%26nbsp%3b%26nbsp%3b%26lt%3ba%2520href%3d%26quott%3b%2fCN%2fsearchcenter%2fPages%2fresults.aspx%3fk%3d%25e7%2595%258c%25e9%259d%25a2%26send%3d%25e7%2595%258c%25e9%259d%25a2%26quott%3b%2520target%3d%26quott%3b_blank%26quott%3b%26gt%3b%26lt%3bFONT%2520style%3d%26quot%3bCOLOR%3a%231196ee%26quot%3b%26gt%3b%e7%95%8c%e9%9d%a2%26lt%3b%2ffont%26gt%3b%26lt%3b%2fa%26gt%3b%2520%26nbsp%3b%26nbsp%3b%26lt%3ba%2520href%3d%26quott%3b%2fCN%2fsearchcenter%2fPages%2fresults.aspx%3fk%3d%25e6%2581%25a2%25e5%25a4%258d%25e5%2590%258e%26send%3d%25e6%2581%25a2%25e5%25a4%258d%25e5%2590%258e%26quott%3b%2520target%3d%26quott%3b_blank%26quott%3b%26gt%3b%26lt%3bFONT%2520style%3d%26quot%3bCOLOR%3a%231196ee%26quot%3b%26gt%3b%e6%81%a2%e5%a4%8d%e5%90%8e%26lt%3b%2ffont%26gt%3b%26lt%3b%2fa%26gt%3b%2520%26nbsp%3b%26nbsp%3b";
    System.out.println( StringEscapeUtils.unescapeXml(str));
      try {
          System.out.println(DateUtils.parseDate("2019/10/19","yyyy/MM/dd"));
      } catch (ParseException e) {
          e.printStackTrace();
      }

    /*//列表页
    Selectable url = page.getUrl();
    if (url.regex(URL_LIST).match()) {
        Html html = page.getHtml();
        // 添加所有详情页
        page.addTargetRequests(html.xpath("//div[@class=\"srch-results\"]").links().regex(URL_POST).all());
        // 获取下一页链接
        Selectable nextPageSelectable = html.xpath("//a[@id=\"SRP_NextImg\"]/@href");
        if(StringUtils.isNotBlank(nextPageSelectable.get())){
            page.addTargetRequest(URL_LIST_PREFIX + nextPageSelectable.get());
        }
    } else {//文章页
        Html html = page.getHtml();
        page.putField("title", html.xpath("//div[@class='articalTitle']/h2"));
        page.putField("content", html.xpath("//div[@id='articlebody']//div[@class='articalContent']"));
        page.putField("date", html.xpath("//div[@id='articlebody']//span[@class='time SG_txtc']").regex("\\((.*)\\)"));
    }*/

  }
}
