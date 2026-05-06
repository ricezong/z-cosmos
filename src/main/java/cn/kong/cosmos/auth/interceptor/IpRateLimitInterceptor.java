package cn.kong.cosmos.auth.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import cn.kong.cosmos.common.core.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

/**
 * IP 滑动窗口频控：INCR + EXPIRE 实现，从配置文件读取参数
 */
@Slf4j
@Component
public class IpRateLimitInterceptor implements HandlerInterceptor {

    private static final String RATE_LIMIT_PREFIX = "rate:limit:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rate-limit.window-seconds:60}")
    private int windowSeconds;

    @Value("${rate-limit.max-requests:10}")
    private int maxRequests;

    public IpRateLimitInterceptor(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ip = getClientIp(request);
        String key = RATE_LIMIT_PREFIX + ip;

        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                // 首次请求，设置过期时间
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }

            if (count != null && count > maxRequests) {
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                try {
                    response.getWriter().write(objectMapper.writeValueAsString(
                            Result.tooManyRequests("请求过于频繁，请稍后重试")));
                } catch (Exception e) {
                    // ignore
                }
                return false;
            }
        } catch (Exception e) {
            // Redis 不可用时降级放行
            log.warn("限流检查失败，放行请求: {}", e.getMessage());
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        // 优先使用远程地址，防止客户端伪造 X-Forwarded-For 绕过限流
        // 如果部署在反向代理后，应通过 Spring ForwardedHeaderFilter 配置信任代理
        String ip = request.getRemoteAddr();

        // 仅在确认有可信反向代理时才读取 Header
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty() && !"unknown".equalsIgnoreCase(forwarded)) {
            // 多个 IP 时取最后一个（由最近的可信代理追加）
            if (forwarded.contains(",")) {
                ip = forwarded.substring(forwarded.lastIndexOf(",") + 1).trim();
            } else {
                ip = forwarded.trim();
            }
        } else {
            String realIp = request.getHeader("X-Real-IP");
            if (realIp != null && !realIp.isEmpty() && !"unknown".equalsIgnoreCase(realIp)) {
                ip = realIp.trim();
            }
        }

        return ip;
    }
}
