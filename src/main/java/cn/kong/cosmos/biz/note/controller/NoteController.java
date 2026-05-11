package cn.kong.cosmos.biz.note.controller;

import cn.kong.cosmos.biz.note.dto.resp.NoteCategoryDTO;
import cn.kong.cosmos.biz.note.dto.resp.NoteDetailDTO;
import cn.kong.cosmos.biz.note.dto.resp.NoteListDTO;
import cn.kong.cosmos.biz.note.service.NoteService;
import cn.kong.cosmos.common.core.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记控制器
 */
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {
    
    private final NoteService noteService;
    
    /**
     * 分页查询笔记列表
     */
    @GetMapping("/list")
    public Result<Page<NoteListDTO>> listNotes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String categoryCode
    ) {
        Page<NoteListDTO> result = noteService.listNotes(page, size, categoryCode);
        return Result.success(result);
    }
    
    /**
     * 获取笔记详情
     */
    @GetMapping("/{noteId}")
    public Result<NoteDetailDTO> getNoteDetail(
            @PathVariable String noteId,
            @RequestHeader(value = "X-Device-ID", required = false) String deviceId
    ) {
        NoteDetailDTO result = noteService.getNoteDetail(noteId, deviceId);
        return Result.success(result);
    }
    
    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public Result<List<NoteCategoryDTO>> listCategories() {
        List<NoteCategoryDTO> result = noteService.listCategories();
        return Result.success(result);
    }
}
