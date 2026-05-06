package com.social.crawler.controller;

import com.social.crawler.service.CrawlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 平台管理控制器 - 查看已注册的平台和系统统计
 */
@RestController
@RequestMapping("/api/platforms")
public class PlatformController {

    @Autowired
    private CrawlService crawlService;

    /** 获取所有已注册的平台 */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getPlatforms() {
        return ResponseEntity.ok(crawlService.getPlatforms());
    }

    /** 获取系统统计信息 */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(crawlService.getStatistics());
    }
}
