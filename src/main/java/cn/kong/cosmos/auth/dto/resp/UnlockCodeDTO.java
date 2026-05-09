package cn.kong.cosmos.auth.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 解锁口令DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnlockCodeDTO {
    /** 6位动态口令 */
    private String code;
    
    /** 过期时间（秒） */
    private Integer expiresIn;
}
