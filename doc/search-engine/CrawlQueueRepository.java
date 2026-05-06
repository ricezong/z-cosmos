package com.search.engine.repository;

import com.search.engine.model.CrawlQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrawlQueueRepository extends JpaRepository<CrawlQueue, Long> {

    /** 根据URL查找 */
    Optional<CrawlQueue> findByUrl(String url);

    /** 检查URL是否已在队列中 */
    boolean existsByUrl(String url);

    /** 获取下一个待爬取的URL（按优先级和创建时间排序） */
    @Query("SELECT cq FROM CrawlQueue cq WHERE cq.status = 'PENDING' ORDER BY cq.priority ASC, cq.createdAt ASC LIMIT 1")
    Optional<CrawlQueue> findNextPending();

    /** 获取所有待爬取的URL */
    List<CrawlQueue> findByStatusOrderByPriorityAscCreatedAtAsc(String status);

    /** 统计各状态的URL数量 */
    long countByStatus(String status);

    /** 批量更新超时的爬取任务为PENDING */
    @Modifying
    @Query("UPDATE CrawlQueue cq SET cq.status = 'PENDING' WHERE cq.status = 'CRAWLING' AND cq.updatedAt < :timeout")
    int resetStaleTasks(java.time.LocalDateTime timeout);
}
