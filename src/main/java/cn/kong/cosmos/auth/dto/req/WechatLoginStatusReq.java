package cn.kong.cosmos.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录状态验证请求体
 */
@Data
public class WechatLoginStatusReq {
    @NotBlank(message = "sessionId 不能为空")
    private String sessionId;
}
