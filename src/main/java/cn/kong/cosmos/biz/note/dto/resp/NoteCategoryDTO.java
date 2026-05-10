package cn.kong.cosmos.biz.note.dto.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记类别DTO
 */
@Data
public class NoteCategoryDTO {
    private Long id;
    private String categoryCode;
    private String categoryName;
    private String description;
    private String iconUrl;
    private Integer sortOrder;
    private Integer isEnabled;
    private Long noteCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}