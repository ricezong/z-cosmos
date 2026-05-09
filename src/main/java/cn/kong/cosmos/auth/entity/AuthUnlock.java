package cn.kong.cosmos.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 全局认证解锁实体 - 对应 z_auth_unlock 表
 */
@Data
@TableName(value = "z_auth_unlock", autoResultMap = true)
public class AuthUnlock {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 设备唯一标识（UUID v4） */
    private String deviceId;

    /** 模块类型（NOTE/OTHER） */
    private String moduleType;

    /** 6位动态口令 */
    private String unlockCode;

    /** 状态：0-未解锁 1-已解锁 */
    private Integer status;

    /** 过期时间（12小时后） */
    private LocalDateTime expiresAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
