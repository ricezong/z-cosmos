package cn.kong.cosmos.auth.service;

import cn.kong.cosmos.auth.entity.User;

/**
 * 用户查询接口 - auth 模块管理用户表
 */
public interface UserQueryService {
    
    /**
     * 根据 ID 查找用户
     */
    User findById(Long id);
    
    /**
     * 根据手机号查找用户
     */
    User findByPhone(String phone);
    
    /**
     * 创建新用户（首次登录自动注册）
     */
    User createByPhone(String phone, String passwordHash);
    
    /**
     * 根据微信 OpenID 查找或创建用户
     */
    User findOrCreateByWechatOpenid(String openId);
    
    /**
     * 根据 OAuth 提供商和 OpenID 查找或创建用户（通用方法）
     * @param provider 提供商：wechat/github/gitee/qq
     * @param openId 第三方用户唯一标识
     * @param nickname 昵称
     * @param avatar 头像 URL
     * @return 用户实体
     */
    User findOrCreateByOAuth(String provider, String openId, String nickname, String avatar);
}
