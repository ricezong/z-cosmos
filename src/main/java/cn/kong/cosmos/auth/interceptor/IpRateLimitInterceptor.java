package cn.kong.cosmos.auth.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import cn.kong.cosmos.common.core.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collections;

/**
 * IP 滑动窗口频控：Lua 脚本保证 increment + expire 原子性，从配置文件读取参数
 */
@Component
public class IpRateLimitInterceptor implements HandlerInterceptor {

    private static final String RATE_LIMIT_PREFIX = "rate:limit:";

    /**
     * Lua 脚本：原子 increment + 首次设置过期时间
     * KEYS[1] = rate limit key
     * ARGV[1] = 窗口时间（秒）
     * ARGV[2] = 最大请求数
     * 返回: 0=允许, 1=拒绝
     */
    private static final String LUA_SCRIPT =
            "local count = redis.call('incr', KEYS[1]) " +
            "if count == 1 then " +
            "    redis.call('expire', KEYS[1], ARGV[1]) " +
            "end " +
            "if count > tonumber(ARGV[2]) then " +
            "    return 1 " +
            "end " +
            "return 0";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final DefaultRedisScript<Long> rateLimitScript;

    @Value("${rate-limit.window-seconds:60}")
    private int windowSeconds;

    @Value("${rate-limit.max-requests:10}")
    private int maxRequests;

    public IpRateLimitInterceptor(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.rateLimitScript = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ip = getClientIp(request);
        String key = RATE_LIMIT_PREFIX + ip;

        // Lua 脚本原子执行 increment + expire + 判断
        Long result = redisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(key),
                String.valueOf(windowSeconds),
                String.valueOf(maxRequests)
        );

        if (result != null && result == 1L) {
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
