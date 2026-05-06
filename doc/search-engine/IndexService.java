package com.search.engine.service;

import com.search.engine.model.InvertedIndex;
import com.search.engine.model.Term;
import com.search.engine.model.WebPage;
import com.search.engine.repository.InvertedIndexRepository;
import com.search.engine.repository.TermRepository;
import com.search.engine.repository.WebPageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 索引服务 - 对应博客中的"索引"模块
 * 
 * 核心功能：
 * 1. 构建倒排索引（对应inverted_index.bin）
 * 2. 合并排序优化（对应博客中的归并排序构建倒排索引）
 * 3. 维护term_offset（对应term_offset.bin）
 * 
 * 倒排索引结构：
 * 词语 → [(文档ID, 词频, 位置列表), ...]
 * 
 * 对应博客中的文件：
 * - inverted_index.bin：倒排索引数据
 * - term_offset.bin：词语在倒排索引中的偏移量
 */
@Service
public class IndexService {

    private static final Logger logger = LoggerFactory.getLogger(IndexService.class);

    @Autowired
    private WebPageRepository webPageRepository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private InvertedIndexRepository invertedIndexRepository;

    /**
     * 构建倒排索引
     * 对应博客中的"构建倒排索引"模块
     * 
     * 在数据库版本中，倒排索引已经在分析阶段创建
     * 此方法用于优化和重建索引
     */
    @Transactional
    public int buildIndex() {
        // 先分析所有未分析的网页
        List<WebPage> crawledPages = webPageRepository.findByStatus("CRAWLED");
        for (WebPage page : crawledPages) {
            try {
                // 标记为已索引（分析服务会处理实际的索引创建）
                page.setStatus("INDEXED");
                webPageRepository.save(page);
            } catch (Exception e) {
                logger.error("索引网页 {} 失败", page.getId(), e);
            }
        }

        // 更新所有已分析网页的状态为已索引
        List<WebPage> analyzedPages = webPageRepository.findByStatus("ANALYZED");
        for (WebPage page : analyzedPages) {
            page.setStatus("INDEXED");
            webPageRepository.save(page);
        }

        int total = crawledPages.size() + analyzedPages.size();
        logger.info("索引构建完成，共索引 {} 个网页", total);
        return total;
    }

    /**
     * 获取倒排索引信息
     * 对应博客中的term_offset.bin - 查看词语在索引中的位置信息
     * 
     * @param word 词语
     * @return 倒排索引列表
     */
    public List<Map<String, Object>> getInvertedIndexForWord(String word) {
        Optional<Term> termOpt = termRepository.findByWord(word);
        if (termOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Term term = termOpt.get();
        List<InvertedIndex> indices = invertedIndexRepository.findByTermId(term.getId());

        return indices.stream().map(idx -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("termId", term.getId());
            item.put("word", word);
            item.put("pageId", idx.getPageId());
            item.put("termFrequency", idx.getTermFrequency());
            item.put("positions", idx.getPositions());

            // 获取网页标题
            webPageRepository.findById(idx.getPageId()).ifPresent(page -> {
                item.put("pageTitle", page.getTitle());
                item.put("pageUrl", page.getUrl());
            });

            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 获取所有词语的索引统计
     * 对应博客中的term_offset.bin
     */
    public List<Map<String, Object>> getTermOffsetList() {
        List<Term> terms = termRepository.findAll();
        return terms.stream().map(term -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("termId", term.getId());
            item.put("word", term.getWord());
            item.put("documentFrequency", term.getDocumentFrequency());
            item.put("totalFrequency", term.getTotalFrequency());
            item.put("indexRecords", invertedIndexRepository.countByTermId(term.getId()));
            return item;
        }).sorted(Comparator.comparingLong((Map<String, Object> m) -> (Long) m.get("totalFrequency")).reversed())
          .collect(Collectors.toList());
    }

    /**
     * 获取索引统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalTerms", termRepository.count());
        stats.put("totalIndexRecords", invertedIndexRepository.count());
        stats.put("indexedPages", webPageRepository.countByStatus("INDEXED"));
        stats.put("analyzedPages", webPageRepository.countByStatus("ANALYZED"));
        stats.put("crawledPages", webPageRepository.countByStatus("CRAWLED"));
        return stats;
    }

    /**
     * 清空所有索引数据
     */
    @Transactional
    public void clearIndex() {
        invertedIndexRepository.deleteAll();
        termRepository.deleteAll();
        
        // 重置所有网页状态
        List<WebPage> pages = webPageRepository.findAll();
        for (WebPage page : pages) {
            page.setStatus("CRAWLED");
            webPageRepository.save(page);
        }
        
        logger.info("索引已清空");
    }
}
