package com.lenovo.repository.data.pojo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "repository_info")
public class RepositoryInfo {

  @Id
  @Column(length = 32)
  @GenericGenerator(name = "uuid", strategy = "uuid")
  @GeneratedValue(generator = "uuid")
  private String id;
  // 内容
  @Column(columnDefinition = "TEXT")
  private String content;
  //文件列表
  @Column(columnDefinition = "TEXT",name = "file_list")
  private String fileList;
  // 关键字
  @Column(length = 500,name = "key_word")
  private String keyWord;
  // 序号
  @Column(length = 16,name = "serial_number")
  private String serialNumber;
  // 最后贡献人
  @Column(length = 200,name = "last_contributory")
  private String lastContributory;
  // 作者
  @Column(length = 200)
  private String author;
  // 发布时间
  @Column(name = "release_time")
  private Date releaseTime;
  //标题
  @Column(length = 500)
  private String title;
  //撰写人
  @Column(length = 200)
  private String copywriter;
  // 创建时间
  @Column(name = "created_time")
  private Date createdTime;
  // 审核人
  @Column(length = 200)
  private String auditor;
  // 发布人
  @Column(length = 200)
  private String publisher;
  // 内容类型
  @Column(length = 128,name = "content_type")
  private String contentType;
  //知识分类
  @Column(length = 1000,name = "knowledge_type")
  private String knowledgeType;
  // 评分
  @Column(length = 32)
  private String grade;
  //安全级别
  @Column(length = 128,name = "security_level")
  private String securityLevel;
  //点击量
  @Column(length = 16)
  private Integer hits;
  //版本
  @Column(length = 32)
  private String versions;
  //来源
  @Column(length = 128)
  private String source;
  //最后更新标题
  @Column(length = 500,name = "last_update_title")
  private String lastUpdateTitle;

  //最后更新内容
  @Column(columnDefinition = "TEXT",name = "last_update_content")
  private String lastUpdateContent;

  @Column(length = 500)
  private String url;

  @Column(length = 6,name = "is_error")
  private Boolean isError;

  @OneToMany(targetEntity= ErrorList.class,cascade= CascadeType.ALL,mappedBy = "repositoryInfo",fetch = FetchType.LAZY)
  List<ErrorList> errorLists;

}
