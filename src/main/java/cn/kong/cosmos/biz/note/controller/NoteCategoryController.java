package cn.kong.cosmos.biz.note.controller;

import cn.kong.cosmos.biz.note.dto.resp.NoteCategoryDTO;
import cn.kong.cosmos.biz.note.service.NoteCategoryService;
import cn.kong.cosmos.common.core.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 笔记分类控制器
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class NoteCategoryController {
    
    private final NoteCategoryService noteCategoryService;
    
    /**
     * 获取所有分类
     */
    @GetMapping("/list")
    public Result<List<NoteCategoryDTO>> listCategories() {
        List<NoteCategoryDTO> result = noteCategoryService.listCategories();
        return Result.success(result);
    }
}
