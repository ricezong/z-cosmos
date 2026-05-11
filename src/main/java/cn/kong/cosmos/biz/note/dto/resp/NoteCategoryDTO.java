package cn.kong.cosmos.biz.note.dto.resp;

import lombok.Data;

/**
 * 笔记分类 DTO
 */
@Data
public class NoteCategoryDTO {
    
    /** 分类编码 */
    private String categoryCode;
    
    /** 分类名称 */
    private String categoryName;
    
    /** 分类描述 */
    private String description;
    
    /** 分类图标 URL */
    private String iconUrl;
    
    /** 关联笔记总数 */
    private Long noteCount;
}
