package com.social.crawler.controller;

import com.social.crawler.model.CrawlTask;
import com.social.crawler.service.CrawlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 爬取控制器 - 触发和管理爬取任务
 */
@RestController
@RequestMapping("/api/crawl")
public class CrawlController {

    @Autowired
    private CrawlService crawlService;

    /** 爬取指定平台的热点 */
    @PostMapping("/hot-topics/{platform}")
    public ResponseEntity<Map<String, Object>> crawlHotTopics(
            @PathVariable String platform,
            @RequestParam(defaultValue = "50") int limit) {
        try {
            CrawlTask task = crawlService.crawlHotTopics(platform, limit);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "taskId", task.getId(),
                "fetchedCount", task.getFetchedCount(),
                "duration", task.getDuration(),
                "message", "热点爬取完成"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** 爬取所有平台的热点 */
    @PostMapping("/hot-topics")
    public ResponseEntity<Map<String, Object>> crawlAllHotTopics(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<CrawlTask> tasks = crawlService.crawlAllHotTopics(limit);
            int total = tasks.stream().mapToInt(CrawlTask::getFetchedCount).sum();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "taskCount", tasks.size(),
                "totalFetched", total,
                "message", String.format("全部平台爬取完成，共获取 %d 条热点", total)
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** 爬取指定话题的评论 */
    @PostMapping("/comments/{topicId}")
    public ResponseEntity<Map<String, Object>> crawlComments(
            @PathVariable Long topicId,
            @RequestParam(defaultValue = "100") int limit) {
        try {
            CrawlTask task = crawlService.crawlComments(topicId, limit);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "taskId", task.getId(),
                "fetchedCount", task.getFetchedCount(),
                "duration", task.getDuration(),
                "message", "评论爬取完成"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /** 获取最近的任务记录 */
    @GetMapping("/tasks")
    public ResponseEntity<List<CrawlTask>> getRecentTasks() {
        return ResponseEntity.ok(crawlService.getRecentTasks());
    }
}
