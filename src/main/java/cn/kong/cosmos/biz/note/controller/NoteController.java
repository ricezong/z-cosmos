package cn.kong.cosmos.biz.note.controller;

import cn.kong.cosmos.biz.note.dto.resp.NoteDetailDTO;
import cn.kong.cosmos.biz.note.dto.resp.NoteListDTO;
import cn.kong.cosmos.biz.note.dto.resp.NotePreviewDTO;
import cn.kong.cosmos.biz.note.service.NoteService;
import cn.kong.cosmos.auth.service.AuthUnlockService;
import cn.kong.cosmos.common.core.Result;
import cn.kong.cosmos.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 技术笔记控制器
 */
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {
    
    private final NoteService noteService;
    private final AuthUnlockService authUnlockService;
    
    /**
     * 获取笔记列表
     */
    @GetMapping
    public Result<IPage<NoteListDTO>> listNotes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag) {
        return Result.success(noteService.listNotes(page, size, category, tag));
    }
    
    /**
     * 获取笔记详情（需验证解锁状态）
     */
    @GetMapping("/{id}")
    public Result<NoteDetailDTO> getNoteDetail(
            @PathVariable String id,
            @RequestParam(required = false) String deviceId) {
        // 检查笔记是否需要解锁
        NotePreviewDTO previewDTO = noteService.getNotePreview(id);
        if (previewDTO.getNote() != null && previewDTO.getNote().getIsLocked() != null 
                && previewDTO.getNote().getIsLocked() == 1) {
            // 需要解锁，验证设备解锁状态
            if (deviceId == null || deviceId.isEmpty()) {
                throw new BusinessException("此笔记需要解锁后查看，请先完成解锁");
            }
            var unlockStatus = authUnlockService.checkUnlockStatus(deviceId, "NOTE");
            if (!Boolean.TRUE.equals(unlockStatus.getUnlocked())) {
                throw new BusinessException("此笔记需要解锁后查看，请先完成解锁");
            }
        }
        return Result.success(noteService.getNoteDetail(id));
    }
    
    /**
     * 获取笔记预览
     */
    @GetMapping("/{id}/preview")
    public Result<NotePreviewDTO> getNotePreview(@PathVariable String id) {
        return Result.success(noteService.getNotePreview(id));
    }
}
