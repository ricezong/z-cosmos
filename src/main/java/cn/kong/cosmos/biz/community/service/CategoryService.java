package cn.kong.cosmos.biz.community.service;

import cn.kong.cosmos.biz.community.dto.resp.CategoryDTO;
import cn.kong.cosmos.biz.community.entity.Category;

import java.util.List;

/**
 * 分类服务
 */
public interface CategoryService {

    /** 分类列表（Redis 缓存） */
    List<CategoryDTO> listAll();

    /** 根据 code 查询单个分类（用于校验存在性） */
    Category findByCode(String code);
}
