package com.social.crawler.adapter;

import com.social.crawler.model.HotTopic;
import com.social.crawler.model.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * 平台适配器抽象基类
 *
 * 提供通用的HTTP请求能力、数据转换、错误处理等基础设施，
 * 子类只需关注平台特有的数据解析逻辑。
 *
 * 设计思路：
 * - 模板方法模式：定义爬取流程骨架，子类实现具体解析
 * - 通用HTTP客户端：带超时、重试、UA伪装
 * - 统一异常处理：网络错误、解析错误、限流等
 */
public abstract class AbstractPlatformAdapter implements PlatformAdapter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** HTTP客户端（带超时设置） */
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    /** 请求超时时间（秒） */
    protected int requestTimeout = 15;

    /** 重试次数 */
    protected int maxRetries = 2;

    /** 请求间隔（毫秒），避免被限流 */
    protected long requestInterval = 1000;

    /** 上次请求时间 */
    private long lastRequestTime = 0;

    // ==================== 通用HTTP请求 ====================

    /**
     * 发送GET请求
     */
    protected String httpGet(String url) {
        rateLimit();
        for (int i = 0; i <= maxRetries; i++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", getRandomUserAgent())
                    .header("Accept", "application/json, text/html, */*")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .timeout(Duration.ofSeconds(requestTimeout))
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return response.body();
                } else if (response.statusCode() == 429) {
                    logger.warn("[{}] 触发限流，等待后重试 (第{}次)", getPlatformName(), i + 1);
                    Thread.sleep(3000 * (i + 1));
                } else {
                    logger.warn("[{}] HTTP {} : {}", getPlatformName(), response.statusCode(), url);
                    return null;
                }
            } catch (Exception e) {
                logger.error("[{}] 请求失败 (第{}次): {}", getPlatformName(), i + 1, e.getMessage());
                if (i == maxRetries) return null;
                try { Thread.sleep(1000 * (i + 1)); } catch (InterruptedException ignored) {}
            }
        }
        return null;
    }

    /**
     * 发送带自定义Headers的GET请求
     */
    protected String httpGet(String url, Map<String, String> headers) {
        rateLimit();
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", getRandomUserAgent())
                .timeout(Duration.ofSeconds(requestTimeout))
                .GET();

            headers.forEach(builder::header);

            HttpResponse<String> response = httpClient.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 ? response.body() : null;
        } catch (Exception e) {
            logger.error("[{}] 请求失败: {}", getPlatformName(), e.getMessage());
            return null;
        }
    }

    // ==================== 限流控制 ====================

    /**
     * 请求限流 - 确保两次请求之间有最小间隔
     */
    protected void rateLimit() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestTime;
        if (elapsed < requestInterval) {
            try {
                Thread.sleep(requestInterval - elapsed);
            } catch (InterruptedException ignored) {}
        }
        lastRequestTime = System.currentTimeMillis();
    }

    // ==================== User-Agent池 ====================

    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:125.0) Gecko/20100101 Firefox/125.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Safari/605.1.15",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    };

    protected String getRandomUserAgent() {
        return USER_AGENTS[new Random().nextInt(USER_AGENTS.length)];
    }

    // ==================== 数据清洗工具 ====================

    /**
     * 清理HTML标签
     */
    protected String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]+>", "").trim();
    }

    /**
     * 截断文本
     */
    protected String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }

    /**
     * 安全解析整数
     */
    protected int safeParseInt(String value) {
        try {
            return Integer.parseInt(value.replaceAll("[^0-9-]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 安全解析长整数
     */
    protected long safeParseLong(String value) {
        try {
            return Long.parseLong(value.replaceAll("[^0-9-]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}
