package cn.kong.cosmos.auth.service.impl;

import cn.kong.cosmos.auth.dto.resp.AuthTokenResp;
import cn.kong.cosmos.auth.entity.User;
import cn.kong.cosmos.auth.service.LoginStrategy;
import cn.kong.cosmos.auth.service.TokenService;
import cn.kong.cosmos.auth.service.UserQueryService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 微信扫码登录策略：从参数获取 OpenID → 查找或创建用户 → 签发 Token
 */
@Service
public class WechatLoginStrategy implements LoginStrategy<Map<String, String>> {

    private final TokenService tokenService;
    private final UserQueryService userQueryService;

    public WechatLoginStrategy(TokenService tokenService, UserQueryService userQueryService) {
        this.tokenService = tokenService;
        this.userQueryService = userQueryService;
    }

    @Override
    public String type() {
        return "WECHAT";
    }

    @Override
    public Class<Map<String, String>> paramType() {
        @SuppressWarnings("unchecked")
        Class<Map<String, String>> clazz = (Class<Map<String, String>>) (Class<?>) Map.class;
        return clazz;
    }

    @Override
    public AuthTokenResp executeLogin(Map<String, String> param) {
        String openId = param.get("openId");
        if (openId == null || openId.isEmpty()) {
            throw new IllegalArgumentException("缺少 openId 参数");
        }

        // 通过 OpenID 查找或创建微信用户
        User user = userQueryService.findOrCreateByWechatOpenid(openId);

        // 检查用户状态
        if (user.getUserStatus() != null && user.getUserStatus() != 1) {
            throw new IllegalArgumentException("账号已被禁用或封禁");
        }

        // 签发双 Token
        return tokenService.issueTokens(user.getId(), user.getNickname());
    }
}
