package com.search.engine.service;

import com.search.engine.model.CrawlQueue;
import com.search.engine.model.WebPage;
import com.search.engine.repository.CrawlQueueRepository;
import com.search.engine.repository.WebPageRepository;
import com.search.engine.util.BloomFilter;
import com.search.engine.util.WordSegmenter;
import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 爬取服务 - 对应博客中的"搜集"模块
 * 
 * 核心功能：
 * 1. BFS遍历互联网（有向图）
 * 2. 使用布隆过滤器进行URL去重（bloom_filter.bin）
 * 3. 使用队列管理待爬取URL（links.bin）
 * 4. 存储网页原始内容（doc_raw.bin）
 * 5. 分配文档ID（doc_id.bin）
 */
@Service
public class CrawlService {

    private static final Logger logger = LoggerFactory.getLogger(CrawlService.class);

    @Autowired
    private WebPageRepository webPageRepository;

    @Autowired
    private CrawlQueueRepository crawlQueueRepository;

    @Autowired
    private WordSegmenter wordSegmenter;

    /** 布隆过滤器 - 内存中维护，用于快速URL去重 */
    private final BloomFilter bloomFilter = new BloomFilter(1_000_000, 0.01);

    /** HTTP客户端 */
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    /** URL正则 - 用于从HTML中提取链接 */
    private static final Pattern URL_PATTERN = Pattern.compile(
        "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+"
    );

    /** 是否正在爬取 */
    private volatile boolean isCrawling = false;

    @PostConstruct
    public void init() {
        // 初始化时，将数据库中已有的URL加载到布隆过滤器
        List<WebPage> existingPages = webPageRepository.findAll();
        for (WebPage page : existingPages) {
            bloomFilter.add(page.getUrl());
        }
        List<CrawlQueue> existingQueue = crawlQueueRepository.findAll();
        for (CrawlQueue cq : existingQueue) {
            bloomFilter.add(cq.getUrl());
        }
        logger.info("布隆过滤器初始化完成，已加载 {} 个URL", existingPages.size() + existingQueue.size());
    }

    /**
     * 添加种子URL到爬取队列
     * @param seedUrl 种子URL
     */
    public void addSeedUrl(String seedUrl) {
        if (bloomFilter.mightContain(seedUrl) || crawlQueueRepository.existsByUrl(seedUrl)) {
            logger.debug("URL已存在，跳过：{}", seedUrl);
            return;
        }
        bloomFilter.add(seedUrl);
        CrawlQueue queue = new CrawlQueue();
        queue.setUrl(seedUrl);
        queue.setPriority(0);
        crawlQueueRepository.save(queue);
        logger.info("添加种子URL：{}", seedUrl);
    }

    /**
     * 批量添加种子URL
     */
    public void addSeedUrls(List<String> urls) {
        for (String url : urls) {
            addSeedUrl(url);
        }
    }

    /**
     * 执行BFS爬取（单步）
     * 对应博客中的BFS遍历有向图
     * @return 本次爬取的网页数量
     */
    public int crawlOne() {
        Optional<CrawlQueue> nextOpt = crawlQueueRepository.findNextPending();
        if (nextOpt.isEmpty()) return 0;

        CrawlQueue queueItem = nextOpt.get();
        queueItem.setStatus("CRAWLING");
        crawlQueueRepository.save(queueItem);

        try {
            // 抓取网页
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(queueItem.getUrl()))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "MiniSearchEngine/1.0 (Educational Crawler)")
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String html = response.body();

                // 解析HTML
                Document doc = Jsoup.parse(html, queueItem.getUrl());
                String title = doc.title();
                String textContent = extractText(doc);

                // 存储网页（对应doc_raw.bin）
                WebPage page = new WebPage();
                page.setUrl(queueItem.getUrl());
                page.setTitle(title);
                page.setRawContent(html);
                page.setTextContent(textContent);
                page.setStatus("CRAWLED");
                webPageRepository.save(page);

                // 提取并添加新的链接到队列（对应links.bin的BFS扩展）
                extractAndAddLinks(doc, queueItem.getUrl());

                // 标记队列项为完成
                queueItem.setStatus("DONE");
                crawlQueueRepository.save(queueItem);

                logger.info("成功爬取：{} (标题: {})", queueItem.getUrl(), title);
                return 1;
            } else {
                queueItem.setStatus("FAILED");
                queueItem.setErrorMessage("HTTP " + response.statusCode());
                crawlQueueRepository.save(queueItem);
                return 0;
            }
        } catch (Exception e) {
            logger.error("爬取失败：{}", queueItem.getUrl(), e);
            queueItem.setStatus("FAILED");
            queueItem.setErrorMessage(e.getMessage());
            crawlQueueRepository.save(queueItem);
            return 0;
        }
    }

    /**
     * 批量爬取
     * @param maxPages 最大爬取页数
     * @return 实际爬取页数
     */
    public int crawlBatch(int maxPages) {
        int count = 0;
        for (int i = 0; i < maxPages; i++) {
            int result = crawlOne();
            count += result;
            if (result == 0) break;
            // 礼貌性延迟，避免过于频繁的请求
            try { Thread.sleep(500); } catch (InterruptedException e) { break; }
        }
        return count;
    }

    /**
     * 从HTML文档中提取纯文本
     * 对应博客中的"抽取网页文本信息"模块
     * 去除JavaScript、CSS、HTML标签等
     */
    private String extractText(Document doc) {
        // 移除不需要的标签
        doc.select("script, style, noscript, iframe, svg, link, meta, input, button, nav, footer, header").remove();
        
        // 获取纯文本
        String text = doc.body() != null ? doc.body().text() : doc.text();
        
        // 清理文本：去除多余空白
        text = text.replaceAll("\\s+", " ").trim();
        
        return text;
    }

    /**
     * 从HTML文档中提取链接并添加到队列
     * 对应博客中的links.bin - BFS队列扩展
     */
    private void extractAndAddLinks(Document doc, String baseUrl) {
        Elements links = doc.select("a[href]");
        Set<String> newUrls = new HashSet<>();

        for (Element link : links) {
            String href = link.attr("abs:href");
            if (href == null || href.isEmpty()) continue;

            // 过滤无效链接
            if (!isValidUrl(href)) continue;

            // 使用布隆过滤器快速去重
            if (bloomFilter.mightContain(href)) continue;

            bloomFilter.add(href);
            newUrls.add(href);
        }

        // 批量保存新URL到队列
        for (String url : newUrls) {
            if (!crawlQueueRepository.existsByUrl(url) && !webPageRepository.existsByUrl(url)) {
                CrawlQueue queue = new CrawlQueue();
                queue.setUrl(url);
                queue.setPriority(1);
                crawlQueueRepository.save(queue);
            }
        }

        if (!newUrls.isEmpty()) {
            logger.debug("从 {} 提取了 {} 个新链接", baseUrl, newUrls.size());
        }
    }

    /**
     * 验证URL是否有效
     */
    private boolean isValidUrl(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getScheme() != null && 
                   (uri.getScheme().equals("http") || uri.getScheme().equals("https")) &&
                   uri.getHost() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取爬取统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalPages", webPageRepository.count());
        stats.put("crawledPages", webPageRepository.countByStatus("CRAWLED"));
        stats.put("analyzedPages", webPageRepository.countByStatus("ANALYZED"));
        stats.put("indexedPages", webPageRepository.countByStatus("INDEXED"));
        stats.put("pendingUrls", crawlQueueRepository.countByStatus("PENDING"));
        stats.put("crawlingUrls", crawlQueueRepository.countByStatus("CRAWLING"));
        stats.put("failedUrls", crawlQueueRepository.countByStatus("FAILED"));
        stats.put("doneUrls", crawlQueueRepository.countByStatus("DONE"));
        stats.put("bloomFilterBits", bloomFilter.getBitCount());
        stats.put("bloomFilterSize", bloomFilter.getBitSize());
        stats.put("bloomFilterFPRate", String.format("%.4f", bloomFilter.getCurrentFalsePositiveRate()));
        return stats;
    }

    /**
     * 获取所有已爬取的网页
     */
    public List<WebPage> getAllPages() {
        return webPageRepository.findAll();
    }

    /**
     * 获取待爬取的URL列表
     */
    public List<CrawlQueue> getPendingUrls() {
        return crawlQueueRepository.findByStatusOrderByPriorityAscCreatedAtAsc("PENDING");
    }

    public boolean isCrawling() {
        return isCrawling;
    }

    public void setCrawling(boolean crawling) {
        isCrawling = crawling;
    }
}
