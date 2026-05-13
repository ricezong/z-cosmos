package cn.kong.cosmos.auth.dto.req;

import lombok.Data;

/**
 * 验证解锁口令请求DTO
 */
@Data
public class UnlockValidateDTO {
    /** 设备ID */
    private String deviceId;
    
    /** 6位解锁口令 */
    private String unlockCode;
}