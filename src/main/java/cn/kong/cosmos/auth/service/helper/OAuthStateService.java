package cn.kong.cosmos.auth.service.helper;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * OAuth State 防 CSRF 管理：生成缓存、一次性消费（getAndDelete）
 */
@Service
public class OAuthStateService {

    private static final String STATE_PREFIX = "oauth:state:";
    private static final long STATE_TTL_MINUTES = 10;

    private final RedisTemplate<String, Object> redisTemplate;

    public OAuthStateService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成并缓存 State
     */
    public String generateState(String provider) {
        String state = UUID.randomUUID().toString().replace("-", "");
        String key = STATE_PREFIX + provider + ":" + state;
        redisTemplate.opsForValue().set(key, "1", STATE_TTL_MINUTES, TimeUnit.MINUTES);
        return state;
    }

    /**
     * 验证并删除 State（一次性消费）
     */
    public boolean validateAndDelete(String provider, String state) {
        String key = STATE_PREFIX + provider + ":" + state;
        Boolean deleted = redisTemplate.delete(key);
        return Boolean.TRUE.equals(deleted);
    }
}
