package cn.kong.cosmos.biz.community.service;

/**
 * 点赞服务（帖子 + 评论通用）
 */
public interface LikeService {

    /** 点赞（幂等，已点赞不重复） */
    void like(String targetType, String targetId);

    /** 取消点赞（幂等） */
    void unlike(String targetType, String targetId);

    /** 当前用户是否已点赞 */
    boolean hasLiked(String targetType, String targetId);
}
