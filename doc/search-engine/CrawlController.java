package com.search.engine.controller;

import com.search.engine.model.CrawlQueue;
import com.search.engine.model.WebPage;
import com.search.engine.service.CrawlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 爬取控制器 - 对应博客中的"搜集"模块的REST API
 */
@RestController
@RequestMapping("/api/crawl")
public class CrawlController {

    @Autowired
    private CrawlService crawlService;

    /**
     * 添加URL到爬取队列
     * 对应博客中的 links.bin（URL队列）
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL不能为空"));
        }
        boolean added = crawlService.addToQueue(url.trim());
        return ResponseEntity.ok(Map.of(
            "success", added,
            "url", url.trim(),
            "message", added ? "URL已添加到爬取队列" : "URL已存在或格式无效"
        ));
    }

    /**
     * 批量添加URL到爬取队列
     */
    @PostMapping("/add-batch")
    public ResponseEntity<Map<String, Object>> addUrls(@RequestBody Map<String, List<String>> request) {
        List<String> urls = request.get("urls");
        if (urls == null || urls.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL列表不能为空"));
        }
        int added = 0;
        for (String url : urls) {
            if (crawlService.addToQueue(url.trim())) added++;
        }
        return ResponseEntity.ok(Map.of(
            "success", true,
            "total", urls.size(),
            "added", added,
            "message", String.format("已添加 %d/%d 个URL到爬取队列", added, urls.size())
        ));
    }

    /**
     * 开始爬取
     * 对应博客中的BFS遍历过程
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startCrawl(
            @RequestParam(defaultValue = "10") int maxPages) {
        if (crawlService.isCrawling()) {
            return ResponseEntity.ok(Map.of("message", "爬取正在进行中"));
        }
        crawlService.setCrawling(true);
        try {
            int crawled = crawlService.startCrawling(maxPages);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "crawledPages", crawled,
                "message", String.format("爬取完成，共爬取 %d 个网页", crawled)
            ));
        } finally {
            crawlService.setCrawling(false);
        }
    }

    /**
     * 停止爬取
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopCrawl() {
        crawlService.setCrawling(false);
        return ResponseEntity.ok(Map.of("success", true, "message", "爬取已停止"));
    }

    /**
     * 获取爬取统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(crawlService.getStatistics());
    }

    /**
     * 获取所有已爬取的网页
     */
    @GetMapping("/pages")
    public ResponseEntity<List<WebPage>> getAllPages() {
        return ResponseEntity.ok(crawlService.getAllPages());
    }

    /**
     * 获取待爬取的URL列表
     */
    @GetMapping("/pending")
    public ResponseEntity<List<CrawlQueue>> getPendingUrls() {
        return ResponseEntity.ok(crawlService.getPendingUrls());
    }

    /**
     * 获取单个网页详情
     */
    @GetMapping("/pages/{id}")
    public ResponseEntity<WebPage> getPage(@PathVariable Long id) {
        return crawlService.getAllPages().stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
