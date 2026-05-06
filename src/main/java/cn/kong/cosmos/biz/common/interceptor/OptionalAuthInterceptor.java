package cn.kong.cosmos.biz.common.interceptor;

import cn.kong.cosmos.auth.service.TokenService;
import cn.kong.cosmos.auth.util.CurrentUserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 可选鉴权拦截器（公开但登录增强）
 * 尝试从 Authorization 头解析 Access Token，成功则设置用户上下文，失败按未登录处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OptionalAuthInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractFromHeader(request);
        if (token == null || token.isEmpty()) {
            return true;
        }
        try {
            Claims claims = tokenService.parseToken(token);
            String userId = claims.getSubject();
            String nickname = claims.get("nickname", String.class);
            if (userId != null) {
                CurrentUserContext.setUser(userId, nickname);
            }
        } catch (Exception e) {
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
