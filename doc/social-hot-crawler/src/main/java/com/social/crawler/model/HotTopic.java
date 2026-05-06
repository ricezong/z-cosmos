package com.social.crawler.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 热点话题实体
 *
 * 统一的数据模型，适配所有平台的热点信息。
 * 每个平台的热点（热搜、热门视频、热门笔记等）都映射到此实体。
 */
@Data
@Entity
@Table(name = "hot_topic", indexes = {
    @Index(name = "idx_platform", columnList = "platform"),
    @Index(name = "idx_platform_rank", columnList = "platform, rank"),
    @Index(name = "idx_crawled_at", columnList = "crawledAt"),
    @Index(name = "idx_platform_topicid", columnList = "platform, topicId", unique = true)
})
public class HotTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属平台标识（douyin, xiaohongshu, zhihu, bilibili, weibo等） */
    @Column(nullable = false, length = 30)
    private String platform;

    /** 平台上的原始话题ID */
    @Column(nullable = false, length = 200)
    private String topicId;

    /** 话题标题 */
    @Column(nullable = false, length = 500)
    private String title;

    /** 话题摘要/描述 */
    @Column(columnDefinition = "TEXT")
    private String summary;

    /** 话题详情URL */
    @Column(length = 2048)
    private String url;

    /** 封面图URL */
    @Column(length = 2048)
    private String coverUrl;

    /** 热度值（各平台计算方式不同，统一存储为数值） */
    private Long heatValue = 0L;

    /** 热度标签（如"热"、"新"、"爆"、"沸"等） */
    @Column(length = 20)
    private String heatLabel;

    /** 在平台榜单中的排名 */
    private Integer rank;

    /** 标签/分类（JSON数组格式，如["科技","AI"]） */
    @Column(columnDefinition = "TEXT")
    private String tags;

    /** 作者/发布者 */
    @Column(length = 200)
    private String author;

    /** 评论数 */
    private Long commentCount = 0L;

    /** 点赞数 */
    private Long likeCount = 0L;

    /** 转发/分享数 */
    private Long shareCount = 0L;

    /** 浏览/播放量 */
    private Long viewCount = 0L;

    /** 是否已爬取评论 */
    @Column(nullable = false)
    private Boolean commentsFetched = false;

    /** 爬取时间 */
    @Column(name = "crawledAt")
    private LocalDateTime crawledAt;

    /** 话题在平台上的发布时间 */
    @Column(name = "publishedAt")
    private LocalDateTime publishedAt;

    /** 原始数据（JSON格式，保留平台特有字段） */
    @Column(columnDefinition = "TEXT")
    private String rawData;

    @PrePersist
    protected void onCreate() {
        if (crawledAt == null) {
            crawledAt = LocalDateTime.now();
        }
    }
}
