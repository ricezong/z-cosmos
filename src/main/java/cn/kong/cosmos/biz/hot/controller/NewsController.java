package cn.kong.cosmos.biz.hot.controller;

import cn.kong.cosmos.biz.hot.dto.resp.NewsDetailDTO;
import cn.kong.cosmos.biz.hot.dto.resp.NewsSummaryDTO;
import cn.kong.cosmos.biz.hot.service.NewsService;
import cn.kong.cosmos.common.core.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 热点/新闻 Controller
 * 全部端点公开（不强制登录）
 */
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    /** 新闻列表 */
    @GetMapping
    public Result<IPage<NewsSummaryDTO>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(newsService.listNews(category, keyword, sort, page, size));
    }

    /** 新闻详情 */
    @GetMapping("/{newsId}")
    public Result<NewsDetailDTO> detail(@PathVariable String newsId) {
        return Result.success(newsService.getDetail(newsId));
    }

    /** 热门榜单 */
    @GetMapping("/top")
    public Result<List<NewsSummaryDTO>> top(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(newsService.topHot(limit));
    }

    /** 按时段的热门排行（对齐 api-design.md） */
    @GetMapping("/ranking")
    public Result<List<NewsSummaryDTO>> ranking(
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(newsService.ranking(period, limit));
    }
}
