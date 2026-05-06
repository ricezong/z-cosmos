package cn.kong.cosmos.auth.service;

import cn.kong.cosmos.auth.dto.resp.AuthTokenResp;
import io.jsonwebtoken.Claims;

/**
 * Token 全生命周期管理：签发双 Token、Redis 管理 RefreshToken、旋转刷新、踢人、验签解析
 * userId 统一使用业务 userId（String，雪花算法）
 */
public interface TokenService {

    /**
     * 签发双 Token
     * @param userId 业务 userId（z_users.user_id）
     * @param nickname 用户昵称
     */
    AuthTokenResp issueTokens(String userId, String nickname);

    /**
     * 刷新 Token（旋转刷新）
     */
    AuthTokenResp refreshToken(String refreshToken);

    /**
     * 验证并解析 Token
     */
    String verifyToken(String token);

    /**
     * 解析 Token 获取完整 Claims
     */
    Claims parseToken(String token);

    /**
     * 撤销 Refresh Token（登出/踢人）
     */
    void revokeToken(String refreshToken);

    /**
     * 将 Access Token 加入黑名单（登出时使其立即失效）
     */
    void blacklistAccessToken(String accessToken);
}