package com.social.crawler.repository;

import com.social.crawler.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** 根据话题ID查询评论（顶级评论） */
    List<Comment> findByTopicIdAndParentIdIsNullOrderByLikeCountDesc(Long topicId);

    /** 根据话题ID查询所有评论 */
    List<Comment> findByTopicIdOrderByPublishedAtAsc(Long topicId);

    /** 根据父评论ID查询回复 */
    List<Comment> findByParentIdOrderByPublishedAtAsc(Long parentId);

    /** 根据话题ID分页查询 */
    Page<Comment> findByTopicId(Long topicId, Pageable pageable);

    /** 按平台统计评论数 */
    long countByPlatform(String platform);

    /** 按话题统计评论数 */
    long countByTopicId(Long topicId);

    /** 搜索评论内容 */
    @Query("SELECT c FROM Comment c WHERE c.topicId = :topicId AND c.content LIKE %:keyword%")
    List<Comment> searchByTopicId(Long topicId, String keyword);

    /** 全平台搜索评论 */
    @Query("SELECT c FROM Comment c WHERE c.content LIKE %:keyword% ORDER BY c.likeCount DESC")
    List<Comment> searchAll(String keyword);

    /** 检查评论是否已存在 */
    boolean existsByPlatformAndCommentId(String platform, String commentId);

    /** 删除指定话题的所有评论 */
    void deleteByTopicId(Long topicId);

    /** 删除指定平台的所有评论 */
    void deleteByPlatform(String platform);
}
