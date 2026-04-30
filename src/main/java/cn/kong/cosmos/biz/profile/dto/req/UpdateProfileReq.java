package cn.kong.cosmos.biz.profile.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新个人资料请求（所有字段可选）
 */
@Data
public class UpdateProfileReq {

    @Size(min = 2, max = 20, message = "昵称长度 2-20 字")
    private String nickname;

    /** 0-未知 1-男 2-女 */
    @Min(value = 0, message = "性别值非法")
    @Max(value = 2, message = "性别值非法")
    private Integer gender;

    @Size(max = 200, message = "签名最多 200 字")
    private String bio;

    @Size(max = 50, message = "地区最多 50 字")
    private String location;

    private LocalDate birthday;
}
