package cn.kong.cosmos.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 线程安全上下文提取：通过 RequestContextHolder 获取拦截器注入的 userId/nickname
 * 数据存储在 Request Attribute 中，随请求结束自动销毁，无需手动清理
 */
public class CurrentUserContext {

    private static final String USER_ID_ATTR = "currentUser.userId";
    private static final String NICKNAME_ATTR = "currentUser.nickname";

    /**
     * 获取当前登录用户 ID
     */
    public static Long getUserId() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        Object userId = request.getAttribute(USER_ID_ATTR);
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    /**
     * 获取当前登录用户 ID (字符串格式)
     */
    public static String getUserIdStr() {
        Long userId = getUserId();
        return userId != null ? userId.toString() : null;
    }

    /**
     * 获取当前登录用户昵称
     */
    public static String getNickname() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return (String) request.getAttribute(NICKNAME_ATTR);
    }

    /**
     * 设置用户信息到 Request Attribute
     */
    public static void setUser(Long userId, String nickname) {
        HttpServletRequest request = getRequest();
        if (request != null) {
            request.setAttribute(USER_ID_ATTR, userId);
            request.setAttribute(NICKNAME_ATTR, nickname);
        }
    }

    /**
     * 清理用户上下文（Request 结束时由 Interceptor afterCompletion 调用）
     * 虽然请求结束会自动销毁，但显式清理可避免异步场景下的信息泄漏
     */
    public static void clear() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            request.removeAttribute(USER_ID_ATTR);
            request.removeAttribute(NICKNAME_ATTR);
        }
    }

    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
