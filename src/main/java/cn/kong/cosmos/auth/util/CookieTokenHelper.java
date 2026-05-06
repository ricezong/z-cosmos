package cn.kong.cosmos.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * HttpOnly/Secure/SameSite Cookie 读写封装
 * 用于管理 Refresh Token Cookie（Access Token 仅通过响应体返回，由前端存内存）
 */
@Component
public class CookieTokenHelper {

    @Value("${cookie.name:REFRESH_TOKEN}")
    private String cookieName;

    @Value("${cookie.path:/}")
    private String cookiePath;

    @Value("${cookie.max-age:2592000}")
    private int cookieMaxAge;

    @Value("${cookie.secure:false}")
    private boolean secure;

    @Value("${cookie.same-site:Lax}")
    private String sameSite;

    @Value("${cookie.domain:}")
    private String domain;

    /**
     * 写入 Refresh Token Cookie（HttpOnly，浏览器自动携带）
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        StringBuilder sb = new StringBuilder();
        sb.append(cookieName).append("=").append(refreshToken);
        sb.append("; Path=").append(cookiePath);
        sb.append("; Max-Age=").append(cookieMaxAge);
        sb.append("; HttpOnly");
        if (secure) {
            sb.append("; Secure");
        }
        sb.append("; SameSite=").append(sameSite);
        if (domain != null && !domain.isEmpty()) {
            sb.append("; Domain=").append(domain);
        }
        response.addHeader("Set-Cookie", sb.toString());
    }

    /**
     * 删除 Refresh Token Cookie
     */
    public void removeRefreshTokenCookie(HttpServletResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append(cookieName).append("=");
        sb.append("; Path=").append(cookiePath);
        sb.append("; Max-Age=0");
        sb.append("; HttpOnly");
        if (secure) {
            sb.append("; Secure");
        }
        sb.append("; SameSite=").append(sameSite);
        if (domain != null && !domain.isEmpty()) {
            sb.append("; Domain=").append(domain);
        }
        response.addHeader("Set-Cookie", sb.toString());
    }

    /**
     * 从 Cookie 读取 Refresh Token
     */
    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
