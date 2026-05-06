package cn.kong.cosmos.common.exception;

import lombok.Getter;

/**
 * 业务异常基类
 * 携带 HTTP 状态码和错误消息，由 GlobalExceptionHandler 统一处理
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    // ==================== 快捷工厂方法 ====================

    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }

    public static BusinessException tooManyRequests(String message) {
        return new BusinessException(429, message);
    }
}
