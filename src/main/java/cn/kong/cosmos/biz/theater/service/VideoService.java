package cn.kong.cosmos.biz.theater.service;

import cn.kong.cosmos.biz.theater.dto.resp.EpisodeDTO;
import cn.kong.cosmos.biz.theater.dto.resp.VideoDetailDTO;
import cn.kong.cosmos.biz.theater.dto.resp.VideoSummaryDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 剧场视频服务
 */
public interface VideoService {

    /**
     * 分页视频列表
     * @param category 分类可选
     * @param keyword  关键词可选
     * @param sort     latest / hot（viewCount） / rating
     */
    IPage<VideoSummaryDTO> listVideos(String category, String keyword, String sort,
                                      Integer page, Integer size);

    /**
     * 视频详情（含剧集列表，副作用：view_count + 1）
     */
    VideoDetailDTO getDetail(String videoId);

    /**
     * 独立获取剧集列表（用于剧集切换）
     */
    List<EpisodeDTO> listEpisodes(String videoId);

    /**
     * 上报播放（登录态）
     * - Redis 防刷（10分钟内同用户同视频仅计一次）
     * - 命中新增：view_count + 1
     * @return true = 有效上报，false = 防刷拦截
     */
    boolean reportPlay(String videoId, Integer episodeNumber);
}
