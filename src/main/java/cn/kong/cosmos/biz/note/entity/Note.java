package cn.kong.cosmos.biz.note.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 技术笔记元数据实体 - 对应 z_notes 表
 */
@Data
@TableName("z_notes")
public class Note {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 笔记业务 ID（雪花算法） */
    private String noteId;

    /** 笔记标题 */
    private String title;

    /** 分类编码 */
    private String categoryCode;

    /** 标签数组（JSON） */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> tags;

    /** 内容类型：0-原创 1-转载 */
    private Integer contentType;

    /** 转载来源链接 */
    private String sourceUrl;

    /** 累计阅读数 */
    private Long viewCount;

    /** 预估阅读耗时 (分钟) */
    private Integer readMinutes;

    /** 访问控制：0-公开 1-需解锁 */
    private Integer isLocked;

    /** SEO 摘要/列表副标题 */
    private String shortSummary;

    /** 预览策略：1-按字数 2-按分隔符 */
    private Integer previewType;

    /** 预览截取字数阈值 */
    private Integer previewLimit;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
