package com.search.engine.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 词语实体类 - 对应单词编号(term_id)的数据库版本
 * 存储分词后的唯一词语及其编号
 */
@Data
@Entity
@Table(name = "term", indexes = {
    @Index(name = "idx_word", columnList = "word", unique = true)
})
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 词语文本 */
    @Column(nullable = false, unique = true, length = 200)
    private String word;

    /** 该词语在所有文档中出现的文档数（文档频率） */
    private int documentFrequency = 0;

    /** 该词语在所有文档中出现的总次数 */
    private long totalFrequency = 0;
}
