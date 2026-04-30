package cn.kong.cosmos.biz.theater.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 剧集实体 - 对应 z_video_episodes 表
 */
@Data
@TableName("z_video_episodes")
public class VideoEpisode {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属视频业务 ID */
    private String videoId;

    /** 集数序号（1,2,3...） */
    private Integer episodeNumber;

    /** 本集标题（可选） */
    private String title;

    /** 时长（秒） */
    private Integer duration;

    /** 播放地址（第三方外链） */
    private String playUrl;

    /** MP4 / YOUTUBE / BILIBILI / HLS */
    private String sourceType;

    private String coverImage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
