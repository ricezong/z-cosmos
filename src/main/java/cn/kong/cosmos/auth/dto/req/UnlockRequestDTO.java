package cn.kong.cosmos.auth.dto.req;

import lombok.Data;

/**
 * 解锁请求DTO
 */
@Data
public class UnlockRequestDTO {
    /** 设备ID */
    private String deviceId;
    
    /** 模块类型 */
    private String moduleType;
}
