package cn.kong.cosmos.biz.note.dto.resp;

import lombok.Data;
import java.util.List;

/**
 * 笔记预览 DTO（用于列表页）
 */
@Data
public class NotePreviewDTO {
    
    /** 笔记业务 ID */
    private String noteId;
    
    /** 笔记标题 */
    private String title;
    
    /** 分类编码 */
    private String categoryCode;
    
    /** 分类名称 */
    private String categoryName;
    
    /** 标签数组 */
    private List<String> tags;
    
    /** 内容类型：0-原创 1-转载 */
    private Integer contentType;
    
    /** 是否锁定 */
    private Boolean isLocked;
    
    /** SEO 摘要 */
    private String shortSummary;
    
    /** 预览内容 */
    private String previewContent;
    
    /** 预估阅读耗时 */
    private Integer readMinutes;
    
    /** 创建时间 */
    private String createdAt;
}
