package cn.kong.cosmos.biz.community.service;

/**
 * 收藏服务
 */
public interface CollectService {

    /** 收藏帖子（幂等） */
    void collect(String postId);

    /** 取消收藏（幂等） */
    void uncollect(String postId);

    /** 当前用户是否已收藏 */
    boolean hasCollected(String postId);
}
