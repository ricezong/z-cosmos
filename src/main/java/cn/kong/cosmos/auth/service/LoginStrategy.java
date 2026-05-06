package cn.kong.cosmos.auth.service;

import cn.kong.cosmos.auth.dto.resp.AuthTokenResp;

/**
 * 登录策略统一接口
 * @param <T> 登录参数类型
 */
public interface LoginStrategy<T> {
    
    /**
     * 策略类型标识
     */
    String type();

    /**
     * 获取参数类型（用于安全转换）
     */
    Class<T> paramType();
    
    /**
     * 执行登录逻辑
     * @param param 登录参数
     * @return 认证 Token 响应
     */
    AuthTokenResp executeLogin(T param);
}
