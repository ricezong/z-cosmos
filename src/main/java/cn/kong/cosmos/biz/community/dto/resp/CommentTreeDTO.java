package cn.kong.cosmos.biz.community.dto.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 楼中楼评论树 DTO
 */
@Data
public class CommentTreeDTO {

    private String commentId;
    private String content;

    private AuthorInfo author;

    /** 被回复者昵称（楼中楼才有） */
    private String replyToNickname;

    private Long likeCount;

    /** 当前用户是否已点赞 */
    private Boolean liked;

    private LocalDateTime createdAt;

    /** 楼中楼子评论（顶层评论才有） */
    private List<CommentTreeDTO> replies = new ArrayList<>();

    /** 楼中楼总数（顶层评论才有） */
    private Integer replyCount;

    @Data
    public static class AuthorInfo {
        private String userId;
        private String nickname;
        private String avatarUrl;
    }
}
