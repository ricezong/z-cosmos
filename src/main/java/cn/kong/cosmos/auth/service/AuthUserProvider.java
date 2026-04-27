package cn.kong.cosmos.auth.service;

import cn.kong.cosmos.auth.dto.resp.UserSimpleDTO;

/**
 * auth 模块对外暴露的用户信息提供者
 * biz 模块通过此接口获取用户数据，不直接依赖 User 实体或 UserMapper
 */
public interface AuthUserProvider {

    /**
     * 根据主键 ID 获取用户简要信息
     */
    UserSimpleDTO getUserById(Long id);

    /**
     * 根据业务 userId 获取用户简要信息
     */
    UserSimpleDTO getUserByUserId(String userId);

    /**
     * 检查用户是否存在（根据业务 userId）
     */
    boolean existsByUserId(String userId);

    /**
     * 根据业务 userId 增加发帖数
     */
    void incrementPostCount(String userId);
}
