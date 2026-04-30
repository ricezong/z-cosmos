package cn.kong.cosmos.biz.hot.dto.resp;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 新闻详情 DTO - 含完整正文
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NewsDetailDTO extends NewsSummaryDTO {

    /** HTML 正文 */
    private String content;
}
