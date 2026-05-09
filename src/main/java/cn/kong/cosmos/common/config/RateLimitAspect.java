package cn.kong.cosmos.common.config;

import cn.kong.cosmos.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * 限流切面 - 基于 Redis 计数器的滑动窗口限流
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String clientIp = getClientIp(request);
        String methodName = joinPoint.getSignature().toShortString();
        
        // 构建限流 key
        String keyPrefix = rateLimit.keyPrefix().isEmpty() ? methodName : rateLimit.keyPrefix();
        String redisKey = "rate_limit:" + keyPrefix + ":" + clientIp;
        
        // 获取当前计数
        Long count = redisTemplate.opsForValue().increment(redisKey);
        
        if (count != null && count == 1) {
            // 第一次请求，设置过期时间
            redisTemplate.expire(redisKey, rateLimit.windowSeconds(), TimeUnit.SECONDS);
        }
        
        if (count != null && count > rateLimit.maxRequests()) {
            log.warn("接口限流触发: ip={}, key={}, count={}", clientIp, redisKey, count);
            throw new BusinessException("请求过于频繁，请稍后再试");
        }
        
        return joinPoint.proceed();
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}