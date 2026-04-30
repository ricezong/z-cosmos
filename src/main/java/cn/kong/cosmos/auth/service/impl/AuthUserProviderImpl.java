package cn.kong.cosmos.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import cn.kong.cosmos.auth.dto.resp.UserSimpleDTO;
import cn.kong.cosmos.auth.entity.User;
import cn.kong.cosmos.auth.mapper.UserMapper;
import cn.kong.cosmos.auth.service.AuthUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * AuthUserProvider 实现 - auth 模块对外提供用户数据
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

    @Override
    public User getFullUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public int updateProfile(Long id, String nickname, Integer gender,
                             String bio, String location, LocalDate birthday) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, id);
        boolean hasUpdate = false;
        if (StrUtil.isNotBlank(nickname)) { wrapper.set(User::getNickname, nickname); hasUpdate = true; }
        if (gender != null)                { wrapper.set(User::getGender, gender);   hasUpdate = true; }
        if (bio != null)                   { wrapper.set(User::getBio, bio);         hasUpdate = true; }
        if (location != null)              { wrapper.set(User::getLocation, location); hasUpdate = true; }
        if (birthday != null)              { wrapper.set(User::getBirthday, birthday); hasUpdate = true; }
        if (!hasUpdate) {
            return 0;
        }
        return userMapper.update(null, wrapper);
    }

    @Override
    public int updatePasswordHash(Long id, String newPasswordHash) {
        return userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, id)
                .set(User::getPasswordHash, newPasswordHash));
    }

    @Override
    public int updateAvatarUrl(Long id, String avatarUrl) {
        return userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, id)
                .set(User::getAvatarUrl, avatarUrl));
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
