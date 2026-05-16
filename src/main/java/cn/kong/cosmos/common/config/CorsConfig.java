package cn.kong.cosmos.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域配置：允许 withCredentials=true
 * 生产环境必须精确配置 allowedOriginPatterns（禁用 *），防止 CSRF
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 生产环境从环境变量读取允许的域名，多个域名用逗号分隔
        String[] origins;
        if (allowedOrigins != null && !allowedOrigins.isEmpty() && !"*".equals(allowedOrigins)) {
            origins = allowedOrigins.split(",");
        } else {
            // 开发环境允许本地调试
            origins = new String[]{"http://localhost:*", "http://127.0.0.1:*"};
        }

        registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}