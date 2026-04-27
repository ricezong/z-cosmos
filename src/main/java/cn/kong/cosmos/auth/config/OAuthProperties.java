package cn.kong.cosmos.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OAuth 第三方登录配置属性 - 统一绑定 application.yml
 */
@Data
@Component
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

    private Provider github = new Provider();
    private Provider gitee = new Provider();
    private Provider qq = new Provider();

    /**
     * 获取指定提供商的配置
     */
    public Provider getProvider(String name) {
        return switch (name.toLowerCase()) {
            case "github" -> github;
            case "gitee" -> gitee;
            case "qq" -> qq;
            default -> throw new IllegalArgumentException("不支持的 OAuth 提供商：" + name);
        };
    }

    @Data
    public static class Provider {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
    }
}
