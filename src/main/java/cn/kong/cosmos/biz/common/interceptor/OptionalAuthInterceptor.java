package cn.kong.cosmos.biz.common.interceptor;

import cn.kong.cosmos.auth.service.TokenService;
import cn.kong.cosmos.auth.util.CookieTokenHelper;
import cn.kong.cosmos.auth.util.CurrentUserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 可选鉴权拦截器
 * <p>
 * 作用于"公开但登录可额外获得互动状态"的路径（如帖子列表/详情/评论列表/分类列表）：
 * - 有有效 Token → 解析并注入 userId/nickname 到 CurrentUserContext
 * - 无 Token 或解析失败 → 静默放行，Service 层按未登录对待
 * <p>
 * 与强制鉴权拦截器 {@code JwtAuthInterceptor} 互补：两者在路径上不重叠。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OptionalAuthInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final CookieTokenHelper cookieTokenHelper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractFromHeader(request);
        if (token == null) {
            token = cookieTokenHelper.getTokenFromCookie(request);
        }
        if (token == null || token.isEmpty()) {
            return true;
        }
        try {
            Claims claims = tokenService.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            String nickname = claims.get("nickname", String.class);
            if (userId != null) {
                CurrentUserContext.setUser(userId, nickname);
            }
        } catch (Exception e) {
            // 无效 Token 直接忽略，不阻断请求
            log.debug("OptionalAuth: token 解析失败，按未登录处理: {}", e.getMessage());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        CurrentUserContext.clear();
    }

    private String extractFromHeader(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
