package cn.kong.cosmos.biz.hot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新闻实体 - 对应 z_news 表
 */
@Data
@TableName("z_news")
public class News {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务 ID（雪花） */
    private String newsId;

    /** 分类：general / tech / finance / sports / entertainment ... */
    private String category;

    private String title;
    private String summary;

    /** 新闻正文 HTML（可为 null，代表仅有外链） */
    private String content;

    private String coverImage;

    /** 来源名称，如 "人民网" / "新华社" */
    private String source;

    /** 原文链接 */
    private String sourceUrl;

    private Long viewCount;

    /** 热度分：结合阅读、时效、权重算出 */
    private Double hotScore;

    /** 发布时间（来源侧时间） */
    private LocalDateTime publishedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
