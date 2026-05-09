package cn.kong.cosmos.biz.search.controller;

import cn.kong.cosmos.biz.search.dto.SearchResultDTO;
import cn.kong.cosmos.biz.search.service.SearchService;
import cn.kong.cosmos.common.core.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索控制器
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    
    private final SearchService searchService;
    
    /**
     * 全局搜索
     */
    @GetMapping
    public Result<IPage<SearchResultDTO>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(searchService.search(keyword, type, page, size));
    }
}
