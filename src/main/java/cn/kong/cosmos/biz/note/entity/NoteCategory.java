package cn.kong.cosmos.biz.note.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记类别实体类
 */
@Data
@TableName(value = "z_note_categories", autoResultMap = true)
public class NoteCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 类别编码（唯一标识） */
    private String categoryCode;

    /** 类别名称 */
    private String categoryName;

    /** 类别描述 */
    private String description;

    /** 图标URL */
    private String iconUrl;

    /** 排序权重 */
    private Integer sortOrder;

    /** 是否启用：0-禁用 1-启用 */
    private Integer isEnabled;

    /** 笔记数量（冗余字段，便于统计） */
    private Long noteCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}