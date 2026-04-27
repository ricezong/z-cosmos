package cn.kong.cosmos.auth.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户简要信息 DTO - auth 模块对外暴露的只读视图
 * biz 模块通过 AuthUserProvider 获取，不直接依赖 User 实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSimpleDTO {

    /**
     * 用户主键 ID
     */
    private Long id;

    /**
     * 用户唯一业务 ID
     */
    private String userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像 URL
     */
    private String avatarUrl;

    /**
     * 用户状态：0-禁用，1-正常，2-封禁
     */
    private Integer userStatus;
}
