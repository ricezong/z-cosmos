package cn.kong.cosmos.biz.note.service.impl;

import cn.kong.cosmos.biz.note.dto.resp.NoteDetailDTO;
import cn.kong.cosmos.biz.note.dto.resp.NoteListDTO;
import cn.kong.cosmos.biz.note.dto.resp.NotePreviewDTO;
import cn.kong.cosmos.biz.note.entity.Note;
import cn.kong.cosmos.biz.note.mapper.NoteMapper;
import cn.kong.cosmos.biz.note.service.NoteService;
import cn.kong.cosmos.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 技术笔记服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    
    private final NoteMapper noteMapper;
    
    @Override
    public IPage<NoteListDTO> listNotes(Integer page, Integer size, String category, String tag) {
        Page<Note> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Note::getUpdatedAt);
        
        if (category != null && !category.isEmpty()) {
            queryWrapper.eq(Note::getCategory, category);
        }
        
        // 标签查询（JSON数组字段，使用 MySQL JSON_CONTAINS 函数）
        if (tag != null && !tag.isEmpty()) {
            queryWrapper.apply("JSON_CONTAINS(tags, JSON_QUOTE({0}))", tag);
        }
        
        IPage<Note> notePage = noteMapper.selectPage(pageParam, queryWrapper);
        return notePage.convert(this::toListDTO);
    }
    
    @Override
    public NoteDetailDTO getNoteDetail(String noteId) {
        Note note = noteMapper.selectOne(
            new LambdaQueryWrapper<Note>().eq(Note::getNoteId, noteId)
        );
        
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        
        // 增加阅读量
        note.setViewCount(note.getViewCount() + 1);
        noteMapper.updateById(note);
        
        return toDetailDTO(note);
    }
    
    @Override
    public NotePreviewDTO getNotePreview(String noteId) {
        Note note = noteMapper.selectOne(
            new LambdaQueryWrapper<Note>().eq(Note::getNoteId, noteId)
        );
        
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        
        // 根据preview_ratio动态提取预览内容
        String previewContent = extractPreview(note.getContent(), note.getPreviewRatio());
        return toPreviewDTO(note, previewContent);
    }
    
    /**
     * 根据比例提取预览内容
     */
    private String extractPreview(String content, BigDecimal ratio) {
        if (content == null) return "";
        
        double ratioValue = ratio != null ? ratio.doubleValue() : 0.30;
        int previewLength = (int) (content.length() * ratioValue);
        int endPos = Math.min(previewLength, content.length());
        
        String preview = content.substring(0, endPos);
        
        // 尝试在HTML标签边界处截断（避免破坏HTML结构）
        int lastTagEnd = preview.lastIndexOf('>');
        if (lastTagEnd > preview.length() * 0.8) {
            preview = preview.substring(0, lastTagEnd + 1);
        }
        
        return preview;
    }
    
    /**
     * 转换为列表DTO
     */
    private NoteListDTO toListDTO(Note note) {
        NoteListDTO dto = new NoteListDTO();
        dto.setNoteId(note.getNoteId());
        dto.setTitle(note.getTitle());
        dto.setShortSummary(note.getShortSummary());
        dto.setCategory(note.getCategory());
        dto.setTags(note.getTags());
        dto.setCoverImage(note.getCoverImage());
        dto.setViewCount(note.getViewCount());
        dto.setUpdatedAt(note.getUpdatedAt());
        return dto;
    }
    
    /**
     * 转换为详情DTO
     */
    private NoteDetailDTO toDetailDTO(Note note) {
        NoteDetailDTO dto = new NoteDetailDTO();
        dto.setNoteId(note.getNoteId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setCategory(note.getCategory());
        dto.setTags(note.getTags());
        dto.setCoverImage(note.getCoverImage());
        dto.setViewCount(note.getViewCount());
        dto.setIsLocked(note.getIsLocked());
        dto.setUpdatedAt(note.getUpdatedAt());
        return dto;
    }
    
    /**
     * 转换为预览DTO
     */
    private NotePreviewDTO toPreviewDTO(Note note, String previewContent) {
        NotePreviewDTO dto = new NotePreviewDTO();
        NotePreviewDTO.NoteInfoDTO noteInfo = new NotePreviewDTO.NoteInfoDTO();
        noteInfo.setNoteId(note.getNoteId());
        noteInfo.setTitle(note.getTitle());
        noteInfo.setIsLocked(note.getIsLocked());
        noteInfo.setPreviewRatio(note.getPreviewRatio());
        dto.setNote(noteInfo);
        dto.setPreviewContent(previewContent);
        return dto;
    }
}
