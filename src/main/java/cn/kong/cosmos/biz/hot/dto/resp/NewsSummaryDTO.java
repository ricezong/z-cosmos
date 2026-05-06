package cn.kong.cosmos.biz.hot.dto.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新闻摘要 DTO - 列表页/热门榜单
 */
@Data
public class NewsSummaryDTO {

    private String newsId;
    private String category;
    private String title;
    private String summary;
    private String coverImage;
    private String source;
    private String sourceUrl;
    private Long viewCount;
    private Double hotScore;
    private LocalDateTime publishedAt;
}
