package cn.kong.cosmos.biz.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类实体 - 对应 z_categories 表
 */
@Data
@TableName("z_categories")
public class Category {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类代码（唯一） */
    private String categoryCode;

    private String name;

    private String description;

    /** RemixIcon 类名，如 ri-code-line */
    private String icon;

    /** 主色 HEX，如 #7890b5 */
    private String color;

    private Integer sort;

    private Long postCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
