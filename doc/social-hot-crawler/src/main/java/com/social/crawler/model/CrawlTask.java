package com.social.crawler.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 爬取任务实体
 *
 * 记录每次爬取任务的执行情况，支持定时调度和手动触发。
 */
@Data
@Entity
@Table(name = "crawl_task", indexes = {
    @Index(name = "idx_task_platform", columnList = "platform"),
    @Index(name = "idx_task_type", columnList = "taskType"),
    @Index(name = "idx_task_status", columnList = "status")
})
public class CrawlTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 目标平台 */
    @Column(nullable = false, length = 30)
    private String platform;

    /** 任务类型：HOT_TOPICS, COMMENTS */
    @Column(nullable = false, length = 20)
    private String taskType;

    /** 关联的话题ID（仅COMMENTS类型） */
    private Long topicId;

    /** 关联的话题原始ID */
    @Column(length = 200)
    private String topicOriginalId;

    /** 任务状态：PENDING, RUNNING, SUCCESS, FAILED */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    /** 爬取到的数据条数 */
    private Integer fetchedCount = 0;

    /** 错误信息 */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /** 执行耗时（毫秒） */
    private Long duration;

    /** 创建时间 */
    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    /** 完成时间 */
    @Column(name = "finishedAt")
    private LocalDateTime finishedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
