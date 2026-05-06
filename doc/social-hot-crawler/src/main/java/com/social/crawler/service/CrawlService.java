package com.social.crawler.service;

import com.social.crawler.adapter.PlatformAdapter;
import com.social.crawler.model.CrawlTask;
import com.social.crawler.model.HotTopic;
import com.social.crawler.repository.CrawlTaskRepository;
import com.social.crawler.repository.CommentRepository;
import com.social.crawler.repository.HotTopicRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 爬取调度服务
 *
 * 核心职责：
 * 1. 管理所有平台适配器（自动发现Spring容器中的PlatformAdapter Bean）
 * 2. 调度热点爬取任务
 * 3. 调度评论爬取任务
 * 4. 记录爬取日志
 *
 * 扩展性：新增平台只需创建Adapter Bean，此服务会自动发现并管理
 */
@Service
public class CrawlService {

    private static final Logger log = LoggerFactory.getLogger(CrawlService.class);

    @Autowired
    private HotTopicRepository hotTopicRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CrawlTaskRepository crawlTaskRepository;

    /** 所有已注册的平台适配器（Spring自动注入） */
    private final Map<String, PlatformAdapter> adapters = new LinkedHashMap<>();

    @Autowired
    public void setAdapters(List<PlatformAdapter> adapterList) {
        for (PlatformAdapter adapter : adapterList) {
            adapters.put(adapter.getPlatformId(), adapter);
            log.info("注册平台适配器: {} ({})", adapter.getPlatformName(), adapter.getPlatformId());
        }
    }

    // ==================== 热点爬取 ====================

    /**
     * 爬取指定平台的热点
     */
    @Transactional
    public CrawlTask crawlHotTopics(String platformId, int limit) {
        PlatformAdapter adapter = adapters.get(platformId);
        if (adapter == null) {
            throw new RuntimeException("未找到平台适配器: " + platformId);
        }

        CrawlTask task = createTask(platformId, "HOT_TOPICS");
        long startTime = System.currentTimeMillis();

        try {
            task.setStatus("RUNNING");
            crawlTaskRepository.save(task);

            List<HotTopic> topics = adapter.fetchHotTopics(limit);
            int newCount = 0;

            for (HotTopic topic : topics) {
                if (!hotTopicRepository.existsByPlatformAndTopicId(platformId, topic.getTopicId())) {
                    hotTopicRepository.save(topic);
                    newCount++;
                } else {
                    // 更新已有热点的热度值和排名
                    hotTopicRepository.findByPlatformAndTopicId(platformId, topic.getTopicId())
                        .ifPresent(existing -> {
                            existing.setHeatValue(topic.getHeatValue());
                            existing.setRank(topic.getRank());
                            existing.setHeatLabel(topic.getHeatLabel());
                            existing.setCommentCount(topic.getCommentCount());
                            existing.setCrawledAt(LocalDateTime.now());
                            hotTopicRepository.save(existing);
                        });
                }
            }

            task.setFetchedCount(topics.size());
            task.setStatus("SUCCESS");
            task.setDuration(System.currentTimeMillis() - startTime);
            task.setFinishedAt(LocalDateTime.now());
            crawlTaskRepository.save(task);

            log.info("[{}] 热点爬取完成: 共{}条, 新增{}条", platformId, topics.size(), newCount);
            return task;

        } catch (Exception e) {
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            task.setDuration(System.currentTimeMillis() - startTime);
            task.setFinishedAt(LocalDateTime.now());
            crawlTaskRepository.save(task);
            log.error("[{}] 热点爬取失败", platformId, e);
            throw e;
        }
    }

    /**
     * 爬取所有平台的热点
     */
    @Transactional
    public List<CrawlTask> crawlAllHotTopics(int limit) {
        List<CrawlTask> results = new ArrayList<>();
        for (String platformId : adapters.keySet()) {
            try {
                results.add(crawlHotTopics(platformId, limit));
            } catch (Exception e) {
                log.error("爬取平台 {} 失败", platformId, e);
            }
        }
        return results;
    }

    // ==================== 评论爬取 ====================

    /**
     * 爬取指定话题的评论
     */
    @Transactional
    public CrawlTask crawlComments(Long topicId, int limit) {
        HotTopic topic = hotTopicRepository.findById(topicId)
            .orElseThrow(() -> new RuntimeException("话题不存在: " + topicId));

        PlatformAdapter adapter = adapters.get(topic.getPlatform());
        if (adapter == null) {
            throw new RuntimeException("未找到平台适配器: " + topic.getPlatform());
        }

        CrawlTask task = createTask(topic.getPlatform(), "COMMENTS");
        task.setTopicId(topicId);
        task.setTopicOriginalId(topic.getTopicId());
        long startTime = System.currentTimeMillis();

        try {
            task.setStatus("RUNNING");
            crawlTaskRepository.save(task);

            List<Comment> comments = adapter.fetchComments(topic.getTopicId(), limit);
            int newCount = 0;

            for (Comment comment : comments) {
                comment.setTopicId(topicId);
                if (!commentRepository.existsByPlatformAndCommentId(topic.getPlatform(), comment.getCommentId())) {
                    commentRepository.save(comment);
                    newCount++;
                }
            }

            topic.setCommentsFetched(true);
            topic.setCommentCount((long) commentRepository.countByTopicId(topicId));
            hotTopicRepository.save(topic);

            task.setFetchedCount(comments.size());
            task.setStatus("SUCCESS");
            task.setDuration(System.currentTimeMillis() - startTime);
            task.setFinishedAt(LocalDateTime.now());
            crawlTaskRepository.save(task);

            log.info("[{}] 评论爬取完成: 话题={}, 共{}条, 新增{}条",
                topic.getPlatform(), topicId, comments.size(), newCount);
            return task;

        } catch (Exception e) {
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            task.setDuration(System.currentTimeMillis() - startTime);
            task.setFinishedAt(LocalDateTime.now());
            crawlTaskRepository.save(task);
            log.error("[{}] 评论爬取失败: 话题={}", topic.getPlatform(), topicId, e);
            throw e;
        }
    }

    // ==================== 查询功能 ====================

    public List<HotTopic> getHotTopics(String platformId) {
        if (platformId == null || platformId.equals("all")) {
            return hotTopicRepository.findLatestHotTopics();
        }
        return hotTopicRepository.findByPlatformOrderByHeatValueDesc(platformId);
    }

    public HotTopic getTopicDetail(Long id) {
        return hotTopicRepository.findById(id).orElse(null);
    }

    public List<Comment> getComments(Long topicId) {
        return commentRepository.findByTopicIdOrderByPublishedAtAsc(topicId);
    }

    public List<Comment> getReplies(Long parentId) {
        return commentRepository.findByParentIdOrderByPublishedAtAsc(parentId);
    }

    public List<HotTopic> searchTopics(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return List.of();
        return hotTopicRepository.searchAll(keyword.trim());
    }

    public List<Comment> searchComments(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return List.of();
        return commentRepository.searchAll(keyword.trim());
    }

    // ==================== 平台管理 ====================

    public List<Map<String, Object>> getPlatforms() {
        return adapters.values().stream().map(a -> {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("id", a.getPlatformId());
            info.put("name", a.getPlatformName());
            info.put("icon", a.getPlatformIcon());
            info.put("capabilities", a.getCapabilities().stream()
                .map(Enum::name).collect(Collectors.toList()));
            info.put("status", a.getStatus());
            info.put("topicCount", hotTopicRepository.countByPlatform(a.getPlatformId()));
            info.put("commentCount", commentRepository.countByPlatform(a.getPlatformId()));
            return info;
        }).collect(Collectors.toList());
    }

    // ==================== 统计信息 ====================

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalTopics", hotTopicRepository.count());
        stats.put("totalComments", commentRepository.count());
        stats.put("totalTasks", crawlTaskRepository.count());
        stats.put("platformCount", adapters.size());
        stats.put("platforms", adapters.keySet());

        // 各平台数据量
        Map<String, Long> platformStats = new LinkedHashMap<>();
        for (String pid : adapters.keySet()) {
            platformStats.put(pid, hotTopicRepository.countByPlatform(pid));
        }
        stats.put("platformTopicCounts", platformStats);

        return stats;
    }

    public List<CrawlTask> getRecentTasks() {
        return crawlTaskRepository.findTop10ByOrderByCreatedAtDesc();
    }

    // ==================== 工具方法 ====================

    private CrawlTask createTask(String platform, String taskType) {
        CrawlTask task = new CrawlTask();
        task.setPlatform(platform);
        task.setTaskType(taskType);
        task.setStatus("PENDING");
        return crawlTaskRepository.save(task);
    }
}
