package com.search.engine.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 倒排索引实体类 - 对应 inverted_index.bin 的数据库版本
 * 记录每个词语在哪些文档中出现，以及出现的位置和频率
 */
@Data
@Entity
@Table(name = "inverted_index", indexes = {
    @Index(name = "idx_term_page", columnList = "termId, pageId", unique = true),
    @Index(name = "idx_page_id", columnList = "pageId")
})
public class InvertedIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 词语ID */
    @Column(name = "termId", nullable = false)
    private Long termId;

    /** 网页ID */
    @Column(name = "pageId", nullable = false)
    private Long pageId;

    /** 该词语在此文档中出现的次数（词频） */
    private int termFrequency = 0;

    /** 词语在文档中的位置列表（JSON格式存储） */
    @Column(columnDefinition = "TEXT")
    private String positions;
}
