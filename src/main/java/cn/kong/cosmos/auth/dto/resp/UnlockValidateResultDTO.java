package cn.kong.cosmos.auth.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证解锁口令结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnlockValidateResultDTO {
    /** 是否验证成功 */
    private Boolean success;
}