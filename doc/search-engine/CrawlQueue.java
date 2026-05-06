package com.search.engine.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 爬取队列实体类 - 对应 links.bin 的数据库版本
 * 管理待爬取的URL队列（BFS队列）
 */
@Data
@Entity
@Table(name = "crawl_queue", indexes = {
    @Index(name = "idx_queue_url", columnList = "url", unique = true),
    @Index(name = "idx_queue_status", columnList = "status")
})
public class CrawlQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 待爬取的URL */
    @Column(nullable = false, unique = true, length = 2048)
    private String url;

    /** 队列状态：PENDING, CRAWLING, DONE, FAILED */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    /** 优先级（数值越小优先级越高） */
    private int priority = 0;

    /** 创建时间 */
    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    /** 错误信息 */
    @Column(length = 1000)
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
