package com.social.crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 社交媒体热点爬虫 - 主启动类
 *
 * 可扩展的多平台热点与评论采集系统。
 * 新增平台只需实现 PlatformAdapter 接口并注册为 Spring Bean。
 */
@SpringBootApplication
public class SocialHotCrawlerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SocialHotCrawlerApplication.class, args);
    }
}
