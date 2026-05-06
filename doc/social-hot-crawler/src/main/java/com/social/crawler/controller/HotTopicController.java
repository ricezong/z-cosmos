package com.social.crawler.controller;

import com.social.crawler.model.CrawlTask;
import com.social.crawler.model.Comment;
import com.social.crawler.model.HotTopic;
import com.social.crawler.service.CrawlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 热点控制器 - 热点浏览与搜索
 */
@RestController
@RequestMapping("/api/topics")
public class HotTopicController {

    @Autowired
    private CrawlService crawlService;

    /** 获取热点列表（支持按平台过滤） */
    @GetMapping
    public ResponseEntity<List<HotTopic>> getTopics(
            @RequestParam(required = false) String platform) {
        return ResponseEntity.ok(crawlService.getHotTopics(platform));
    }

    /** 获取话题详情 */
    @GetMapping("/{id}")
    public ResponseEntity<HotTopic> getTopicDetail(@PathVariable Long id) {
        HotTopic topic = crawlService.getTopicDetail(id);
        return topic != null ? ResponseEntity.ok(topic) : ResponseEntity.notFound().build();
    }

    /** 获取话题的评论 */
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(crawlService.getComments(id));
    }

    /** 获取评论的回复 */
    @GetMapping("/{id}/replies/{parentId}")
    public ResponseEntity<List<Comment>> getReplies(
            @PathVariable Long id, @PathVariable Long parentId) {
        return ResponseEntity.ok(crawlService.getReplies(parentId));
    }

    /** 搜索话题 */
    @GetMapping("/search")
    public ResponseEntity<List<HotTopic>> searchTopics(@RequestParam String q) {
        return ResponseEntity.ok(crawlService.searchTopics(q));
    }

    /** 搜索评论 */
    @GetMapping("/comments/search")
    public ResponseEntity<List<Comment>> searchComments(@RequestParam String q) {
        return ResponseEntity.ok(crawlService.searchComments(q));
    }
}
