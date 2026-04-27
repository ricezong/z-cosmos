package cn.kong.cosmos.auth.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.kong.cosmos.auth.entity.User;
import cn.kong.cosmos.auth.mapper.UserMapper;
import cn.kong.cosmos.auth.service.UserQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户查询服务实现
 * - 唯一索引兜底 + DuplicateKeyException 捕获重试
 * - 并发安全创建
 */
@Slf4j
@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserMapper userMapper;
    private static final int MAX_RETRY = 3;

    public UserQueryServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User findByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public User createByPhone(String phone, String passwordHash) {
        // 先检查是否已存在（防并发重复插入）
        User existing = findByPhone(phone);
        if (existing != null) {
            return existing;
        }

        User user = new User();
        user.setUserId(generateUserId());
        user.setPhone(phone);
        user.setPasswordHash(passwordHash);
        user.setNickname(phone.substring(0, 3) + "****" + phone.substring(7));
        user.setUserStatus(1);
        user.setCreditScore(100);

        try {
            userMapper.insert(user);
            log.info("自动注册新用户成功：userId={}, phone={}", user.getId(), phone);
        } catch (DuplicateKeyException e) {
            // 并发插入冲突，回查返回
            log.warn("手机号注册冲突，回查：phone={}", phone);
            user = findByPhone(phone);
        }

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User findOrCreateByWechatOpenid(String openId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getWechatOpenid, openId);
        User user = userMapper.selectOne(wrapper);

        if (user != null) {
            return user;
        }

        return createWithRetry(() -> {
            User newUser = new User();
            newUser.setUserId(generateUserId());
            newUser.setWechatOpenid(openId);
            newUser.setNickname("wx_" + System.currentTimeMillis());
            newUser.setUserStatus(1);
            newUser.setCreditScore(100);
            return newUser;
        }, () -> {
            LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
            w.eq(User::getWechatOpenid, openId);
            return userMapper.selectOne(w);
        }, "wechat");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User findOrCreateByOAuth(String provider, String openId, String nickname, String avatar) {
        User user = queryByProviderAndOpenId(provider, openId);

        if (user != null) {
            return user;
        }

        return createWithRetry(() -> {
            User newUser = new User();
            newUser.setUserId(generateUserId());
            if (StrUtil.isNotBlank(nickname)) {
                newUser.setNickname(nickname);
            } else {
                newUser.setNickname(provider + "_" + System.currentTimeMillis());
            }
            if (StrUtil.isNotBlank(avatar)) {
                newUser.setAvatarUrl(avatar);
            }
            setProviderOpenId(newUser, provider, openId);
            newUser.setUserStatus(1);
            newUser.setCreditScore(100);
            return newUser;
        }, () -> queryByProviderAndOpenId(provider, openId), provider);
    }

    /**
     * 通用重试创建模板
     */
    private User createWithRetry(UserFactory factory, UserFallbackQuery fallbackQuery, String provider) {
        int retryCount = 0;
        while (retryCount < MAX_RETRY) {
            try {
                User user = factory.create();
                userMapper.insert(user);
                log.info("创建{}用户成功：userId={}", provider.toUpperCase(), user.getId());
                return user;
            } catch (DuplicateKeyException e) {
                retryCount++;
                log.warn("创建{}用户冲突，第{}次重试", provider.toUpperCase(), retryCount);
                if (retryCount >= MAX_RETRY) {
                    User existing = fallbackQuery.query();
                    if (existing != null) {
                        return existing;
                    }
                    throw new RuntimeException("创建用户失败，请稍后重试");
                }
                try {
                    Thread.sleep(50L * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("创建用户被中断");
                }
            }
        }
        throw new RuntimeException("创建用户失败");
    }

    /**
     * 根据提供商和 OpenID 查询用户
     */
    private User queryByProviderAndOpenId(String provider, String openId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        switch (provider.toLowerCase()) {
            case "wechat":
                wrapper.eq(User::getWechatOpenid, openId);
                break;
            case "github":
                wrapper.eq(User::getGithubOpenid, openId);
                break;
            case "gitee":
                wrapper.eq(User::getGiteeOpenid, openId);
                break;
            case "qq":
                wrapper.eq(User::getQqOpenid, openId);
                break;
            default:
                throw new IllegalArgumentException("不支持的 OAuth 提供商：" + provider);
        }

        return userMapper.selectOne(wrapper);
    }

    /**
     * 设置提供商 OpenID
     */
    private void setProviderOpenId(User user, String provider, String openId) {
        switch (provider.toLowerCase()) {
            case "wechat":
                user.setWechatOpenid(openId);
                break;
            case "github":
                user.setGithubOpenid(openId);
                break;
            case "gitee":
                user.setGiteeOpenid(openId);
                break;
            case "qq":
                user.setQqOpenid(openId);
                break;
            default:
                throw new IllegalArgumentException("不支持的 OAuth 提供商：" + provider);
        }
    }

    @FunctionalInterface
    private interface UserFactory {
        User create();
    }

    @FunctionalInterface
    private interface UserFallbackQuery {
        User query();
    }

    /**
     * 生成用户唯一业务 ID（基于雪花算法，18 位数字字符串）
     */
    private String generateUserId() {
        return IdUtil.getSnowflakeNextIdStr();
    }
}
