package cn.kong.cosmos.biz.profile.controller;

import cn.kong.cosmos.biz.community.dto.resp.PostSummaryDTO;
import cn.kong.cosmos.biz.community.service.PostService;
import cn.kong.cosmos.biz.profile.dto.req.ChangePasswordReq;
import cn.kong.cosmos.biz.profile.dto.req.UpdateProfileReq;
import cn.kong.cosmos.biz.profile.dto.resp.UserProfileDTO;
import cn.kong.cosmos.biz.profile.service.UserProfileService;
import cn.kong.cosmos.common.core.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人中心 Controller
 * 路径前缀：/api/user  （全部端点需登录，由 JwtAuthInterceptor 拦截）
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final PostService postService;

    /**
     * 获取当前登录用户完整资料
     */
    @GetMapping("/me")
    public Result<UserProfileDTO> getCurrentUser() {
        return Result.success(userProfileService.getCurrentUserProfile());
    }

    /**
     * 更新个人资料
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileReq req) {
        userProfileService.updateProfile(req);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordReq req) {
        userProfileService.changePassword(req);
        return Result.success();
    }

    /**
     * 上传头像
     * 返回可访问的 URL
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return Result.success(userProfileService.uploadAvatar(file));
    }

    /**
     * 我的帖子（当前登录用户发布的）
     */
    @GetMapping("/posts")
    public Result<IPage<PostSummaryDTO>> listMyPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return Result.success(postService.listMyPosts(page, size));
    }

    /**
     * 我的收藏（当前登录用户收藏的帖子）
     */
    @GetMapping("/collections")
    public Result<IPage<PostSummaryDTO>> listMyCollections(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return Result.success(postService.listMyCollections(page, size));
    }
}
