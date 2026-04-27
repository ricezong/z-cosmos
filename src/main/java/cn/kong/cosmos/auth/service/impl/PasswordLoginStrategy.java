package cn.kong.cosmos.auth.service.impl;

import cn.kong.cosmos.auth.dto.req.AccountLoginReq;
import cn.kong.cosmos.auth.dto.resp.AuthTokenResp;
import cn.kong.cosmos.auth.entity.User;
import cn.kong.cosmos.auth.service.LoginStrategy;
import cn.kong.cosmos.auth.service.TokenService;
import cn.kong.cosmos.auth.service.UserQueryService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 账密登录策略：
 * - 手机号存在：BCrypt 校验密码 → 登录
 * - 手机号不存在：自动注册 → 登录（首次登录即注册）
 */
@Service
public class PasswordLoginStrategy implements LoginStrategy<AccountLoginReq> {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final UserQueryService userQueryService;
    private final TokenService tokenService;

    public PasswordLoginStrategy(UserQueryService userQueryService, TokenService tokenService) {
        this.userQueryService = userQueryService;
        this.tokenService = tokenService;
    }

    @Override
    public String type() {
        return "PASSWORD";
    }

    @Override
    public Class<AccountLoginReq> paramType() {
        return AccountLoginReq.class;
    }

    @Override
    public AuthTokenResp executeLogin(AccountLoginReq req) {

        User user = userQueryService.findByPhone(req.getPhone());

        if (user == null) {
            // 首次登录：自动注册
            String passwordHash = PASSWORD_ENCODER.encode(req.getPassword());
            user = userQueryService.createByPhone(req.getPhone(), passwordHash);
        } else {
            // 检查用户状态
            if (user.getUserStatus() != null && user.getUserStatus() != 1) {
                throw new IllegalArgumentException("账号已被禁用或封禁");
            }
            // 已存在：校验密码
            if (!PASSWORD_ENCODER.matches(req.getPassword(), user.getPasswordHash())) {
                throw new IllegalArgumentException("手机号或密码错误");
            }
        }

        return tokenService.issueTokens(user.getId(), user.getNickname());
    }
}
