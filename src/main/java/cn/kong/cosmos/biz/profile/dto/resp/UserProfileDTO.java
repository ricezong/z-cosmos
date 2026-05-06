package cn.kong.cosmos.biz.profile.dto.resp;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 个人资料完整 DTO（用户自身查看）
 */
@Data
public class UserProfileDTO {

    private Long id;
    private String userId;

    /** 手机号（脱敏展示） */
    private String phone;

    private String nickname;
    private String avatarUrl;
    private Integer gender;
    private String bio;
    private String location;
    private LocalDate birthday;

    private Integer userType;
    private Integer userLevel;
    private Integer verifiedStatus;
    private String verifiedReason;

    private Long followerCount;
    private Long followingCount;
    private Long postCount;
    private Long likeCount;
    private Long collectCount;

    private Integer userStatus;
    private Integer creditScore;

    private LocalDateTime createdAt;
}
