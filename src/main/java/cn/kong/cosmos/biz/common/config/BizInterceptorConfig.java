package cn.kong.cosmos.biz.common.config;

import cn.kong.cosmos.biz.common.interceptor.OptionalAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 为"公开但登录可增强"的 biz 路径注册可选鉴权拦截器
 * 注意：所注册的路径必须与 AuthSecurityConfig 中 JwtAuthInterceptor 的 exclude 列表一致
 */
@Configuration
@RequiredArgsConstructor
public class BizInterceptorConfig implements WebMvcConfigurer {

    private final OptionalAuthInterceptor optionalAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(optionalAuthInterceptor)
                .addPathPatterns(
                        "/api/posts",                    // 帖子列表 + 发帖（POST 时 Service 校验登录）
                        "/api/posts/{postId}",           // 帖子详情 + 编辑/删除（写操作 Service 校验登录）
                        "/api/posts/{postId}/comments",  // 评论列表 + 发表评论（同上）
                        "/api/categories"                // 分类列表（纯读）
                );
    }
}
