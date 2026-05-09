package cn.kong.cosmos.biz.note.dto.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记列表DTO
 */
@Data
public class NoteListDTO {
    private String noteId;
    private String title;
    private String shortSummary;
    private String category;
    private List<String> tags;
    private String coverImage;
    private Long viewCount;
    private LocalDateTime updatedAt;
}
