package cn.kong.cosmos.biz.note.service.impl;

import cn.kong.cosmos.biz.note.dto.resp.NoteCategoryDTO;
import cn.kong.cosmos.biz.note.entity.NoteCategory;
import cn.kong.cosmos.biz.note.mapper.NoteCategoryMapper;
import cn.kong.cosmos.biz.note.service.NoteCategoryService;
import cn.kong.cosmos.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 笔记类别服务实现类
 */
@Service
@RequiredArgsConstructor
public class NoteCategoryServiceImpl implements NoteCategoryService {
    
    private final NoteCategoryMapper noteCategoryMapper;
    
    @Override
    public List<NoteCategoryDTO> listEnabledCategories() {
        LambdaQueryWrapper<NoteCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteCategory::getIsEnabled, 1)
                   .orderByAsc(NoteCategory::getSortOrder);
        
        List<NoteCategory> categories = noteCategoryMapper.selectList(queryWrapper);
        return categories.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<NoteCategoryDTO> listAllCategories() {
        LambdaQueryWrapper<NoteCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(NoteCategory::getSortOrder);
        
        List<NoteCategory> categories = noteCategoryMapper.selectList(queryWrapper);
        return categories.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public NoteCategoryDTO getCategoryByCode(String categoryCode) {
        LambdaQueryWrapper<NoteCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteCategory::getCategoryCode, categoryCode);
        
        NoteCategory category = noteCategoryMapper.selectOne(queryWrapper);
        if (category == null) {
            throw new BusinessException("类别不存在");
        }
        
        return toDTO(category);
    }
    
    @Override
    public void updateNoteCount(String categoryCode, Long count) {
        LambdaQueryWrapper<NoteCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteCategory::getCategoryCode, categoryCode);
        
        NoteCategory category = noteCategoryMapper.selectOne(queryWrapper);
        if (category != null) {
            category.setNoteCount(count);
            noteCategoryMapper.updateById(category);
        }
    }
    
    /**
     * 转换为DTO
     */
    private NoteCategoryDTO toDTO(NoteCategory category) {
        NoteCategoryDTO dto = new NoteCategoryDTO();
        BeanUtils.copyProperties(category, dto);
        return dto;
    }
}