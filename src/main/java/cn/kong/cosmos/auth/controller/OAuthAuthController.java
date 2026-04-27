package cn.kong.cosmos.auth.controller;

import cn.kong.cosmos.auth.config.OAuthProperties;
import cn.kong.cosmos.auth.config.OAuthProvider;
import cn.kong.cosmos.auth.dto.req.OAuthUrlReq;
import cn.kong.cosmos.auth.dto.resp.AuthTokenResp;
import cn.kong.cosmos.auth.service.AuthDispatcherService;
import cn.kong.cosmos.auth.service.helper.OAuthStateService;
import cn.kong.cosmos.auth.util.CookieTokenHelper;
import cn.kong.cosmos.common.core.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 第三方 OAuth 认证控制器：GitHub / Gitee / QQ 授权码模式
 */
@RestController
@RequestMapping("/api/auth/oauth")
public class OAuthAuthController {

    private final OAuthStateService oauthStateService;
    private final AuthDispatcherService authDispatcherService;
    private final CookieTokenHelper cookieTokenHelper;
    private final OAuthProperties oAuthProperties;

    public OAuthAuthController(OAuthStateService oauthStateService,
                               AuthDispatcherService authDispatcherService,
                               CookieTokenHelper cookieTokenHelper,
                               OAuthProperties oAuthProperties) {
        this.oauthStateService = oauthStateService;
        this.authDispatcherService = authDispatcherService;
        this.cookieTokenHelper = cookieTokenHelper;
        this.oAuthProperties = oAuthProperties;
    }

    /**
     * 获取 OAuth 授权链接
     */
    @PostMapping("/url")
    public Result<Map<String, String>> getOAuthUrl(@Valid @RequestBody OAuthUrlReq req) {
        String providerId = req.getProvider().toLowerCase();
        OAuthProvider provider = OAuthProvider.fromId(providerId);
        OAuthProperties.Provider config = oAuthProperties.getProvider(providerId);

        // 生成一次性 State 防 CSRF
        String state = oauthStateService.generateState(providerId);

        // 统一构建授权链接
        String authUrl = String.format(
                "%s?client_id=%s&redirect_uri=%s&state=%s&response_type=code&scope=%s",
                provider.getAuthUrl(),
                URLEncoder.encode(config.getClientId(), StandardCharsets.UTF_8),
                URLEncoder.encode(config.getRedirectUri(), StandardCharsets.UTF_8),
                state,
                provider.getScope()
        );

        Map<String, String> result = new HashMap<>();
        result.put("authUrl", authUrl);
        result.put("state", state);

        return Result.success(result);
    }

    /**
     * OAuth 回调处理
     */
    @GetMapping("/callback")
    public void handleOAuthCallback(@RequestParam String provider,
                                    @RequestParam String code,
                                    @RequestParam String state,
                                    HttpServletResponse response) throws Exception {
        // 验证 State 并一次性消费（防 CSRF）
        if (!oauthStateService.validateAndDelete(provider.toLowerCase(), state)) {
            response.sendRedirect("/login?error=invalid_state");
            return;
        }

        try {
            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("provider", provider.toLowerCase());
            params.put("code", code);

            // 执行 OAuth 登录策略
            AuthTokenResp resp = authDispatcherService.dispatch("OAUTH", params);

            // 签发 Token Cookie
            cookieTokenHelper.setTokenCookie(response, resp.getAccessToken());

            // 重定向到前端回调页面，携带必要信息（不包含敏感 token）
            response.sendRedirect("/auth/callback?success=true&userId=" + resp.getUserId());

        } catch (Exception e) {
            response.sendRedirect("/login?error=oauth_failed&msg=" +
                URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }
}
