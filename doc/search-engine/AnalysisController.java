package com.search.engine.controller;

import com.search.engine.model.Term;
import com.search.engine.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 分析控制器 - 对应博客中的"分析"模块的REST API
 */
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    /**
     * 分析单个网页
     * 对应博客中的"抽取网页文本信息" + "分词并创建临时索引"
     */
    @PostMapping("/page/{pageId}")
    public ResponseEntity<Map<String, Object>> analyzePage(@PathVariable Long pageId) {
        int tokenCount = analysisService.analyzePage(pageId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "pageId", pageId,
            "tokenCount", tokenCount,
            "message", String.format("网页 %d 分析完成，提取 %d 个词语", pageId, tokenCount)
        ));
    }

    /**
     * 分析所有网页
     */
    @PostMapping("/all")
    public ResponseEntity<Map<String, Object>> analyzeAll() {
        int count = analysisService.analyzeAll();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "analyzedPages", count,
            "message", String.format("批量分析完成，共分析 %d 个网页", count)
        ));
    }

    /**
     * 获取分析统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(analysisService.getStatistics());
    }

    /**
     * 获取高频词语
     */
    @GetMapping("/top-terms")
    public ResponseEntity<List<Term>> getTopTerms(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(analysisService.getTopTerms(limit));
    }
}
