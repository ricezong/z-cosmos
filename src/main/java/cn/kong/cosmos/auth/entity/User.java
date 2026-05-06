package cn.kong.cosmos.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体 - 对应 z_users 表
 * 存储用户核心资料、社交统计、状态控制
 */
@Data
@TableName("z_users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户唯一业务 ID
     */
    private String userId;

    // ==================== 登录凭证 ====================

    /**
     * 手机号（账密登录唯一标识）
     */
    private String phone;

    /**
     * 密码哈希 (BCrypt，自带盐值)
     */
    private String passwordHash;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    // ==================== 第三方登录标识 ====================

    /**
     * 微信 OpenID
     */
    private String wechatOpenid;

    /**
     * 微信 UnionID
     */
    private String wechatUnionid;

    /**
     * GitHub OpenID
     */
    private String githubOpenid;

    /**
     * Gitee OpenID
     */
    private String giteeOpenid;

    /**
     * QQ OpenID
     */
    private String qqOpenid;

    // ==================== 基础资料 ====================

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像 URL
     */
    private String avatarUrl;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 个人简介/签名
     */
    private String bio;

    /**
     * 所在地区
     */
    private String location;

    /**
     * 生日
     */
    private LocalDate birthday;

    // ==================== 身份认证 ====================

    /**
     * 类型：1-普通，2-创作者，3-品牌，4-官方
     */
    private Integer userType;

    /**
     * 等级：1-10 级
     */
    private Integer userLevel;

    /**
     * 认证：0-未认证，1-已认证
     */
    private Integer verifiedStatus;

    /**
     * 认证说明
     */
    private String verifiedReason;

    // ==================== 社交统计 ====================

    /**
     * 粉丝数
     */
    private Long followerCount;

    /**
     * 关注数
     */
    private Long followingCount;

    /**
     * 发帖数
     */
    private Long postCount;

    /**
     * 获赞数
     */
    private Long likeCount;

    /**
     * 被收藏数
     */
    private Long collectCount;

    // ==================== 状态风控 ====================

    /**
     * 状态：0-禁用，1-正常，2-封禁
     */
    private Integer userStatus;

    /**
     * 信用分：0-100
     */
    private Integer creditScore;

    /**
     * 封禁原因
     */
    private String banReason;

    /**
     * 封禁到期时间
     */
    private LocalDateTime banExpireTime;

    // ==================== 审计字段 ====================

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除：0-正常，1-已删
     */
    @TableLogic
    private Integer isDeleted;
}
