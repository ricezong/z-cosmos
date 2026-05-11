package cn.kong.cosmos.biz.note.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记内容实体 - 对应 z_note_contents 表
 */
@Data
@TableName("z_note_contents")
public class NoteContent {

    /** 关联 z_notes.note_id */
    @TableId(type = IdType.NONE)
    private String noteId;

    /** 完整 Markdown 源码 */
    private String content;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
