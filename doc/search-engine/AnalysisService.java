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
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 分析服务 - 对应博客中的"分析"模块
 * 
 * 核心功能：
 * 1. 抽取网页文本信息（去除JS/CSS/HTML标签）
 * 2. 分词（使用Trie树进行前向最大匹配）
 * 3. 创建临时索引（对应tmp_Index.bin）
 * 4. 单词编号（使用哈希表为每个词分配唯一编号）
 */
@Service
public class AnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);

    @Autowired
    private WebPageRepository webPageRepository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private InvertedIndexRepository invertedIndexRepository;

    @Autowired
    private WordSegmenter wordSegmenter;

    /**
     * 分析单个网页
     * 对应博客中的"分析"阶段：
     * 1. 抽取网页文本信息
     * 2. 分词并创建临时索引
     * 3. 单词编号
     * 
     * @param pageId 网页ID
     * @return 分词结果数量
     */
    @Transactional
    public int analyzePage(Long pageId) {
        Optional<WebPage> pageOpt = webPageRepository.findById(pageId);
        if (pageOpt.isEmpty()) {
            logger.warn("网页不存在：{}", pageId);
            return 0;
        }

        WebPage page = pageOpt.get();
        String textContent = page.getTextContent();
        if (textContent == null || textContent.isEmpty()) {
            logger.warn("网页文本内容为空：{}", pageId);
            return 0;
        }

        // 步骤1：分词（对应博客中的"分词并创建临时索引"）
        List<String> tokens = wordSegmenter.segment(textContent);
        logger.debug("网页 {} 分词结果：{} 个词", pageId, tokens.size());

        // 步骤2：构建临时索引（对应tmp_Index.bin）
        // 临时索引结构：Map<词语, List<位置>>
        Map<String, List<Integer>> tempIndex = new LinkedHashMap<>();
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            tempIndex.computeIfAbsent(token, k -> new ArrayList<>()).add(i);
        }

        // 步骤3：单词编号并持久化到数据库
        // 对应博客中的"单词编号" - 使用哈希表为每个词分配唯一编号
        int indexCount = 0;
        for (Map.Entry<String, List<Integer>> entry : tempIndex.entrySet()) {
            String word = entry.getValue().get(0) != null ? entry.getKey() : entry.getKey();
            List<Integer> positions = entry.getValue();

            // 获取或创建词语记录（单词编号）
            Term term = termRepository.findByWord(word).orElseGet(() -> {
                Term newTerm = new Term();
                newTerm.setWord(word);
                newTerm.setDocumentFrequency(0);
                newTerm.setTotalFrequency(0);
                return termRepository.save(newTerm);
            });

            // 更新词语统计
            term.setDocumentFrequency(term.getDocumentFrequency() + 1);
            term.setTotalFrequency(term.getTotalFrequency() + positions.size());
            termRepository.save(term);

            // 创建倒排索引记录
            InvertedIndex index = new InvertedIndex();
            index.setTermId(term.getId());
            index.setPageId(pageId);
            index.setTermFrequency(positions.size());
            // 将位置列表转为JSON字符串存储
            index.setPositions(positions.toString());
            invertedIndexRepository.save(index);
            indexCount++;
        }

        // 更新网页状态
        page.setStatus("ANALYZED");
        webPageRepository.save(page);

        logger.info("网页 {} 分析完成，生成 {} 条索引", pageId, indexCount);
        return indexCount;
    }

    /**
     * 分析所有已爬取但未分析的网页
     * @return 分析的网页数量
     */
    @Transactional
    public int analyzeAll() {
        List<WebPage> pages = webPageRepository.findByStatus("CRAWLED");
        int count = 0;
        for (WebPage page : pages) {
            try {
                analyzePage(page.getId());
                count++;
            } catch (Exception e) {
                logger.error("分析网页 {} 失败", page.getId(), e);
            }
        }
        logger.info("批量分析完成，共分析 {} 个网页", count);
        return count;
    }

    /**
     * 获取分析统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalTerms", termRepository.count());
        stats.put("totalIndexRecords", invertedIndexRepository.count());
        stats.put("crawledPages", webPageRepository.countByStatus("CRAWLED"));
        stats.put("analyzedPages", webPageRepository.countByStatus("ANALYZED"));
        stats.put("indexedPages", webPageRepository.countByStatus("INDEXED"));
        stats.put("dictionarySize", wordSegmenter.getDictionarySize());
        return stats;
    }

    /**
     * 获取所有词语（按频率排序）
     */
    public List<Term> getTopTerms(int limit) {
        List<Term> allTerms = termRepository.findAll();
        allTerms.sort((a, b) -> Long.compare(b.getTotalFrequency(), a.getTotalFrequency()));
        return allTerms.subList(0, Math.min(limit, allTerms.size()));
    }
}
