package cn.kong.cosmos.biz.note.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记分类实体 - 对应 z_note_categories 表
 */
@Data
@TableName("z_note_categories")
public class NoteCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类编码 (唯一) */
    private String categoryCode;

    /** 分类名称 */
    private String categoryName;

    /** 分类描述 */
    private String description;

    /** 分类图标 URL */
    private String iconUrl;

    /** 排序权重 */
    private Integer sortOrder;

    /** 启用状态：0-禁用 1-启用 */
    private Integer isEnabled;

    /** 关联笔记总数 (冗余) */
    private Long noteCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
