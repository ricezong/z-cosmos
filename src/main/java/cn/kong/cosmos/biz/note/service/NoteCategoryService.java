package cn.kong.cosmos.biz.note.service;

import cn.kong.cosmos.biz.note.dto.resp.NoteCategoryDTO;
import java.util.List;

/**
 * 笔记分类服务接口
 */
public interface NoteCategoryService {
    
    /**
     * 获取所有启用的分类
     */
    List<NoteCategoryDTO> listCategories();
}
