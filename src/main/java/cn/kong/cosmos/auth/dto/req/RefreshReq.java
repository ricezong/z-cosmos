package cn.kong.cosmos.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Token 刷新请求体
 */
@Data
public class RefreshReq {
    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
