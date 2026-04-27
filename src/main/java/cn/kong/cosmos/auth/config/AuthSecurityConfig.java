package cn.kong.cosmos.auth.config;

import cn.kong.cosmos.auth.interceptor.IpRateLimitInterceptor;
import cn.kong.cosmos.auth.interceptor.JwtAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 认证安全配置：拦截器注册链
 * - IP 频控限定 /api/auth/**
 * - JWT 全局保护 /api/**（精准放行登录/回调/公开接口）
 */
@Configuration
public class AuthSecurityConfig implements WebMvcConfigurer {

    private final IpRateLimitInterceptor ipRateLimitInterceptor;
    private final JwtAuthInterceptor jwtAuthInterceptor;

    public AuthSecurityConfig(IpRateLimitInterceptor ipRateLimitInterceptor,
                              JwtAuthInterceptor jwtAuthInterceptor) {
        this.ipRateLimitInterceptor = ipRateLimitInterceptor;
        this.jwtAuthInterceptor = jwtAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // IP 频控拦截器 - 仅作用于认证接口
        registry.addInterceptor(ipRateLimitInterceptor)
                .addPathPatterns("/api/auth/**");

        // JWT 认证拦截器 - 全局保护 /api/**，放行公开接口
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/auth/login",           // 账密登录
                    "/api/auth/refresh",         // Token 刷新
                    "/api/auth/oauth/url",       // 获取 OAuth 链接
                    "/api/auth/oauth/callback",  // OAuth 回调
                    "/api/auth/wechat/**",       // 微信相关（扫码/回调）
                    "/api/post/list",            // 帖子列表(公开)
                    "/api/post/{postId}",        // 帖子详情(公开)
                    "/api/public/**"             // 公开接口
                );
    }
}
