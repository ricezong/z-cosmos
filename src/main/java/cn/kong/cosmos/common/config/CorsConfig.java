package cn.kong.cosmos.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域配置：允许 withCredentials=true
 * 生产环境必须精确配置 allowedOriginPatterns（禁用 *），防止 CSRF
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 开发环境允许所有来源，生产环境请替换为具体域名
                .allowedOriginPatterns(
                    "http://localhost:*",
                    "http://127.0.0.1:*",
                    // 生产环境请取消下方注释并替换为实际域名
                    // "https://yourdomain.com"
                    "${cors.allowed-origins:*}"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
