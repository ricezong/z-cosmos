package cn.kong.cosmos.auth.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import cn.kong.cosmos.auth.service.TokenService;
import cn.kong.cosmos.auth.util.CookieTokenHelper;
import cn.kong.cosmos.auth.util.CurrentUserContext;
import cn.kong.cosmos.common.core.Result;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 解析拦截器：
 * 1. 优先从 Authorization Header 读取 Bearer Token
 * 2. 回退到 HttpOnly Cookie 读取
 * 3. 委托 TokenService 统一验签，避免密钥解析逻辑重复
 */
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final CookieTokenHelper cookieTokenHelper;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 从 Authorization Header 读取
        String token = extractFromHeader(request);

        // 2. 回退到 Cookie 读取
        if (token == null) {
            token = cookieTokenHelper.getTokenFromCookie(request);
        }

        if (token == null || token.isEmpty()) {
            writeUnauthorizedResponse(response, "未登录，请先登录");
            return false;
        }

        try {
            // 3. 委托 TokenService 统一验签解析
            Claims claims = tokenService.parseToken(token);

            Long userId = claims.get("userId", Long.class);
            String nickname = claims.get("nickname", String.class);

            if (userId == null || nickname == null) {
                writeUnauthorizedResponse(response, "Token 信息不完整");
                return false;
            }

            // 4. 注入用户信息到 Request
            CurrentUserContext.setUser(userId, nickname);
            return true;

        } catch (Exception e) {
            writeUnauthorizedResponse(response, "Token 无效或已过期");
            return false;
        }
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

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(objectMapper.writeValueAsString(
                    Result.unauthorized(message)));
        } catch (Exception e) {
            // ignore
        }
    }
}
