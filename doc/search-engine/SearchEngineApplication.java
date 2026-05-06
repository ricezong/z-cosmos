package com.search.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 搜索引擎主启动类
 * 
 * 本项目实现了一个小型搜索引擎，包含以下核心模块：
 * - 搜集（Crawling）：BFS爬取网页，布隆过滤器去重
 * - 分析（Analysis）：HTML解析，文本提取，分词
 * - 索引（Indexing）：倒排索引构建，合并排序
 * - 查询（Query）：关键词搜索，结果排序
 */
@SpringBootApplication
public class SearchEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchEngineApplication.class, args);
    }
}
