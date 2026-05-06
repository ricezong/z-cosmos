package cn.kong.cosmos.biz.community.dto.resp;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子详情 DTO - 继承摘要，额外包含 content 和当前用户互动状态
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostDetailDTO extends PostSummaryDTO {

    /** 完整 HTML 富文本 */
    private String content;

    /** 当前用户是否已点赞 */
    private Boolean liked;

    /** 当前用户是否已收藏 */
    private Boolean collected;

    /** 当前用户是否为作者 */
    private Boolean isAuthor;
}
