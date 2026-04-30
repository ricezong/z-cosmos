package cn.kong.cosmos.biz.theater.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.kong.cosmos.auth.util.CurrentUserContext;
import cn.kong.cosmos.biz.theater.dto.resp.EpisodeDTO;
import cn.kong.cosmos.biz.theater.dto.resp.VideoDetailDTO;
import cn.kong.cosmos.biz.theater.dto.resp.VideoSummaryDTO;
import cn.kong.cosmos.biz.theater.entity.Video;
import cn.kong.cosmos.biz.theater.entity.VideoEpisode;
import cn.kong.cosmos.biz.theater.mapper.VideoEpisodeMapper;
import cn.kong.cosmos.biz.theater.mapper.VideoMapper;
import cn.kong.cosmos.biz.theater.service.VideoService;
import cn.kong.cosmos.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 视频服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoMapper videoMapper;
    private final VideoEpisodeMapper videoEpisodeMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PLAY_DEDUP_KEY = "biz:video:played:%s:%s";
    private static final Duration PLAY_DEDUP_TTL = Duration.ofMinutes(10);

    @Override
    public IPage<VideoSummaryDTO> listVideos(String category, String keyword, String sort,
                                             Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 12 : Math.min(size, 50);

        Page<Video> pager = new Page<>(p, s);
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getStatus, 1);
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(Video::getCategory, category);
        }
        if (StrUtil.isNotBlank(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Video::getTitle, kw)
                    .or().like(Video::getDirector, kw)
                    .or().like(Video::getActors, kw));
        }
        switch (sort == null ? "latest" : sort.toLowerCase()) {
            case "hot":
                wrapper.orderByDesc(Video::getViewCount);
                break;
            case "rating":
                wrapper.orderByDesc(Video::getRating).orderByDesc(Video::getViewCount);
                break;
            case "latest":
            default:
                wrapper.orderByDesc(Video::getCreatedAt);
        }

        IPage<Video> result = videoMapper.selectPage(pager, wrapper);
        return result.convert(this::toSummary);
    }

    @Override
    public VideoDetailDTO getDetail(String videoId) {
        Video video = videoMapper.selectOne(new LambdaQueryWrapper<Video>()
                .eq(Video::getVideoId, videoId));
        if (video == null || (video.getStatus() != null && video.getStatus() == 0)) {
            throw BusinessException.notFound("视频不存在或已下架");
        }

        try {
            videoMapper.update(null, new LambdaUpdateWrapper<Video>()
                    .eq(Video::getVideoId, videoId)
                    .setSql("view_count = view_count + 1"));
        } catch (Exception e) {
            log.warn("视频浏览数自增失败: {}", e.getMessage());
        }

        VideoDetailDTO dto = new VideoDetailDTO();
        fillSummary(dto, video);
        dto.setDescription(video.getDescription());
        dto.setDirector(video.getDirector());
        dto.setActors(splitActors(video.getActors()));
        dto.setEpisodes(listEpisodes(videoId));
        return dto;
    }

    @Override
    public List<EpisodeDTO> listEpisodes(String videoId) {
        LambdaQueryWrapper<VideoEpisode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VideoEpisode::getVideoId, videoId)
                .orderByAsc(VideoEpisode::getEpisodeNumber);
        List<VideoEpisode> list = videoEpisodeMapper.selectList(wrapper);
        return list.stream().map(this::toEpisodeDTO).collect(Collectors.toList());
    }

    @Override
    public boolean reportPlay(String videoId, Integer episodeNumber) {
        String userId = CurrentUserContext.getUserIdStr();
        if (StrUtil.isBlank(userId)) {
            throw BusinessException.unauthorized("请先登录");
        }

        Video video = videoMapper.selectOne(new LambdaQueryWrapper<Video>()
                .eq(Video::getVideoId, videoId));
        if (video == null || (video.getStatus() != null && video.getStatus() == 0)) {
            throw BusinessException.notFound("视频不存在或已下架");
        }

        // 剧集存在性校验（可选）
        if (episodeNumber != null && episodeNumber > 0) {
            Long cnt = videoEpisodeMapper.selectCount(new LambdaQueryWrapper<VideoEpisode>()
                    .eq(VideoEpisode::getVideoId, videoId)
                    .eq(VideoEpisode::getEpisodeNumber, episodeNumber));
            if (cnt == null || cnt == 0) {
                throw BusinessException.notFound("剧集不存在");
            }
        }

        // Redis 防刷：10 分钟内同用户同视频仅计一次
        String key = String.format(PLAY_DEDUP_KEY, videoId, userId);
        try {
            Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, 1, PLAY_DEDUP_TTL);
            if (Boolean.FALSE.equals(ok)) {
                return false;
            }
        } catch (Exception e) {
            log.warn("播放防刷键写入失败，放行上报: {}", e.getMessage());
        }

        try {
            videoMapper.update(null, new LambdaUpdateWrapper<Video>()
                    .eq(Video::getVideoId, videoId)
                    .setSql("view_count = view_count + 1"));
        } catch (Exception e) {
            log.warn("视频播放数自增失败: {}", e.getMessage());
        }
        return true;
    }

    // ================ private ================

    private VideoSummaryDTO toSummary(Video video) {
        VideoSummaryDTO dto = new VideoSummaryDTO();
        fillSummary(dto, video);
        return dto;
    }

    private void fillSummary(VideoSummaryDTO dto, Video v) {
        dto.setVideoId(v.getVideoId());
        dto.setCategory(v.getCategory());
        dto.setTitle(v.getTitle());
        dto.setCoverImage(v.getCoverImage());
        dto.setTotalEpisodes(v.getTotalEpisodes());
        dto.setRating(v.getRating());
        dto.setViewCount(v.getViewCount());
        dto.setStatus(v.getStatus());
    }

    private EpisodeDTO toEpisodeDTO(VideoEpisode e) {
        EpisodeDTO dto = new EpisodeDTO();
        dto.setEpisodeNumber(e.getEpisodeNumber());
        dto.setTitle(e.getTitle());
        dto.setDuration(e.getDuration());
        dto.setPlayUrl(e.getPlayUrl());
        dto.setSourceType(e.getSourceType());
        dto.setCoverImage(e.getCoverImage());
        return dto;
    }

    private List<String> splitActors(String actors) {
        if (StrUtil.isBlank(actors)) {
            return Collections.emptyList();
        }
        return Arrays.stream(actors.split("[,，、]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
