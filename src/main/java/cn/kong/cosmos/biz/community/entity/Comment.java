package cn.kong.cosmos.biz.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论实体 - 对应 z_comments 表（支持楼中楼）
 */
@Data
@TableName("z_comments")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 评论业务 ID（雪花） */
    private String commentId;

    /** 所属帖子业务 ID */
    private String postId;

    /** 作者业务 userId */
    private String userId;

    /** 顶层评论为 null；楼中楼指向顶层 commentId */
    private String parentId;

    /** 被回复者 userId */
    private String replyToUserId;

    /** 被回复者昵称（冗余展示用） */
    private String replyToNickname;

    private String content;

    private Long likeCount;

    /** 0-删除 1-正常 2-审核中 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
