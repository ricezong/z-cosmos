package cn.kong.cosmos.biz.profile.service;

import cn.kong.cosmos.biz.profile.dto.req.ChangePasswordReq;
import cn.kong.cosmos.biz.profile.dto.req.UpdateProfileReq;
import cn.kong.cosmos.biz.profile.dto.resp.UserProfileDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人中心服务
 */
public interface UserProfileService {

    /** 获取当前登录用户完整资料 */
    UserProfileDTO getCurrentUserProfile();

    /** 更新当前用户资料 */
    void updateProfile(UpdateProfileReq req);

    /** 修改密码 */
    void changePassword(ChangePasswordReq req);

    /** 上传头像，返回 URL */
    String uploadAvatar(MultipartFile file);
}
