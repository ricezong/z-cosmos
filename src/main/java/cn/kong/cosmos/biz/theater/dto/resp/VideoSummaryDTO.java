package cn.kong.cosmos.biz.theater.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 视频摘要 DTO - 列表页
 */
@Data
public class VideoSummaryDTO {

    private String videoId;
    private String category;
    private String title;
    private String coverImage;
    private Integer totalEpisodes;
    private BigDecimal rating;
    private Long viewCount;
    private Integer status;
}
