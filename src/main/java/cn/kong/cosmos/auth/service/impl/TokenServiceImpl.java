package cn.kong.cosmos.auth.service.impl;

import cn.kong.cosmos.auth.dto.resp.AuthTokenResp;
import cn.kong.cosmos.auth.entity.User;
import cn.kong.cosmos.auth.service.TokenService;
import cn.kong.cosmos.auth.service.UserQueryService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Token 全生命周期管理实现
 * - 双 Token 架构：Access Token（2 小时）+ Refresh Token（30 天）
 * - Redis 管理 Refresh Token，支持踢人、过期自动淘汰
 * - Access Token 黑名单（登出后立即失效）
 * - Token 旋转刷新机制
 */
@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    private final StringRedisTemplate redisTemplate;
    private final UserQueryService userQueryService;
    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";
    private static final String ACCESS_BLACKLIST_PREFIX = "auth:blacklist:";

    public TokenServiceImpl(
            StringRedisTemplate redisTemplate,
            UserQueryService userQueryService,
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.access-expiration:7200000}") long accessExpiration,
            @Value("${jwt.refresh-expiration:2592000000}") long refreshExpiration) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("JWT 密钥未配置！请设置环境变量 JWT_SECRET 或配置 jwt.secret");
        }
        if (jwtSecret.length() < 32) {
            throw new IllegalStateException("JWT 密钥长度不足！至少需要 32 字符，当前：" + jwtSecret.length());
        }
        this.redisTemplate = redisTemplate;
        this.userQueryService = userQueryService;
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    @Override
    public AuthTokenResp issueTokens(Long userId, String nickname) {
        // 签发 Access Token
        String accessToken = createAccessToken(userId, nickname);
        // 签发 Refresh Token
        String refreshToken = createRefreshToken(userId, nickname);

        // 将 Refresh Token 存入 Redis
        String redisKey = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(redisKey, userId.toString(), refreshExpiration, TimeUnit.MILLISECONDS);

        return new AuthTokenResp(accessToken, refreshToken, userId, nickname);
    }

    @Override
    public AuthTokenResp refreshToken(String refreshToken) {
        // 验证 Refresh Token
        Long userId = verifyRefreshToken(refreshToken);
        if (userId == null) {
            throw new JwtException("Refresh Token 无效或已过期");
        }

        // 检查 Redis 中是否存在（防重放/踢人）
        String redisKey = REFRESH_TOKEN_PREFIX + refreshToken;
        String storedUserId = redisTemplate.opsForValue().get(redisKey);
        if (storedUserId == null || !storedUserId.equals(userId.toString())) {
            throw new JwtException("Refresh Token 已失效或被撤销");
        }

        // 删除旧 Refresh Token（旋转刷新）
        redisTemplate.delete(redisKey);

        // 从 JWT Claims 中获取显示名称，不存在则从数据库查询
        String nickname = getUsername(userId);

        // 签发新双 Token
        return issueTokens(userId, nickname);
    }

    @Override
    public Long verifyToken(String token) {
        // 先检查黑名单
        if (isAccessTokenBlacklisted(token)) {
            throw new JwtException("Token 已被撤销");
        }
        try {
            Claims claims = parseToken(token);
            return claims.get("userId", Long.class);
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (JwtException e) {
            throw new JwtException("Token 验证失败：" + e.getMessage());
        }
    }

    @Override
    public Claims parseToken(String token) {
        // 先检查黑名单
        if (isAccessTokenBlacklisted(token)) {
            throw new JwtException("Token 已被撤销");
        }
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public void revokeToken(String refreshToken) {
        String redisKey = REFRESH_TOKEN_PREFIX + refreshToken;
        String userIdStr = redisTemplate.opsForValue().get(redisKey);
        redisTemplate.delete(redisKey);

        // 将关联的 Access Token 加入黑名单
        // 由于 Access Token 本身不存储在 Redis 中，这里通过删除 Refresh Token 实现
        // 如果需要立即失效 Access Token，需要前端配合传递 Access Token
        log.info("撤销 Refresh Token：userId={}", userIdStr);
    }

    /**
     * 将 Access Token 加入黑名单（登出时调用，使 Access Token 立即失效）
     */
    public void blacklistAccessToken(String accessToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();

            // 计算剩余有效期
            long remainingMs = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (remainingMs > 0) {
                String key = ACCESS_BLACKLIST_PREFIX + accessToken;
                redisTemplate.opsForValue().set(key, "1", remainingMs, TimeUnit.MILLISECONDS);
                log.info("Access Token 已加入黑名单，剩余有效期：{}ms", remainingMs);
            }
        } catch (ExpiredJwtException e) {
            // Token 已过期，无需加入黑名单
        } catch (JwtException e) {
            log.warn("加入黑名单失败，Token 无效：{}", e.getMessage());
        }
    }

    /**
     * 检查 Access Token 是否在黑名单中
     */
    private boolean isAccessTokenBlacklisted(String token) {
        String key = ACCESS_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 创建 Access Token
     */
    private String createAccessToken(Long userId, String nickname) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("userId", userId)
                .claim("nickname", nickname)
                .claim("type", "access")
                .issuedAt(new java.util.Date(now))
                .expiration(new java.util.Date(now + accessExpiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 创建 Refresh Token
     */
    private String createRefreshToken(Long userId, String nickname) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("userId", userId)
                .claim("nickname", nickname)
                .claim("type", "refresh")
                .issuedAt(new java.util.Date(now))
                .expiration(new java.util.Date(now + refreshExpiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 验证 Refresh Token
     */
    private Long verifyRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String type = claims.get("type", String.class);
            if (!"refresh".equals(type)) {
                throw new JwtException("不是 Refresh Token");
            }

            return claims.get("userId", Long.class);
        } catch (ExpiredJwtException e) {
            return null;
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * 根据 userId 获取显示名称（昵称）
     */
    private String getUsername(Long userId) {
        User user = userQueryService.findById(userId);
        if (user != null && user.getNickname() != null) {
            return user.getNickname();
        }
        return "user_" + userId;
    }
}
