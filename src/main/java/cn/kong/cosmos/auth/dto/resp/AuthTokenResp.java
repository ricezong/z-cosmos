package cn.kong.cosmos.auth.dto.resp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证 Token 响应体
 * - Access Token: 响应体返回，前端存内存，通过 Authorization 头携带
 * - Refresh Token: 通过 HttpOnly Cookie 传递，@JsonIgnore 防止暴露给前端 JS
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenResp {
    private String accessToken;
    @JsonIgnore
    private String refreshToken;
    private String userId;
    private String nickname;
}
