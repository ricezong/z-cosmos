package cn.kong.cosmos.biz.community.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 发布帖子请求
 */
@Data
public class CreatePostReq {

    @NotBlank(message = "分类不能为空")
    private String categoryCode;

    @NotBlank(message = "标题不能为空")
    @Size(min = 4, max = 100, message = "标题长度 4-100 字")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    /** 封面图（可选） */
    private String coverImage;

    /** 图片数组，最多 9 张 */
    @Size(max = 9, message = "最多上传 9 张图片")
    private List<String> images;
}
