package cn.kong.cosmos.auth.config;

import lombok.Getter;

/**
 * OAuth 提供商枚举 - 消除 switch-case 硬编码
 */
@Getter
public enum OAuthProvider {

    GITHUB("github",
            "https://github.com/login/oauth/authorize",
            "https://github.com/login/oauth/access_token",
            "https://api.github.com/user",
            "user:email"),

    GITEE("gitee",
            "https://gitee.com/oauth/authorize",
            "https://gitee.com/oauth/token",
            "https://gitee.com/api/v5/user",
            "user_info"),

    QQ("qq",
            "https://graph.qq.com/oauth2.0/authorize",
            "https://graph.qq.com/oauth2.0/token",
            "https://graph.qq.com/user/get_user_info",
            "get_user_info");

    /** 提供商标识 */
    private final String id;
    /** 授权码 URL */
    private final String authUrl;
    /** Token 交换 URL */
    private final String tokenUrl;
    /** 用户信息 URL */
    private final String userInfoUrl;
    /** 默认 scope */
    private final String scope;

    OAuthProvider(String id, String authUrl, String tokenUrl, String userInfoUrl, String scope) {
        this.id = id;
        this.authUrl = authUrl;
        this.tokenUrl = tokenUrl;
        this.userInfoUrl = userInfoUrl;
        this.scope = scope;
    }

    /**
     * 根据 id 获取枚举
     */
    public static OAuthProvider fromId(String id) {
        for (OAuthProvider p : values()) {
            if (p.id.equalsIgnoreCase(id)) {
                return p;
            }
        }
        throw new IllegalArgumentException("不支持的 OAuth 提供商：" + id);
    }
}
