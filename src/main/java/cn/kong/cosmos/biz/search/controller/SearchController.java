package cn.kong.cosmos.biz.search.controller;

import cn.kong.cosmos.biz.search.dto.resp.GlobalSearchDTO;
import cn.kong.cosmos.biz.search.dto.resp.HotKeywordDTO;
import cn.kong.cosmos.biz.search.dto.resp.SearchItemDTO;
import cn.kong.cosmos.biz.search.service.SearchService;
import cn.kong.cosmos.common.core.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聚合搜索 Controller
 * 全部端点公开（不强制登录）
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 全局聚合搜索 - 首页搜索框
     * GET /api/search?q=关键词&topN=5
     */
    @GetMapping
    public Result<GlobalSearchDTO> global(
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "5") Integer topN) {
        return Result.success(searchService.globalSearch(keyword, topN));
    }

    /**
     * 按类型分页搜索 - 搜索结果页
     * GET /api/search/type?type=post|news|video&q=关键词&page=1&size=10
     */
    @GetMapping("/type")
    public Result<IPage<SearchItemDTO>> byType(
            @RequestParam(defaultValue = "post") String type,
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(searchService.searchByType(type, keyword, page, size));
    }

    /**
     * 热门搜索词 Top10
     * GET /api/search/hot-keywords?limit=10
     */
    @GetMapping("/hot-keywords")
    public Result<List<HotKeywordDTO>> hotKeywords(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(searchService.hotKeywords(limit));
    }

    /**
     * 输入建议
     * GET /api/search/suggestions?q=关键词&limit=10
     */
    @GetMapping("/suggestions")
    public Result<List<String>> suggestions(
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(searchService.suggestions(keyword, limit));
    }
}
