package com.search.engine.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 网页实体类 - 对应 doc_raw.bin 的数据库版本
 * 存储爬取到的网页原始内容和提取的文本信息
 */
@Data
@Entity
@Table(name = "web_page", indexes = {
    @Index(name = "idx_url", columnList = "url", unique = true),
    @Index(name = "idx_crawled_at", columnList = "crawledAt")
})
public class WebPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 网页URL */
    @Column(nullable = false, unique = true, length = 2048)
    private String url;

    /** 网页标题 */
    @Column(length = 500)
    private String title;

    /** 网页原始HTML内容 */
    @Column(columnDefinition = "TEXT")
    private String rawContent;

    /** 提取的纯文本内容 */
    @Column(columnDefinition = "TEXT")
    private String textContent;

    /** 爬取时间 */
    @Column(name = "crawledAt")
    private LocalDateTime crawledAt;

    /** 网页状态：CRAWLED, ANALYZED, INDEXED */
    @Column(length = 20)
    private String status = "CRAWLED";

    @PrePersist
    protected void onCreate() {
        if (crawledAt == null) {
            crawledAt = LocalDateTime.now();
        }
    }
}
