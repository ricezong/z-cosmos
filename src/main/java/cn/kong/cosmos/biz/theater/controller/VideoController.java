package cn.kong.cosmos.biz.theater.controller;

import cn.kong.cosmos.biz.theater.dto.req.PlayReportReq;
import cn.kong.cosmos.biz.theater.dto.resp.EpisodeDTO;
import cn.kong.cosmos.biz.theater.dto.resp.VideoDetailDTO;
import cn.kong.cosmos.biz.theater.dto.resp.VideoSummaryDTO;
import cn.kong.cosmos.biz.theater.service.VideoService;
import cn.kong.cosmos.common.core.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 剧场视频 Controller
 * - 列表/详情/剧集端点公开
 * - 播放上报需登录
 */
@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    /** 视频列表 */
    @GetMapping
    public Result<IPage<VideoSummaryDTO>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        return Result.success(videoService.listVideos(category, keyword, sort, page, size));
    }

    /** 视频详情（含剧集列表） */
    @GetMapping("/{videoId}")
    public Result<VideoDetailDTO> detail(@PathVariable String videoId) {
        return Result.success(videoService.getDetail(videoId));
    }

    /** 独立获取剧集列表（用于前端剧集切换面板） */
    @GetMapping("/{videoId}/episodes")
    public Result<List<EpisodeDTO>> episodes(@PathVariable String videoId) {
        return Result.success(videoService.listEpisodes(videoId));
    }

    /**
     * 播放上报（登录态）
     * POST /api/videos/{videoId}/play  body: { "episodeNumber": 1 }
     */
    @PostMapping("/{videoId}/play")
    public Result<Map<String, Object>> play(@PathVariable String videoId,
                                            @RequestBody(required = false) PlayReportReq req) {
        Integer ep = req == null ? null : req.getEpisodeNumber();
        boolean counted = videoService.reportPlay(videoId, ep);
        Map<String, Object> data = new HashMap<>(2);
        data.put("counted", counted);
        return Result.success(data);
    }
}
