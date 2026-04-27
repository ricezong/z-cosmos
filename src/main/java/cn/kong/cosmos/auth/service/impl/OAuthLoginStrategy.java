package cn.kong.cosmos.auth.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import cn.kong.cosmos.auth.config.OAuthProperties;
import cn.kong.cosmos.auth.config.OAuthProvider;
import cn.kong.cosmos.auth.dto.resp.AuthTokenResp;
import cn.kong.cosmos.auth.entity.User;
import cn.kong.cosmos.auth.service.LoginStrategy;
import cn.kong.cosmos.auth.service.TokenService;
import cn.kong.cosmos.auth.service.UserQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth 登录策略：校验 State → 换 Token → 拉取用户 → 委托 biz 层创建
 * 通过 OAuthProperties 读取配置，不再使用 System.getenv()
 */
@Slf4j
@Service
public class OAuthLoginStrategy implements LoginStrategy<Map<String, String>> {

    private final TokenService tokenService;
    private final UserQueryService userQueryService;
    private final OAuthProperties oAuthProperties;

    public OAuthLoginStrategy(TokenService tokenService, UserQueryService userQueryService,
                              OAuthProperties oAuthProperties) {
        this.tokenService = tokenService;
        this.userQueryService = userQueryService;
        this.oAuthProperties = oAuthProperties;
    }

    @Override
    public String type() {
        return "OAUTH";
    }

    @Override
    public Class<Map<String, String>> paramType() {
        @SuppressWarnings("unchecked")
        Class<Map<String, String>> clazz = (Class<Map<String, String>>) (Class<?>) Map.class;
        return clazz;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AuthTokenResp executeLogin(Map<String, String> params) {
        String providerId = params.get("provider");
        String code = params.get("code");

        if (providerId == null || code == null) {
            throw new IllegalArgumentException("缺少 provider 或 code 参数");
        }

        OAuthProvider provider = OAuthProvider.fromId(providerId);

        // 1. 使用 code 换取 access_token
        String accessToken = getOAuthAccessToken(provider, code);

        // 2. 拉取第三方用户信息
        Map<String, Object> userInfo = getOAuthUserInfo(provider, accessToken);

        // 3. 提取唯一标识（OpenID）
        String openId = (String) userInfo.get("openId");
        if (openId == null) {
            throw new RuntimeException("获取用户 OpenID 失败");
        }

        // 4. 创建或关联本地用户
        User user = userQueryService.findOrCreateByOAuth(providerId, openId,
            (String) userInfo.get("nickname"),
            (String) userInfo.get("avatar"));

        // 5. 检查用户状态
        if (user.getUserStatus() != null && user.getUserStatus() != 1) {
            throw new IllegalArgumentException("账号已被禁用或封禁");
        }

        // 6. 签发双 Token
        return tokenService.issueTokens(user.getId(), user.getNickname());
    }

    private String getOAuthAccessToken(OAuthProvider provider, String code) {
        OAuthProperties.Provider config = oAuthProperties.getProvider(provider.getId());

        switch (provider) {
            case GITHUB:
                return getGithubAccessToken(config, code);
            case GITEE:
                return getGiteeAccessToken(config, code);
            case QQ:
                return getQQAccessToken(config, code);
            default:
                throw new IllegalArgumentException("不支持的 OAuth 提供商：" + provider.getId());
        }
    }

    private String getGithubAccessToken(OAuthProperties.Provider config, String code) {
        Map<String, Object> data = new HashMap<>();
        data.put("client_id", config.getClientId());
        data.put("client_secret", config.getClientSecret());
        data.put("code", code);
        data.put("redirect_uri", config.getRedirectUri());

        String response = HttpUtil.post(OAuthProvider.GITHUB.getTokenUrl(), data);
        JSONObject result = parseFormResponse(response);
        String accessToken = result.getString("access_token");

        if (accessToken == null) {
            log.error("GitHub Access Token 获取失败：{}", response);
            throw new RuntimeException("GitHub Access Token 获取失败");
        }

        log.info("GitHub AccessToken 获取成功");
        return accessToken;
    }

    private String getGiteeAccessToken(OAuthProperties.Provider config, String code) {
        Map<String, Object> data = new HashMap<>();
        data.put("grant_type", "authorization_code");
        data.put("client_id", config.getClientId());
        data.put("client_secret", config.getClientSecret());
        data.put("code", code);
        data.put("redirect_uri", config.getRedirectUri());

        String response = HttpUtil.post(OAuthProvider.GITEE.getTokenUrl(), data);
        JSONObject result = JSON.parseObject(response);
        String accessToken = result.getString("access_token");

        if (accessToken == null) {
            log.error("Gitee Access Token 获取失败：{}", response);
            throw new RuntimeException("Gitee Access Token 获取失败");
        }

        log.info("Gitee AccessToken 获取成功");
        return accessToken;
    }

    private String getQQAccessToken(OAuthProperties.Provider config, String code) {
        String queryString = "grant_type=authorization_code"
                + "&client_id=" + URLEncoder.encode(config.getClientId(), StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(config.getClientSecret(), StandardCharsets.UTF_8)
                + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(config.getRedirectUri(), StandardCharsets.UTF_8);

        String response = HttpUtil.get(OAuthProvider.QQ.getTokenUrl() + "?" + queryString);
        JSONObject result = parseFormResponse(response);
        String accessToken = result.getString("access_token");

        if (accessToken == null) {
            log.error("QQ Access Token 获取失败：{}", response);
            throw new RuntimeException("QQ Access Token 获取失败");
        }

        log.info("QQ AccessToken 获取成功");
        return accessToken;
    }

    private Map<String, Object> getOAuthUserInfo(OAuthProvider provider, String accessToken) {
        switch (provider) {
            case GITHUB:
                return getGithubUserInfo(accessToken);
            case GITEE:
                return getGiteeUserInfo(accessToken);
            case QQ:
                return getQQUserInfo(accessToken);
            default:
                throw new IllegalArgumentException("不支持的 OAuth 提供商：" + provider.getId());
        }
    }

    private Map<String, Object> getGithubUserInfo(String accessToken) {
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", accessToken);

        String response = HttpUtil.get(OAuthProvider.GITHUB.getUserInfoUrl(), params);
        JSONObject json = JSON.parseObject(response);

        Map<String, Object> result = new HashMap<>();
        result.put("openId", String.valueOf(json.get("id")));
        result.put("nickname", json.getString("login"));
        result.put("avatar", json.getString("avatar_url"));
        return result;
    }

    private Map<String, Object> getGiteeUserInfo(String accessToken) {
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", accessToken);

        String response = HttpUtil.get(OAuthProvider.GITEE.getUserInfoUrl(), params);
        JSONObject json = JSON.parseObject(response);

        Map<String, Object> result = new HashMap<>();
        result.put("openId", String.valueOf(json.get("id")));
        result.put("nickname", json.getString("login"));
        result.put("avatar", json.getString("avatar_url"));
        return result;
    }

    private Map<String, Object> getQQUserInfo(String accessToken) {
        // QQ 需要先获取 openid
        String meUrl = "https://graph.qq.com/oauth2.0/me?access_token=" + accessToken;
        String meResponse = HttpUtil.get(meUrl);
        String jsonStr = meResponse.substring(meResponse.indexOf("{"), meResponse.lastIndexOf("}") + 1);
        JSONObject meJson = JSON.parseObject(jsonStr);
        String openId = meJson.getString("openid");

        // 获取用户信息
        OAuthProperties.Provider config = oAuthProperties.getProvider("qq");
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", accessToken);
        params.put("oauth_consumer_key", config.getClientId());
        params.put("openid", openId);

        String infoResponse = HttpUtil.get(OAuthProvider.QQ.getUserInfoUrl(), params);
        JSONObject infoJson = JSON.parseObject(infoResponse);

        Map<String, Object> result = new HashMap<>();
        result.put("openId", openId);
        result.put("nickname", infoJson.getString("nickname"));
        result.put("avatar", infoJson.getString("figureurl_qq_2"));
        return result;
    }

    private JSONObject parseFormResponse(String response) {
        Map<String, String> map = HttpUtil.decodeParamMap(response, StandardCharsets.UTF_8);
        return JSON.parseObject(JSON.toJSONString(map));
    }
}
