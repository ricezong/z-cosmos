package cn.kong.cosmos.biz.note.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 笔记预览DTO
 */
@Data
public class NotePreviewDTO {
    private NoteInfoDTO note;
    private String previewContent;
    
    @Data
    public static class NoteInfoDTO {
        private String noteId;
        private String title;
        private Integer isLocked;
        private BigDecimal previewRatio;
    }
}
