package cn.kong.cosmos.biz.community.dto.resp;

import lombok.Data;

/**
 * 分类 DTO
 */
@Data
public class CategoryDTO {

    private String code;
    private String name;
    private String description;
    private String icon;
    private String color;
    private Integer sort;
    private Long postCount;
}
