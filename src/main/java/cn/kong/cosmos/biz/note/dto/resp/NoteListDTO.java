package cn.kong.cosmos.biz.note.dto.resp;

import lombok.Data;
import java.util.List;

/**
 * 笔记列表项 DTO
 */
@Data
public class NoteListDTO {
    
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
    
    /** 转载来源链接 */
    private String sourceUrl;
    
    /** 预估阅读耗时 (分钟) */
    private Integer readMinutes;
    
    /** 是否锁定：0-公开 1-需解锁 */
    private Integer isLocked;
    
    /** SEO 摘要 */
    private String shortSummary;
    
    /** 累计阅读数 */
    private Long viewCount;
    
    /** 创建时间 */
    private String createdAt;
}
