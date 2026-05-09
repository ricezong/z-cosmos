package cn.kong.cosmos.biz.hot.dto.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 热点话题DTO
 */
@Data
public class HotTopicDTO {
    private String topicId;
    private String title;
    private String summary;
    private String sourceUrl;
    private String sourceName;
    private LocalDateTime publishTime;
    private String category;
    private LocalDateTime createdAt;
}
