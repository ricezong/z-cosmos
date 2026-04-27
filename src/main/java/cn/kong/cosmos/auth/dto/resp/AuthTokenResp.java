package cn.kong.cosmos.auth.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证 Token 响应体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenResp {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String nickname;
}
