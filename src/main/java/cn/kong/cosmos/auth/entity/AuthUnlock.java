package cn.kong.cosmos.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 全局认证解锁实体 - 对应 z_auth_unlock 表
 */
@Data
@TableName("z_auth_unlock")
public class AuthUnlock {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 设备指纹或 SessionID */
    private String deviceId;

    /** 业务模块标识 */
    private String moduleType;

    /** 6 位动态口令 */
    private String unlockCode;

    /** 解锁状态：0-未解锁 1-已解锁 */
    private Integer status;

    /** 授权过期时间 */
    private LocalDateTime expiresAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
