package cn.kong.cosmos.biz.note.controller;

import cn.kong.cosmos.biz.note.dto.resp.NoteCategoryDTO;
import cn.kong.cosmos.biz.note.service.NoteCategoryService;
import cn.kong.cosmos.common.core.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 笔记类别控制器
 */
@RestController
@RequestMapping("/api/note-categories")
@RequiredArgsConstructor
public class NoteCategoryController {
    
    private final NoteCategoryService noteCategoryService;
    
    /**
     * 获取所有启用的类别列表
     */
    @GetMapping("/enabled")
    public Result<List<NoteCategoryDTO>> listEnabledCategories() {
        return Result.success(noteCategoryService.listEnabledCategories());
    }
    
    /**
     * 获取所有类别列表
     */
    @GetMapping
    public Result<List<NoteCategoryDTO>> listAllCategories() {
        return Result.success(noteCategoryService.listAllCategories());
    }
    
    /**
     * 根据类别编码获取类别信息
     */
    @GetMapping("/{code}")
    public Result<NoteCategoryDTO> getCategoryByCode(@PathVariable String code) {
        return Result.success(noteCategoryService.getCategoryByCode(code));
    }
}