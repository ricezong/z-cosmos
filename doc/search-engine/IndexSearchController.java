package com.search.engine.controller;

import com.search.engine.service.IndexService;
import com.search.engine.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 索引和搜索控制器 - 对应博客中的"索引"和"查询"模块的REST API
 */
@RestController
@RequestMapping("/api")
public class IndexSearchController {

    @Autowired
    private IndexService indexService;

    @Autowired
    private SearchService searchService;

    // ==================== 索引相关API ====================

    /**
     * 构建倒排索引
     * 对应博客中的"构建倒排索引"模块
     */
    @PostMapping("/index/build")
    public ResponseEntity<Map<String, Object>> buildIndex() {
        int count = indexService.buildIndex();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "indexedPages", count,
            "message", String.format("倒排索引构建完成，共索引 %d 个网页", count)
        ));
    }

    /**
     * 获取倒排索引信息
     * 对应博客中的 term_offset.bin
     */
    @GetMapping("/index/term/{word}")
    public ResponseEntity<List<Map<String, Object>>> getTermIndex(@PathVariable String word) {
        return ResponseEntity.ok(indexService.getInvertedIndexForWord(word));
    }

    /**
     * 获取所有词语的索引偏移量列表
     * 对应博客中的 term_offset.bin
     */
    @GetMapping("/index/terms")
    public ResponseEntity<List<Map<String, Object>>> getTermOffsetList() {
        return ResponseEntity.ok(indexService.getTermOffsetList());
    }

    /**
     * 获取索引统计信息
     */
    @GetMapping("/index/statistics")
    public ResponseEntity<Map<String, Object>> getIndexStatistics() {
        return ResponseEntity.ok(indexService.getStatistics());
    }

    /**
     * 清空索引
     */
    @PostMapping("/index/clear")
    public ResponseEntity<Map<String, Object>> clearIndex() {
        indexService.clearIndex();
        return ResponseEntity.ok(Map.of("success", true, "message", "索引已清空"));
    }

    // ==================== 搜索相关API ====================

    /**
     * 搜索
     * 对应博客中的完整查询流程：
     * 用户输入 → 分词 → 查term_id → 查offset → 查index → 结果排序 → 返回
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {
        List<SearchService.SearchResult> results = searchService.search(q, limit);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("query", q);
        response.put("totalResults", results.size());
        response.put("results", results);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取系统整体统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getOverallStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.putAll(indexService.getStatistics());
        stats.putAll(indexService.getStatistics());
        return ResponseEntity.ok(stats);
    }
}
