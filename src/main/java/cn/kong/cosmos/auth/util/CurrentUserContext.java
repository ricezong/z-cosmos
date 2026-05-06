package cn.kong.cosmos.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 线程安全上下文提取：通过 RequestContextHolder 获取拦截器注入的 userId/nickname
 * userId 统一使用业务 userId（String，雪花算法生成）
 * 数据存储在 Request Attribute 中，随请求结束自动销毁，无需手动清理
 */
public class CurrentUserContext {

    private static final String USER_ID_ATTR = "currentUser.userId";
    private static final String NICKNAME_ATTR = "currentUser.nickname";

    /**
     * 获取当前登录用户业务 userId（z_users.user_id，雪花算法生成）
     */
    public static String getUserId() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return (String) request.getAttribute(USER_ID_ATTR);
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
    public static void setUser(String userId, String nickname) {
        HttpServletRequest request = getRequest();
        if (request != null) {
            request.setAttribute(USER_ID_ATTR, userId);
            request.setAttribute(NICKNAME_ATTR, nickname);
        }
    }

    /**
     * 清理用户上下文
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
        return attributes == null ? null : attributes.getRequest();
    }
}