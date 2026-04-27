package cn.kong.cosmos.auth.controller;

import cn.kong.cosmos.auth.dto.req.AccountLoginReq;
import cn.kong.cosmos.auth.dto.req.RefreshReq;
import cn.kong.cosmos.auth.dto.resp.AuthTokenResp;
import cn.kong.cosmos.auth.service.AuthDispatcherService;
import cn.kong.cosmos.auth.service.TokenService;
import cn.kong.cosmos.auth.util.CookieTokenHelper;
import cn.kong.cosmos.common.core.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 统一认证入口：账密登录/注册/Token 刷新/登出
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
        cookieTokenHelper.setTokenCookie(response, resp.getAccessToken());
        return Result.success(resp);
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/refresh")
    public Result<AuthTokenResp> refresh(@Valid @RequestBody RefreshReq req,
                                          HttpServletResponse response) {
        AuthTokenResp resp = tokenService.refreshToken(req.getRefreshToken());
        cookieTokenHelper.setTokenCookie(response, resp.getAccessToken());
        return Result.success(resp);
    }

    /**
     * 登出：撤销 Refresh Token + 将 Access Token 加入黑名单 + 清除 Cookie
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                               @RequestBody(required = false) RefreshReq req,
                               HttpServletResponse response) {
        // 将 Access Token 加入黑名单（立即使其失效）
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            tokenService.blacklistAccessToken(accessToken);
        }

        // 撤销 Refresh Token
        if (req != null && req.getRefreshToken() != null) {
            tokenService.revokeToken(req.getRefreshToken());
        }

        cookieTokenHelper.removeTokenCookie(response);
        return Result.success();
    }
}
