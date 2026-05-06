package cn.kong.cosmos.biz.community.dto.req;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 编辑帖子请求（字段均可选，传什么改什么）
 */
@Data
public class UpdatePostReq {

    private String categoryCode;

    @Size(min = 4, max = 100, message = "标题长度 4-100 字")
    private String title;

    private String content;

    private String coverImage;

    @Size(max = 9, message = "最多上传 9 张图片")
    private List<String> images;
}
