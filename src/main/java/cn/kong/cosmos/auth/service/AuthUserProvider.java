package cn.kong.cosmos.auth.service;

import cn.kong.cosmos.auth.dto.resp.UserSimpleDTO;
import cn.kong.cosmos.auth.entity.User;

/**
 * auth 模块对外暴露的用户信息提供者
 * biz 模块通过此接口获取/更新用户数据，不直接依赖 User 实体或 UserMapper
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

    // ==================== 为 Profile 模块提供的扩展契约 ====================

    /**
     * 根据主键 ID 获取完整 User 实体（仅供 Profile 个人中心读自己资料）
     * 返回 null 表示不存在
     */
    User getFullUserById(Long id);

    /**
     * 更新个人资料（昵称/性别/签名/位置/生日）
     * 传入 null 字段跳过更新，非 null 字段执行更新
     * @return 影响行数
     */
    int updateProfile(Long id,
                      String nickname,
                      Integer gender,
                      String bio,
                      String location,
                      java.time.LocalDate birthday);

    /**
     * 修改密码（已验签旧密码后调用）
     * @return 影响行数
     */
    int updatePasswordHash(Long id, String newPasswordHash);

    /**
     * 更新头像 URL
     * @return 影响行数
     */
    int updateAvatarUrl(Long id, String avatarUrl);
}
