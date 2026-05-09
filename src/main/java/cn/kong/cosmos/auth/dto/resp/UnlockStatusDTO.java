package cn.kong.cosmos.auth.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 解锁状态DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnlockStatusDTO {
    /** 是否已解锁 */
    private Boolean unlocked;
}
