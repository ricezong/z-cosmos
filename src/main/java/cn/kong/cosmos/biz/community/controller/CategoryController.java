package cn.kong.cosmos.biz.community.controller;

import cn.kong.cosmos.biz.community.dto.resp.CategoryDTO;
import cn.kong.cosmos.biz.community.service.CategoryService;
import cn.kong.cosmos.common.core.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 社区分类 Controller
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /** 分类列表（公开，Redis 缓存） */
    @GetMapping
    public Result<List<CategoryDTO>> list() {
        return Result.success(categoryService.listAll());
    }
}
