package cn.kong.cosmos.biz.hot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 热点话题实体 - 对应 z_hot_topics 表
 */
@Data
@TableName(value = "z_hot_topics", autoResultMap = true)
public class HotTopic {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 热点业务ID */
    private String topicId;

    /** AI提炼标题 */
    private String title;

    /** AI完整总结 */
    private String summary;

    /** 原始链接 */
    private String sourceUrl;

    /** 来源平台名称 */
    private String sourceName;

    /** 原始发布时间 */
    private LocalDateTime publishTime;

    /** 分类标签 */
    private String category;

    /** 是否有效：0-下架 1-展示 */
    private Integer isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
