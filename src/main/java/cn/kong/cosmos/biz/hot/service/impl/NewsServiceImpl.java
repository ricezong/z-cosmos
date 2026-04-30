package cn.kong.cosmos.biz.hot.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.kong.cosmos.biz.hot.dto.resp.NewsDetailDTO;
import cn.kong.cosmos.biz.hot.dto.resp.NewsSummaryDTO;
import cn.kong.cosmos.biz.hot.entity.News;
import cn.kong.cosmos.biz.hot.mapper.NewsMapper;
import cn.kong.cosmos.biz.hot.service.NewsService;
import cn.kong.cosmos.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 新闻服务实现
 * - 列表/详情读 MySQL
 * - 热门榜单使用 Redis 缓存
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private static final String CACHE_KEY_TOP_HOT = "biz:news:top_hot:%d";
    private static final String CACHE_KEY_RANKING = "biz:news:ranking:%s:%d";

    private final NewsMapper newsMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${community.cache.ttl:3600}")
    private long cacheTtlSeconds;

    @Override
    public IPage<NewsSummaryDTO> listNews(String category, String keyword, String sort,
                                          Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : Math.min(size, 50);

        Page<News> pager = new Page<>(p, s);
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(News::getCategory, category);
        }
        if (StrUtil.isNotBlank(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(News::getTitle, kw).or().like(News::getSummary, kw));
        }
        if ("hot".equalsIgnoreCase(sort)) {
            wrapper.orderByDesc(News::getHotScore).orderByDesc(News::getPublishedAt);
        } else {
            wrapper.orderByDesc(News::getPublishedAt);
        }

        IPage<News> result = newsMapper.selectPage(pager, wrapper);
        return result.convert(this::toSummary);
    }

    @Override
    public NewsDetailDTO getDetail(String newsId) {
        News news = newsMapper.selectOne(new LambdaQueryWrapper<News>()
                .eq(News::getNewsId, newsId));
        if (news == null) {
            throw BusinessException.notFound("新闻不存在");
        }

        // view_count + 1（失败不影响主流程）
        try {
            newsMapper.update(null, new LambdaUpdateWrapper<News>()
                    .eq(News::getNewsId, newsId)
                    .setSql("view_count = view_count + 1"));
        } catch (Exception e) {
            log.warn("新闻浏览数自增失败: {}", e.getMessage());
        }

        NewsDetailDTO dto = new NewsDetailDTO();
        fillSummary(dto, news);
        dto.setContent(news.getContent());
        return dto;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<NewsSummaryDTO> topHot(Integer limit) {
        int n = limit == null || limit < 1 ? 10 : Math.min(limit, 50);
        String key = String.format(CACHE_KEY_TOP_HOT, n);

        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof List<?> list && !list.isEmpty()) {
                return new com.fasterxml.jackson.databind.ObjectMapper()
                        .convertValue(list,
                                new com.fasterxml.jackson.core.type.TypeReference<List<NewsSummaryDTO>>() {});
            }
        } catch (Exception e) {
            log.warn("读取热门新闻缓存失败，降级查库: {}", e.getMessage());
        }

        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(News::getHotScore)
                .orderByDesc(News::getPublishedAt)
                .last("LIMIT " + n);
        List<News> list = newsMapper.selectList(wrapper);
        List<NewsSummaryDTO> dtos = list.stream().map(this::toSummary).collect(Collectors.toList());

        try {
            redisTemplate.opsForValue().set(key, dtos, Duration.ofSeconds(Math.min(cacheTtlSeconds, 600)));
        } catch (Exception e) {
            log.warn("写入热门新闻缓存失败: {}", e.getMessage());
        }
        return dtos;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<NewsSummaryDTO> ranking(String period, Integer limit) {
        String p = normalizePeriod(period);
        int n = limit == null || limit < 1 ? 10 : Math.min(limit, 50);
        String key = String.format(CACHE_KEY_RANKING, p, n);

        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof List<?> list && !list.isEmpty()) {
                return new com.fasterxml.jackson.databind.ObjectMapper()
                        .convertValue(list,
                                new com.fasterxml.jackson.core.type.TypeReference<List<NewsSummaryDTO>>() {});
            }
        } catch (Exception e) {
            log.warn("读取新闻排行缓存失败，降级查库: {}", e.getMessage());
        }

        LocalDateTime since = computeSince(p);
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        if (since != null) {
            wrapper.ge(News::getPublishedAt, since);
        }
        wrapper.orderByDesc(News::getHotScore)
                .orderByDesc(News::getPublishedAt)
                .last("LIMIT " + n);
        List<News> list = newsMapper.selectList(wrapper);
        List<NewsSummaryDTO> dtos = list.stream().map(this::toSummary).collect(Collectors.toList());

        try {
            redisTemplate.opsForValue().set(key, dtos, Duration.ofSeconds(Math.min(cacheTtlSeconds, 600)));
        } catch (Exception e) {
            log.warn("写入新闻排行缓存失败: {}", e.getMessage());
        }
        return dtos;
    }

    // ================ private ================

    private String normalizePeriod(String period) {
        if (StrUtil.isBlank(period)) return "day";
        String p = period.trim().toLowerCase();
        return switch (p) {
            case "day", "week", "month" -> p;
            default -> "day";
        };
    }

    private LocalDateTime computeSince(String period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period) {
            case "week" -> now.minusDays(7);
            case "month" -> now.minusDays(30);
            default -> now.minusDays(1);
        };
    }
    private NewsSummaryDTO toSummary(News news) {
        NewsSummaryDTO dto = new NewsSummaryDTO();
        fillSummary(dto, news);
        return dto;
    }

    private void fillSummary(NewsSummaryDTO dto, News news) {
        dto.setNewsId(news.getNewsId());
        dto.setCategory(news.getCategory());
        dto.setTitle(news.getTitle());
        dto.setSummary(news.getSummary());
        dto.setCoverImage(news.getCoverImage());
        dto.setSource(news.getSource());
        dto.setSourceUrl(news.getSourceUrl());
        dto.setViewCount(news.getViewCount());
        dto.setHotScore(news.getHotScore());
        dto.setPublishedAt(news.getPublishedAt());
    }
}
