package cn.kong.cosmos.biz.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子实体 - 对应 z_posts 表
 */
@Data
@TableName(value = "z_posts", autoResultMap = true)
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 帖子业务 ID（雪花） */
    private String postId;

    /** 作者业务 userId */
    private String userId;

    /** 分类代码 */
    private String categoryCode;

    private String title;

    /** HTML 富文本（已 Jsoup 清洗） */
    private String content;

    /** 摘要（前端展示用） */
    private String summary;

    /** 封面图 URL */
    private String coverImage;

    /** 图片数组（JSON） */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> images;

    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Long collectCount;

    /** 0-否 1-置顶 */
    private Integer isTop;

    /** 0-否 1-精华 */
    private Integer isEssence;

    /** 0-草稿 1-已发布 2-审核中 3-已隐藏 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
