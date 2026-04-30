package cn.kong.cosmos.biz.theater.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 视频实体 - 对应 z_videos 表
 */
@Data
@TableName("z_videos")
public class Video {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务 ID（雪花） */
    private String videoId;

    /** movie / drama / anime / variety */
    private String category;

    private String title;
    private String description;
    private String coverImage;

    private Integer totalEpisodes;

    /** 评分 0.0 - 10.0 */
    private BigDecimal rating;

    private String director;

    /** 主演，逗号分隔 */
    private String actors;

    private Long viewCount;

    /** 0-下架 1-正常 2-即将上线 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
