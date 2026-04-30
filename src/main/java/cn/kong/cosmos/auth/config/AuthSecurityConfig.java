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
                    "/api/auth/login",                   // 账密登录
                    "/api/auth/refresh",                 // Token 刷新
                    "/api/auth/oauth/url",               // 获取 OAuth 链接
                    "/api/auth/oauth/callback",          // OAuth 回调
                    "/api/auth/wechat/**",               // 微信相关（扫码/回调）
                    "/api/posts",                        // 帖子列表(公开，发帖走 Service 鉴权)
                    "/api/posts/{postId}",               // 帖子详情(公开，编辑/删除走 Service 鉴权)
                    "/api/posts/{postId}/comments",      // 评论列表(公开，发评走 Service 鉴权)
                    "/api/categories",                   // 分类列表(公开)
                    "/api/news/**",                      // 热点/新闻(公开只读)
                    "/api/videos",                       // 视频列表(公开)
                    "/api/videos/*",                     // 视频详情(公开，play 端点需登录)
                    "/api/videos/*/episodes",            // 剧集列表(公开)
                    "/api/search/**",                    // 聚合搜索(公开只读)
                    "/api/public/**"                     // 其他公开接口
                );
    }
}
