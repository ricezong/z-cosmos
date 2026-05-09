package cn.kong.cosmos.biz.hot.controller;

import cn.kong.cosmos.biz.hot.dto.resp.HotTopicDTO;
import cn.kong.cosmos.biz.hot.service.HotTopicService;
import cn.kong.cosmos.common.core.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 热点话题控制器
 */
@RestController
@RequestMapping("/api/hot/topics")
@RequiredArgsConstructor
public class HotTopicController {
    
    private final HotTopicService hotTopicService;
    
    /**
     * 获取热点列表
     */
    @GetMapping
    public Result<IPage<HotTopicDTO>> listHotTopics(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category) {
        return Result.success(hotTopicService.listHotTopics(page, size, category));
    }
    
    /**
     * 获取热点详情
     */
    @GetMapping("/{id}")
    public Result<HotTopicDTO> getHotTopicDetail(@PathVariable String id) {
        return Result.success(hotTopicService.getHotTopicDetail(id));
    }
}
