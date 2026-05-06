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
 * userId 统一使用业务 userId（String，雪花算法）
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
    public AuthTokenResp issueTokens(String userId, String nickname) {
        String accessToken = createAccessToken(userId, nickname);
        String refreshToken = createRefreshToken(userId, nickname);

        String redisKey = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(redisKey, userId, refreshExpiration, TimeUnit.MILLISECONDS);

        return AuthTokenResp.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userId)
                .nickname(nickname)
                .build();
    }

    @Override
    public AuthTokenResp refreshToken(String refreshToken) {
        String userId = verifyRefreshToken(refreshToken);
        if (userId == null) {
            throw new JwtException("Refresh Token 无效或已过期");
        }

        String redisKey = REFRESH_TOKEN_PREFIX + refreshToken;
        String storedUserId = redisTemplate.opsForValue().get(redisKey);
        if (storedUserId == null || !storedUserId.equals(userId)) {
            throw new JwtException("Refresh Token 已失效或被撤销");
        }

        redisTemplate.delete(redisKey);

        String nickname = getUsername(userId);
        return issueTokens(userId, nickname);
    }

    @Override
    public String verifyToken(String token) {
        if (isAccessTokenBlacklisted(token)) {
            throw new JwtException("Token 已被撤销");
        }
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (JwtException e) {
            throw new JwtException("Token 验证失败：" + e.getMessage());
        }
    }

    @Override
    public Claims parseToken(String token) {
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
        log.info("撤销 Refresh Token：userId={}", userIdStr);
    }

    @Override
    public void blacklistAccessToken(String accessToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
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

    private boolean isAccessTokenBlacklisted(String token) {
        String key = ACCESS_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private String createAccessToken(String userId, String nickname) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userId)
                .claim("nickname", nickname)
                .claim("type", "access")
                .issuedAt(new java.util.Date(now))
                .expiration(new java.util.Date(now + accessExpiration))
                .signWith(secretKey)
                .compact();
    }

    private String createRefreshToken(String userId, String nickname) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userId)
                .claim("nickname", nickname)
                .claim("type", "refresh")
                .issuedAt(new java.util.Date(now))
                .expiration(new java.util.Date(now + refreshExpiration))
                .signWith(secretKey)
                .compact();
    }

    private String verifyRefreshToken(String token) {
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
            return claims.getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    private String getUsername(String userId) {
        User user = userQueryService.findByUserId(userId);
        if (user != null && user.getNickname() != null) {
            return user.getNickname();
        }
        return "user_" + userId;
    }
}