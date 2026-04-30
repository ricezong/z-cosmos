package cn.kong.cosmos.biz.profile.service.impl;

import cn.kong.cosmos.auth.entity.User;
import cn.kong.cosmos.auth.service.AuthUserProvider;
import cn.kong.cosmos.auth.util.CurrentUserContext;
import cn.kong.cosmos.biz.profile.dto.req.ChangePasswordReq;
import cn.kong.cosmos.biz.profile.dto.req.UpdateProfileReq;
import cn.kong.cosmos.biz.profile.dto.resp.UserProfileDTO;
import cn.kong.cosmos.biz.profile.service.UserProfileService;
import cn.kong.cosmos.biz.storage.FileStorageService;
import cn.kong.cosmos.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人中心服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final AuthUserProvider authUserProvider;
    private final FileStorageService fileStorageService;

    @Override
    public UserProfileDTO getCurrentUserProfile() {
        Long id = requireLoginId();
        User user = authUserProvider.getFullUserById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return toDTO(user);
    }

    @Override
    public void updateProfile(UpdateProfileReq req) {
        Long id = requireLoginId();
        int rows = authUserProvider.updateProfile(
                id,
                req.getNickname(),
                req.getGender(),
                req.getBio(),
                req.getLocation(),
                req.getBirthday()
        );
        if (rows == 0) {
            log.warn("updateProfile 未更新任何字段, userId={}", id);
        }
    }

    @Override
    public void changePassword(ChangePasswordReq req) {
        Long id = requireLoginId();
        User user = authUserProvider.getFullUserById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            throw BusinessException.badRequest("当前账号未设置密码，请使用第三方登录或联系管理员");
        }
        if (!PASSWORD_ENCODER.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw BusinessException.badRequest("旧密码不正确");
        }
        if (req.getOldPassword().equals(req.getNewPassword())) {
            throw BusinessException.badRequest("新密码不能与旧密码相同");
        }
        String newHash = PASSWORD_ENCODER.encode(req.getNewPassword());
        authUserProvider.updatePasswordHash(id, newHash);
        log.info("用户修改密码成功, userId={}", id);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        Long id = requireLoginId();
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("头像文件不能为空");
        }
        String url = fileStorageService.uploadImage(file, "avatar", id.toString());
        authUserProvider.updateAvatarUrl(id, url);
        log.info("用户更新头像, userId={}, url={}", id, url);
        return url;
    }

    // ========================= private =========================

    private Long requireLoginId() {
        Long id = CurrentUserContext.getUserId();
        if (id == null) {
            throw BusinessException.unauthorized("未登录");
        }
        return id;
    }

    private UserProfileDTO toDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUserId(user.getUserId());
        dto.setPhone(maskPhone(user.getPhone()));
        dto.setNickname(user.getNickname());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setGender(user.getGender());
        dto.setBio(user.getBio());
        dto.setLocation(user.getLocation());
        dto.setBirthday(user.getBirthday());
        dto.setUserType(user.getUserType());
        dto.setUserLevel(user.getUserLevel());
        dto.setVerifiedStatus(user.getVerifiedStatus());
        dto.setVerifiedReason(user.getVerifiedReason());
        dto.setFollowerCount(user.getFollowerCount());
        dto.setFollowingCount(user.getFollowingCount());
        dto.setPostCount(user.getPostCount());
        dto.setLikeCount(user.getLikeCount());
        dto.setCollectCount(user.getCollectCount());
        dto.setUserStatus(user.getUserStatus());
        dto.setCreditScore(user.getCreditScore());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    /**
     * 手机号脱敏：138****0001
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
