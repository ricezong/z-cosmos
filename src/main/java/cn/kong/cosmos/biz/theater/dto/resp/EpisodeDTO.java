package cn.kong.cosmos.biz.theater.dto.resp;

import lombok.Data;

/**
 * 剧集 DTO
 */
@Data
public class EpisodeDTO {

    private Integer episodeNumber;
    private String title;
    private Integer duration;
    private String playUrl;
    private String sourceType;
    private String coverImage;
}
