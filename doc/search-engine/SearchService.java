package com.search.engine.service;

import com.search.engine.model.InvertedIndex;
import com.search.engine.model.Term;
import com.search.engine.model.WebPage;
import com.search.engine.repository.InvertedIndexRepository;
import com.search.engine.repository.TermRepository;
import com.search.engine.repository.WebPageRepository;
import com.search.engine.util.WordSegmenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索服务 - 对应博客中的"查询"模块
 * 
 * 核心功能：
 * 1. 对查询关键词进行分词
 * 2. 查询term_id（对应term_offset.bin）
 * 3. 查询offset获取倒排索引位置
 * 4. 查询inverted_index获取包含关键词的文档
 * 
 * 查询流程（对应博客中的查询流程图）：
 * 用户输入 → 分词 → 查term_id → 查offset → 查index → 结果排序 → 返回
 * 
 * 使用的文件：
 * - bloom_filter.bin：URL去重（爬取阶段使用）
 * - doc_raw.bin：网页原始内容
 * - term_offset.bin：词语偏移量
 * - inverted_index.bin：倒排索引
 */
@Service
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private WebPageRepository webPageRepository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private InvertedIndexRepository invertedIndexRepository;

    @Autowired
    private WordSegmenter wordSegmenter;

    /**
     * 搜索
     * 对应博客中的完整查询流程
     * 
     * @param query 用户查询字符串
     * @param limit 返回结果数量限制
     * @return 搜索结果列表
     */
    public List<SearchResult> search(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 步骤1：分词（对应博客中的"分词"步骤）
        List<String> tokens = wordSegmenter.segment(query.trim());
        if (tokens.isEmpty()) {
            return Collections.emptyList();
        }

        logger.debug("查询分词结果：{}", tokens);

        // 步骤2：查询term_id（对应博客中的"查term_id"步骤）
        // 将分词结果映射到词语ID
        Map<String, Long> tokenToTermId = new LinkedHashMap<>();
        for (String token : tokens) {
            termRepository.findByWord(token).ifPresent(term -> {
                tokenToTermId.put(token, term.getId());
            });
        }

        if (tokenToTermId.isEmpty()) {
            logger.debug("没有找到匹配的词语");
            return Collections.emptyList();
        }

        // 步骤3：查offset → 查index（对应博客中的"查offset → 查index"步骤）
        // 对每个词语，获取其倒排索引记录
        Map<Long, List<InvertedIndex>> termIdToIndices = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : tokenToTermId.entrySet()) {
            List<InvertedIndex> indices = invertedIndexRepository.findByTermId(entry.getValue());
            termIdToIndices.put(entry.getValue(), indices);
        }

        // 步骤4：计算文档得分并排序
        // 使用TF-IDF简化版进行排序
        Map<Long, Double> pageScores = new HashMap<>();
        Map<Long, Map<String, Object>> pageDetails = new HashMap<>();

        long totalDocs = webPageRepository.count();

        for (Map.Entry<Long, List<InvertedIndex>> entry : termIdToIndices.entrySet()) {
            Long termId = entry.getKey();
            List<InvertedIndex> indices = entry.getValue();

            // 获取词语信息
            Term term = termRepository.findById(termId).orElse(null);
            if (term == null) continue;

            for (InvertedIndex idx : indices) {
                Long pageId = idx.getPageId();

                // TF-IDF计算
                double tf = 1.0 + Math.log(idx.getTermFrequency());
                double idf = Math.log((totalDocs + 1) / (term.getDocumentFrequency() + 1)) + 1;
                double score = tf * idf;

                pageScores.merge(pageId, score, Double::sum);

                // 记录匹配详情
                pageDetails.computeIfAbsent(pageId, k -> new LinkedHashMap<>());
                Map<String, Object> details = pageDetails.get(pageId);
                details.put("matchedTerms", details.getOrDefault("matchedTerms", "") + 
                    (details.getOrDefault("matchedTerms", "").toString().isEmpty() ? "" : ", ") + term.getWord());
            }
        }

        // 步骤5：排序并返回结果
        List<SearchResult> results = pageScores.entrySet().stream()
            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> {
                Long pageId = entry.getKey();
                double score = entry.getValue();
                WebPage page = webPageRepository.findById(pageId).orElse(null);
                if (page == null) return null;

                SearchResult result = new SearchResult();
                result.setPageId(pageId);
                result.setTitle(page.getTitle());
                result.setUrl(page.getUrl());
                result.setScore(score);
                result.setSnippet(generateSnippet(page.getTextContent(), tokens));
                result.setMatchedTerms(pageDetails.get(pageId).getOrDefault("matchedTerms", "").toString());
                return result;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        logger.info("搜索 '{}' 返回 {} 个结果", query, results.size());
        return results;
    }

    /**
     * 生成搜索结果摘要
     * 在文本中找到关键词附近的片段作为摘要
     */
    private String generateSnippet(String textContent, List<String> tokens) {
        if (textContent == null || textContent.isEmpty()) return "";

        // 找到第一个关键词出现的位置
        int bestPos = -1;
        for (String token : tokens) {
            int pos = textContent.indexOf(token);
            if (pos != -1 && (bestPos == -1 || pos < bestPos)) {
                bestPos = pos;
            }
        }

        if (bestPos == -1) {
            // 没有找到关键词，返回文本开头
            return textContent.length() > 200 ? textContent.substring(0, 200) + "..." : textContent;
        }

        // 提取关键词前后的文本作为摘要
        int start = Math.max(0, bestPos - 80);
        int end = Math.min(textContent.length(), bestPos + 120);
        String snippet = textContent.substring(start, end);

        if (start > 0) snippet = "..." + snippet;
        if (end < textContent.length()) snippet = snippet + "...";

        return snippet;
    }

    /**
     * 搜索结果
     */
    public static class SearchResult {
        private Long pageId;
        private String title;
        private String url;
        private double score;
        private String snippet;
        private String matchedTerms;

        // Getters and Setters
        public Long getPageId() { return pageId; }
        public void setPageId(Long pageId) { this.pageId = pageId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public String getSnippet() { return snippet; }
        public void setSnippet(String snippet) { this.snippet = snippet; }
        public String getMatchedTerms() { return matchedTerms; }
        public void setMatchedTerms(String matchedTerms) { this.matchedTerms = matchedTerms; }
    }
}
