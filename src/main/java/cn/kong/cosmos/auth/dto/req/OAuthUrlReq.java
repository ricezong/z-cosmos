package cn.kong.cosmos.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 获取授权链接请求体
 */
@Data
public class OAuthUrlReq {
    @NotBlank(message = "provider 不能为空")
    private String provider;
}
