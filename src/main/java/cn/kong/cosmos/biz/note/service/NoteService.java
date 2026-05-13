package cn.kong.cosmos.biz.note.service;

import cn.kong.cosmos.biz.note.dto.resp.NoteCategoryDTO;
import cn.kong.cosmos.biz.note.dto.resp.NoteDetailDTO;
import cn.kong.cosmos.biz.note.dto.resp.NoteListDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 笔记服务接口
 */
public interface NoteService {
    
    /**
     * 分页查询笔记列表
     */
    Page<NoteListDTO> listNotes(Integer page, Integer size, String categoryCode);
    
    /**
     * 获取笔记详情（根据解锁状态返回预览或全文）
     */
    NoteDetailDTO getNoteDetail(String noteId, String deviceId);
    
    /**
     * 获取所有启用的分类
     */
    List<NoteCategoryDTO> listCategories();
}
