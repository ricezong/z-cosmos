package cn.kong.cosmos.biz.community.dto.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子摘要 DTO - 列表页使用
 */
@Data
public class PostSummaryDTO {

    private String postId;
    private String title;
    private String summary;
    private String coverImage;
    private List<String> images;

    private AuthorInfo author;
    private CategoryInfo category;

    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Long collectCount;

    private Integer isTop;
    private Integer isEssence;

    private LocalDateTime createdAt;

    @Data
    public static class AuthorInfo {
        private String userId;
        private String nickname;
        private String avatarUrl;
    }

    @Data
    public static class CategoryInfo {
        private String code;
        private String name;
        private String color;
    }
}
