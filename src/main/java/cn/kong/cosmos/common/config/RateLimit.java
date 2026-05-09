package cn.kong.cosmos.common.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解
 * 基于 Redis + 滑动窗口实现
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /** 时间窗口（秒） */
    int windowSeconds() default 60;
    /** 最大请求数 */
    int maxRequests() default 10;
    /** 限流 key 前缀 */
    String keyPrefix() default "";
}