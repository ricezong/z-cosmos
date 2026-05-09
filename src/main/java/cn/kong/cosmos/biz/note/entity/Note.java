package cn.kong.cosmos.biz.note.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 技术笔记实体 - 对应 z_notes 表
 */
@Data
@TableName(value = "z_notes", autoResultMap = true)
public class Note {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 笔记业务ID（雪花算法） */
    private String noteId;

    /** 笔记标题 */
    private String title;

    /** 笔记完整内容（HTML格式） */
    private String content;

    /** 预览比例（0.30表示前30%） */
    private BigDecimal previewRatio;

    /** 简短摘要（SEO用，100字以内） */
    private String shortSummary;

    /** 封面图URL */
    private String coverImage;

    /** 分类标签 */
    private String category;

    /** 标签数组（JSON） */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> tags;

    /** 已读数 */
    private Long viewCount;

    /** 是否锁定：0-公开 1-需解锁 */
    private Integer isLocked;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
