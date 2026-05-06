package com.social.crawler.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论实体
 *
 * 支持嵌套回复（楼层结构）。
 * 所有平台的评论统一映射到此实体。
 */
@Data
@Entity
@Table(name = "comment", indexes = {
    @Index(name = "idx_comment_topic", columnList = "topicId"),
    @Index(name = "idx_comment_platform", columnList = "platform"),
    @Index(name = "idx_comment_parent", columnList = "parentId"),
    @Index(name = "idx_comment_plat_cid", columnList = "platform, commentId", unique = true)
})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属平台 */
    @Column(nullable = false, length = 30)
    private String platform;

    /** 平台上的原始评论ID */
    @Column(nullable = false, length = 200)
    private String commentId;

    /** 关联的话题ID（外键指向hot_topic.id） */
    @Column(nullable = false)
    private Long topicId;

    /** 父评论ID（null表示顶级评论，非null表示回复） */
    private Long parentId;

    /** 回复的目标评论ID（被回复者） */
    private Long replyToId;

    /** 评论内容 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 评论者用户名 */
    @Column(length = 200)
    private String username;

    /** 评论者头像URL */
    @Column(length = 2048)
    private String userAvatar;

    /** 评论者主页URL */
    @Column(length = 2048)
    private String userProfileUrl;

    /** 点赞数 */
    private Long likeCount = 0L;

    /** 回复数 */
    private Long replyCount = 0L;

    /** 评论在平台上的发布时间 */
    @Column(name = "publishedAt")
    private LocalDateTime publishedAt;

    /** 楼层/排序序号 */
    private Integer floor;

    /** 评论的情感倾向（positive/neutral/negative，可选） */
    @Column(length = 20)
    private String sentiment;

    /** 原始数据（JSON格式） */
    @Column(columnDefinition = "TEXT")
    private String rawData;

    /** 爬取时间 */
    @Column(name = "crawledAt")
    private LocalDateTime crawledAt;

    @PrePersist
    protected void onCreate() {
        if (crawledAt == null) {
            crawledAt = LocalDateTime.now();
        }
    }
}
