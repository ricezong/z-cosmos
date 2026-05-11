package cn.kong.cosmos.biz.note.service.impl;

import cn.kong.cosmos.biz.note.dto.resp.NoteCategoryDTO;
import cn.kong.cosmos.biz.note.entity.NoteCategory;
import cn.kong.cosmos.biz.note.mapper.NoteCategoryMapper;
import cn.kong.cosmos.biz.note.service.NoteCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 笔记分类服务实现
 */
@Service
@RequiredArgsConstructor
public class NoteCategoryServiceImpl implements NoteCategoryService {
    
    private final NoteCategoryMapper categoryMapper;
    
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
}
