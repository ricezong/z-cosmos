package cn.kong.cosmos.auth.config;

import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信公众号 SDK 初始化（AppID/Secret/Token/AESKey 注入，自动缓存 AccessToken）
 * 配置来源统一由 WechatMpProperties 提供
 */
@Configuration
public class WxMpConfig {

    private final WechatMpProperties properties;

    public WxMpConfig(WechatMpProperties properties) {
        this.properties = properties;
    }

    @Bean
    public WxMpService wxMpService() {
        properties.validate();

        WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
        config.setAppId(properties.getAppId());
        config.setSecret(properties.getSecret());
        config.setToken(properties.getToken());
        if (properties.getAesKey() != null && !properties.getAesKey().isBlank()) {
            config.setAesKey(properties.getAesKey());
        }

        WxMpServiceImpl service = new WxMpServiceImpl();
        service.setWxMpConfigStorage(config);
        return service;
    }
}
