package cn.kong.cosmos.biz.search.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜索索引实体 - 对应 z_search_index 表
 */
@Data
@TableName(value = "z_search_index", autoResultMap = true)
public class SearchIndex {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 内容类型（NOTE/HOT） */
    private String contentType;

    /** 内容业务ID */
    private String contentId;

    /** 标题 */
    private String title;

    /** 关键词数组（JSON） */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> keywords;

    /** 标签数组（JSON） */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> tags;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
