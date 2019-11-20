package com.lenovo.repository.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCodeEnum {
  ITEM_ID(1),
  WEB_ID(2),
  LIST_ID(3),
  CONTENT_DETAIL_INTERFACE(4),
  CONTENT_DETAIL_HTML(5),
  TOP_DETAIL_HTML(6),
  CONTENT(7),
  FILE_LIST(8),
  KEY_WORD(9),
  SERIAL_NUMBER(10),
  LAST_CONTRIBUTORY(11),
  AUTHOR(12),
  RELEASE_TIME(13),
  TITLE(14),
  COPYWRITER(15),
  CREATED_TIME(16),
  AUDITOR(17),
  PUBLISHER(18),
  CONTENT_TYPE(19),
  KNOWLEDGE_TYPE(20),
  SECURITY_LEVEL(21),
  HITS(22),
  VERSIONS(23),
  SOURCE(24),
  LAST_UPDATE_TITLE(25),
  LAST_UPDATE_CONTENT(26),
  UNKNOWN_EXCEPTION(27),
  INSERT_ERROR(28);


  private int code;
}
