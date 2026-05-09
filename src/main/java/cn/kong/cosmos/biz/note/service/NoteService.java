package cn.kong.cosmos.biz.note.service;

import cn.kong.cosmos.biz.note.dto.resp.NoteDetailDTO;
import cn.kong.cosmos.biz.note.dto.resp.NoteListDTO;
import cn.kong.cosmos.biz.note.dto.resp.NotePreviewDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 技术笔记服务接口
 */
public interface NoteService {
    
    /**
     * 获取笔记列表
     */
    IPage<NoteListDTO> listNotes(Integer page, Integer size, String category, String tag);
    
    /**
     * 获取笔记详情
     */
    NoteDetailDTO getNoteDetail(String noteId);
    
    /**
     * 获取笔记预览
     */
    NotePreviewDTO getNotePreview(String noteId);
}
