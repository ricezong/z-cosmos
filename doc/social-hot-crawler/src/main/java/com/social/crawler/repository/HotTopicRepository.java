package com.social.crawler.repository;

import com.social.crawler.model.HotTopic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotTopicRepository extends JpaRepository<HotTopic, Long> {

    /** 根据平台和话题ID查找 */
    Optional<HotTopic> findByPlatformAndTopicId(String platform, String topicId);

    /** 检查是否已存在 */
    boolean existsByPlatformAndTopicId(String platform, String topicId);

    /** 按平台查询，按热度排序 */
    List<HotTopic> findByPlatformOrderByHeatValueDesc(String platform);

    /** 按平台查询，按排名排序 */
    List<HotTopic> findByPlatformOrderByRankAsc(String platform);

    /** 按平台分页查询 */
    Page<HotTopic> findByPlatform(String platform, Pageable pageable);

    /** 查询所有平台最新热点（每个平台取前N条） */
    @Query("SELECT h FROM HotTopic h WHERE h.crawledAt = (" +
           "SELECT MAX(h2.crawledAt) FROM HotTopic h2 WHERE h2.platform = h.platform) " +
           "ORDER BY h.heatValue DESC")
    List<HotTopic> findLatestHotTopics();

    /** 按平台统计数量 */
    long countByPlatform(String platform);

    /** 搜索标题或摘要 */
    @Query("SELECT h FROM HotTopic h WHERE h.platform = :platform AND " +
           "(h.title LIKE %:keyword% OR h.summary LIKE %:keyword%) " +
           "ORDER BY h.heatValue DESC")
    List<HotTopic> searchByPlatform(String platform, String keyword);

    /** 全平台搜索 */
    @Query("SELECT h FROM HotTopic h WHERE " +
           "(h.title LIKE %:keyword% OR h.summary LIKE %:keyword%) " +
           "ORDER BY h.heatValue DESC")
    List<HotTopic> searchAll(String keyword);

    /** 获取指定平台最新的爬取时间 */
    @Query("SELECT MAX(h.crawledAt) FROM HotTopic h WHERE h.platform = :platform")
    LocalDateTime findLatestCrawlTime(String platform);

    /** 删除指定平台的所有热点 */
    void deleteByPlatform(String platform);
}
