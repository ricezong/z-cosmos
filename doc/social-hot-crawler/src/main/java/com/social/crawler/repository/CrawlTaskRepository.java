package com.social.crawler.repository;

import com.social.crawler.model.CrawlTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrawlTaskRepository extends JpaRepository<CrawlTask, Long> {

    List<CrawlTask> findByPlatformOrderByCreatedAtDesc(String platform);

    List<CrawlTask> findByTaskTypeOrderByCreatedAtDesc(String taskType);

    List<CrawlTask> findByStatusOrderByCreatedAtDesc(String status);

    List<CrawlTask> findTop10ByOrderByCreatedAtDesc();
}
