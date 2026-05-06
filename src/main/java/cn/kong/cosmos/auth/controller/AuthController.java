package cn.kong.cosmos.auth.controller;

import cn.kong.cosmos.auth.dto.req.AccountLoginReq;
import cn.kong.cosmos.auth.dto.resp.AuthTokenResp;
import cn.kong.cosmos.auth.service.AuthDispatcherService;
import cn.kong.cosmos.auth.service.TokenService;
import cn.kong.cosmos.auth.util.CookieTokenHelper;
import cn.kong.cosmos.common.core.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 统一认证入口：登录/Token 刷新/登出
 *
 * Token 策略：
 * - Access Token: 响应体返回，前端存内存，通过 Authorization 头携带
 * - Refresh Token: HttpOnly Cookie，浏览器自动携带，JS 不可读取
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthDispatcherService authDispatcherService;
    private final TokenService tokenService;
    private final CookieTokenHelper cookieTokenHelper;

    public AuthController(AuthDispatcherService authDispatcherService,
                          TokenService tokenService,
                          CookieTokenHelper cookieTokenHelper) {
        this.authDispatcherService = authDispatcherService;
        this.tokenService = tokenService;
        this.cookieTokenHelper = cookieTokenHelper;
    }

    /**
     * 账密登录
     */
    @PostMapping("/login")
    public Result<AuthTokenResp> login(@Valid @RequestBody AccountLoginReq req,
                                        HttpServletResponse response) {
        AuthTokenResp resp = authDispatcherService.dispatch("PASSWORD", req);
        // Refresh Token 写入 HttpOnly Cookie
        cookieTokenHelper.setRefreshTokenCookie(response, resp.getRefreshToken());
        // 响应体仅返回 Access Token（refreshToken 字段已移除）
        return Result.success(resp);
    }

    /**
     * 刷新 Token：从 Cookie 读取 Refresh Token，签发新的双 Token
     */
    @PostMapping("/refresh")
    public Result<AuthTokenResp> refresh(HttpServletRequest request,
                                          HttpServletResponse response) {
        String refreshToken = cookieTokenHelper.getRefreshTokenFromCookie(request);
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Result.unauthorized("Refresh Token 不存在，请重新登录");
        }

        AuthTokenResp resp = tokenService.refreshToken(refreshToken);
        // 旋转刷新：写入新的 Refresh Token Cookie
        cookieTokenHelper.setRefreshTokenCookie(response, resp.getRefreshToken());
        return Result.success(resp);
    }

    /**
     * 登出：撤销 Refresh Token + 将 Access Token 加入黑名单 + 清除 Cookie
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        // 将 Access Token 加入黑名单（立即使其失效）
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            tokenService.blacklistAccessToken(accessToken);
        }

        // 从 Cookie 读取并撤销 Refresh Token
        String refreshToken = cookieTokenHelper.getRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            tokenService.revokeToken(refreshToken);
        }

        // 清除 Refresh Token Cookie
        cookieTokenHelper.removeRefreshTokenCookie(response);
        return Result.success();
    }
}
