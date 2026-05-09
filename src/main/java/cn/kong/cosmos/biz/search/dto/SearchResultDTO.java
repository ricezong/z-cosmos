package cn.kong.cosmos.biz.search.dto;

import lombok.Data;

/**
 * 搜索结果DTO
 */
@Data
public class SearchResultDTO {
    /** 内容类型（NOTE/HOT） */
    private String contentType;
    
    /** 内容业务ID */
    private String contentId;
    
    /** 标题 */
    private String title;
    
    /** 高亮片段 */
    private String highlight;
}
