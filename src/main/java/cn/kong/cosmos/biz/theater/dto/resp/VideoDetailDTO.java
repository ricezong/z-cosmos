package cn.kong.cosmos.biz.theater.dto.resp;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 视频详情 DTO - 含完整元信息 + 剧集列表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VideoDetailDTO extends VideoSummaryDTO {

    private String description;
    private String director;

    /** 主演列表（从 actors 字符串拆分） */
    private List<String> actors;

    /** 剧集列表 */
    private List<EpisodeDTO> episodes;
}
