package cn.kong.cosmos.biz.community.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论/回复请求
 */
@Data
public class CreateCommentReq {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论最多 500 字")
    private String content;

    /** 顶层评论为 null；楼中楼回复指向顶层 commentId */
    private String parentId;

    /** 被回复者 userId（楼中楼才填） */
    private String replyToUserId;
}
