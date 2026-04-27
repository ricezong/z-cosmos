package cn.kong.cosmos.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信公众号配置属性 - 统一绑定 application.yml
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.mp")
public class WechatMpProperties {

    /** 公众号 AppID */
    private String appId;
    /** 公众号 Secret */
    private String secret;
    /** 公众号 Token（用于验签） */
    private String token;
    /** 公众号 AES 加密密钥（可选） */
    private String aesKey;

    /**
     * 校验必要配置是否完整
     */
    public void validate() {
        if (appId == null || appId.isBlank()) {
            throw new IllegalStateException("微信公众号 appId 未配置！请设置 wechat.mp.appId");
        }
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("微信公众号 secret 未配置！请设置 wechat.mp.secret");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("微信公众号 token 未配置！请设置 wechat.mp.token");
        }
    }
}
