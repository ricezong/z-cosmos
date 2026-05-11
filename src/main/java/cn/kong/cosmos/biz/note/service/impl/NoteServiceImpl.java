package cn.kong.cosmos.biz.note.service.impl;

import cn.kong.cosmos.auth.entity.AuthUnlock;
import cn.kong.cosmos.auth.mapper.AuthUnlockMapper;
import cn.kong.cosmos.biz.note.dto.resp.NoteCategoryDTO;
import cn.kong.cosmos.biz.note.dto.resp.NoteDetailDTO;
import cn.kong.cosmos.biz.note.dto.resp.NoteListDTO;
import cn.kong.cosmos.biz.note.entity.Note;
import cn.kong.cosmos.biz.note.entity.NoteCategory;
import cn.kong.cosmos.biz.note.entity.NoteContent;
import cn.kong.cosmos.biz.note.mapper.NoteMapper;
import cn.kong.cosmos.biz.note.mapper.NoteCategoryMapper;
import cn.kong.cosmos.biz.note.mapper.NoteContentMapper;
import cn.kong.cosmos.biz.note.service.NoteService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 笔记服务实现
 */
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    
    private final NoteMapper noteMapper;
    private final NoteContentMapper noteContentMapper;
    private final NoteCategoryMapper categoryMapper;
    private final AuthUnlockMapper authUnlockMapper;
    
    @Override
    public Page<NoteListDTO> listNotes(Integer page, Integer size, String categoryCode) {
        Page<Note> notePage = new Page<>(page, size);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Note::getCreatedAt);
        
        if (categoryCode != null && !categoryCode.isEmpty()) {
            wrapper.eq(Note::getCategoryCode, categoryCode);
        }
        
        Page<Note> result = noteMapper.selectPage(notePage, wrapper);
        
        List<NoteListDTO> dtoList = result.getRecords().stream().map(note -> {
            NoteListDTO dto = new NoteListDTO();
            dto.setNoteId(note.getNoteId());
            dto.setTitle(note.getTitle());
            dto.setCategoryCode(note.getCategoryCode());
            dto.setTags(note.getTags());
            dto.setContentType(note.getContentType());
            dto.setSourceUrl(note.getSourceUrl());
            dto.setReadMinutes(note.getReadMinutes());
            dto.setIsLocked(note.getIsLocked());
            dto.setShortSummary(note.getShortSummary());
            dto.setViewCount(note.getViewCount());
            dto.setCreatedAt(note.getCreatedAt() != null ? note.getCreatedAt().toString() : null);
            
            // 查询分类名称
            NoteCategory category = categoryMapper.selectOne(
                new LambdaQueryWrapper<NoteCategory>().eq(NoteCategory::getCategoryCode, note.getCategoryCode())
            );
            if (category != null) {
                dto.setCategoryName(category.getCategoryName());
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        Page<NoteListDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }
    
    @Override
    public NoteDetailDTO getNoteDetail(String noteId, String deviceId) {
        // 查询笔记元数据
        Note note = noteMapper.selectOne(new LambdaQueryWrapper<Note>().eq(Note::getNoteId, noteId));
        if (note == null) {
            throw new RuntimeException("笔记不存在");
        }
        
        // 查询笔记内容
        NoteContent content = noteContentMapper.selectById(noteId);
        if (content == null) {
            throw new RuntimeException("笔记内容为空");
        }
        
        // 检查解锁状态
        boolean isUnlocked = checkUnlockStatus(deviceId, note.getIsLocked() == 1);
        
        // 构建详情 DTO
        NoteDetailDTO dto = new NoteDetailDTO();
        dto.setNoteId(note.getNoteId());
        dto.setTitle(note.getTitle());
        dto.setCategoryCode(note.getCategoryCode());
        dto.setTags(note.getTags());
        dto.setContentType(note.getContentType());
        dto.setSourceUrl(note.getSourceUrl());
        dto.setReadMinutes(note.getReadMinutes());
        dto.setIsLocked(note.getIsLocked());
        dto.setShortSummary(note.getShortSummary());
        dto.setViewCount(note.getViewCount());
        dto.setUnlocked(isUnlocked);
        dto.setCreatedAt(note.getCreatedAt() != null ? note.getCreatedAt().toString() : null);
        dto.setUpdatedAt(note.getUpdatedAt() != null ? note.getUpdatedAt().toString() : null);
        
        // 查询分类名称
        NoteCategory category = categoryMapper.selectOne(
            new LambdaQueryWrapper<NoteCategory>().eq(NoteCategory::getCategoryCode, note.getCategoryCode())
        );
        if (category != null) {
            dto.setCategoryName(category.getCategoryName());
        }
        
        // 根据解锁状态返回预览或全文
        String fullContent = content.getContent();
        if (note.getIsLocked() == 1 && !isUnlocked) {
            // 未解锁，返回预览内容
            dto.setPreviewContent(generatePreview(fullContent, note.getPreviewType(), note.getPreviewLimit()));
            dto.setFullContent(null);
        } else {
            // 已解锁或公开笔记，返回全文
            dto.setPreviewContent(null);
            dto.setFullContent(fullContent);
        }
        
        // 增加阅读数
        note.setViewCount(note.getViewCount() + 1);
        noteMapper.updateById(note);
        
        return dto;
    }
    
    @Override
    public List<NoteCategoryDTO> listCategories() {
        LambdaQueryWrapper<NoteCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteCategory::getIsEnabled, 1)
               .orderByDesc(NoteCategory::getSortOrder);
        
        List<NoteCategory> categories = categoryMapper.selectList(wrapper);
        return categories.stream().map(cat -> {
            NoteCategoryDTO dto = new NoteCategoryDTO();
            dto.setCategoryCode(cat.getCategoryCode());
            dto.setCategoryName(cat.getCategoryName());
            dto.setDescription(cat.getDescription());
            dto.setIconUrl(cat.getIconUrl());
            dto.setNoteCount(cat.getNoteCount());
            return dto;
        }).collect(Collectors.toList());
    }
    
    /**
     * 检查设备是否已解锁
     */
    private boolean checkUnlockStatus(String deviceId, boolean needUnlock) {
        if (!needUnlock) {
            return true; // 公开笔记不需要解锁
        }
        if (deviceId == null || deviceId.isEmpty()) {
            return false;
        }
        
        AuthUnlock unlock = authUnlockMapper.selectOne(
            new LambdaQueryWrapper<AuthUnlock>()
                .eq(AuthUnlock::getDeviceId, deviceId)
                .eq(AuthUnlock::getModuleType, "NOTE")
                .eq(AuthUnlock::getStatus, 1)
                .gt(AuthUnlock::getExpiresAt, LocalDateTime.now())
        );
        
        return unlock != null;
    }
    
    /**
     * 生成预览内容
     */
    private String generatePreview(String content, Integer previewType, Integer previewLimit) {
        if (content == null) {
            return "";
        }
        
        // 查找分隔符 <!-- more -->
        int moreIndex = content.indexOf("<!-- more -->");
        if (moreIndex != -1) {
            return content.substring(0, moreIndex);
        }
        
        // 按字数截取
        if (previewLimit > 0 && content.length() > previewLimit) {
            return content.substring(0, previewLimit) + "...";
        }
        
        return content;
    }
}
