package cn.kong.cosmos.biz.note.service;

import cn.kong.cosmos.biz.note.dto.resp.NoteCategoryDTO;

import java.util.List;

/**
 * 笔记类别服务接口
 */
public interface NoteCategoryService {
    
    /**
     * 获取所有启用的类别列表
     */
    List<NoteCategoryDTO> listEnabledCategories();
    
    /**
     * 获取所有类别列表（包含禁用的）
     */
    List<NoteCategoryDTO> listAllCategories();
    
    /**
     * 根据类别编码获取类别信息
     */
    NoteCategoryDTO getCategoryByCode(String categoryCode);
    
    /**
     * 更新类别的笔记数量
     */
    void updateNoteCount(String categoryCode, Long count);
}