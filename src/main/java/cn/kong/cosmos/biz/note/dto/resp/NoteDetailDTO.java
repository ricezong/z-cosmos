package cn.kong.cosmos.biz.note.dto.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记详情DTO
 */
@Data
public class NoteDetailDTO {
    private String noteId;
    private String title;
    private String content;
    private String category;
    private List<String> tags;
    private String coverImage;
    private Long viewCount;
    private Integer isLocked;
    private LocalDateTime updatedAt;
}
