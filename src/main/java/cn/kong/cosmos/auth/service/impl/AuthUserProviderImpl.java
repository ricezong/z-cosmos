package cn.kong.cosmos.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import cn.kong.cosmos.auth.dto.resp.UserSimpleDTO;
import cn.kong.cosmos.auth.entity.User;
import cn.kong.cosmos.auth.mapper.UserMapper;
import cn.kong.cosmos.auth.service.AuthUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * AuthUserProvider 实现 - auth 模块对外提供用户只读数据
 */
@Service
@RequiredArgsConstructor
public class AuthUserProviderImpl implements AuthUserProvider {

    private final UserMapper userMapper;

    @Override
    public UserSimpleDTO getUserById(Long id) {
        User user = userMapper.selectById(id);
        return toDTO(user);
    }

    @Override
    public UserSimpleDTO getUserByUserId(String userId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserId, userId);
        User user = userMapper.selectOne(wrapper);
        return toDTO(user);
    }

    @Override
    public boolean existsByUserId(String userId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserId, userId);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public void incrementPostCount(String userId) {
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getUserId, userId)
                .setSql("post_count = post_count + 1"));
    }

    private UserSimpleDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UserSimpleDTO(
                user.getId(),
                user.getUserId(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getUserStatus()
        );
    }
}
