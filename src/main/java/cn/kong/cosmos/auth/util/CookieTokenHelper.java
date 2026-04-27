package cn.kong.cosmos.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * HttpOnly/Secure/SameSite Cookie 读写封装（生产环境防 XSS 核心）
 * 仅通过 Set-Cookie 响应头方式写入，避免重复下发
 */
@Component
public class CookieTokenHelper {

    @Value("${jwt.cookie-name:ACCESS_TOKEN}")
    private String cookieName;

    @Value("${jwt.cookie-path:/}")
    private String cookiePath;

    @Value("${jwt.cookie-max-age:86400}")
    private int cookieMaxAge;

    @Value("${cookie.secure:false}")
    private boolean secure;

    @Value("${cookie.same-site:Lax}")
    private String sameSite;

    @Value("${cookie.domain:}")
    private String domain;

    /**
     * 写入 Token Cookie（仅通过 Set-Cookie 响应头）
     */
    public void setTokenCookie(HttpServletResponse response, String token) {
        StringBuilder sb = new StringBuilder();
        sb.append(cookieName).append("=").append(token);
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
     * 删除 Token Cookie
     */
    public void removeTokenCookie(HttpServletResponse response) {
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
     * 从 Cookie 读取 Token
     */
    public String getTokenFromCookie(HttpServletRequest request) {
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
