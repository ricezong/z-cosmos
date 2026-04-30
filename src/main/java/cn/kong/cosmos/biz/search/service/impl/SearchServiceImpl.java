package cn.kong.cosmos.biz.search.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.kong.cosmos.biz.community.entity.Post;
import cn.kong.cosmos.biz.community.mapper.PostMapper;
import cn.kong.cosmos.biz.hot.entity.News;
import cn.kong.cosmos.biz.hot.mapper.NewsMapper;
import cn.kong.cosmos.biz.search.dto.resp.GlobalSearchDTO;
import cn.kong.cosmos.biz.search.dto.resp.HotKeywordDTO;
import cn.kong.cosmos.biz.search.dto.resp.SearchItemDTO;
import cn.kong.cosmos.biz.search.service.SearchService;
import cn.kong.cosmos.biz.theater.entity.Video;
import cn.kong.cosmos.biz.theater.mapper.VideoMapper;
import cn.kong.cosmos.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 聚合搜索服务实现
 * - 帖子：仅 status=1（已发布）
 * - 新闻：按发布时间倒序
 * - 视频：仅 status=1（正常上架）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final int MAX_TOP_N = 20;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MIN_HOT_KW_LEN = 2;
    private static final int MAX_HOT_KW_LEN = 32;
    private static final String HOT_KEYWORD_ZSET = "biz:search:hot_keywords";

    private final PostMapper postMapper;
    private final NewsMapper newsMapper;
    private final VideoMapper videoMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public GlobalSearchDTO globalSearch(String keyword, Integer topN) {
        GlobalSearchDTO resp = new GlobalSearchDTO();
        resp.setKeyword(keyword);
        if (StrUtil.isBlank(keyword)) {
            resp.setPosts(Collections.emptyList());
            resp.setNews(Collections.emptyList());
            resp.setVideos(Collections.emptyList());
            resp.setTotal(0);
            return resp;
        }
        int n = topN == null || topN < 1 ? 5 : Math.min(topN, MAX_TOP_N);
        String kw = keyword.trim();

        List<SearchItemDTO> posts = searchPosts(kw, n);
        List<SearchItemDTO> news = searchNews(kw, n);
        List<SearchItemDTO> videos = searchVideos(kw, n);

        resp.setPosts(posts);
        resp.setNews(news);
        resp.setVideos(videos);
        resp.setTotal(posts.size() + news.size() + videos.size());
        recordKeyword(kw);
        return resp;
    }

    @Override
    public IPage<SearchItemDTO> searchByType(String type, String keyword, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : Math.min(size, MAX_PAGE_SIZE);
        if (StrUtil.isBlank(keyword)) {
            return new Page<>(p, s, 0);
        }
        String kw = keyword.trim();
        recordKeyword(kw);
        if (StrUtil.isBlank(type) || "post".equalsIgnoreCase(type)) {
            return pagePosts(kw, p, s);
        }
        if ("news".equalsIgnoreCase(type)) {
            return pageNews(kw, p, s);
        }
        if ("video".equalsIgnoreCase(type)) {
            return pageVideos(kw, p, s);
        }
        throw BusinessException.badRequest("不支持的搜索类型: " + type);
    }

    @Override
    public List<HotKeywordDTO> hotKeywords(Integer limit) {
        int n = limit == null || limit < 1 ? 10 : Math.min(limit, MAX_TOP_N);
        try {
            Set<ZSetOperations.TypedTuple<Object>> tuples =
                    redisTemplate.opsForZSet().reverseRangeWithScores(HOT_KEYWORD_ZSET, 0, n - 1L);
            if (tuples == null || tuples.isEmpty()) {
                return Collections.emptyList();
            }
            List<HotKeywordDTO> list = new ArrayList<>(tuples.size());
            for (ZSetOperations.TypedTuple<Object> t : tuples) {
                if (t.getValue() == null) continue;
                HotKeywordDTO dto = new HotKeywordDTO();
                dto.setKeyword(t.getValue().toString());
                dto.setCount(t.getScore() == null ? 0L : t.getScore().longValue());
                list.add(dto);
            }
            return list;
        } catch (Exception e) {
            log.warn("读取热门搜索词失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> suggestions(String keyword, Integer limit) {
        if (StrUtil.isBlank(keyword)) return Collections.emptyList();
        int n = limit == null || limit < 1 ? 10 : Math.min(limit, MAX_TOP_N);
        String kw = keyword.trim();
        Set<String> set = new LinkedHashSet<>();

        // 帖子标题前缀匹配
        LambdaQueryWrapper<Post> postWrap = new LambdaQueryWrapper<>();
        postWrap.eq(Post::getStatus, 1)
                .likeRight(Post::getTitle, kw)
                .orderByDesc(Post::getViewCount)
                .last("LIMIT " + n);
        postMapper.selectList(postWrap).forEach(p -> {
            if (StrUtil.isNotBlank(p.getTitle())) set.add(p.getTitle());
        });
        if (set.size() >= n) return new ArrayList<>(set).subList(0, n);

        // 新闻标题前缀匹配
        LambdaQueryWrapper<News> newsWrap = new LambdaQueryWrapper<>();
        newsWrap.likeRight(News::getTitle, kw)
                .orderByDesc(News::getHotScore)
                .last("LIMIT " + n);
        newsMapper.selectList(newsWrap).forEach(x -> {
            if (StrUtil.isNotBlank(x.getTitle())) set.add(x.getTitle());
        });
        if (set.size() >= n) return new ArrayList<>(set).subList(0, n);

        // 视频标题前缀匹配
        LambdaQueryWrapper<Video> videoWrap = new LambdaQueryWrapper<>();
        videoWrap.eq(Video::getStatus, 1)
                .likeRight(Video::getTitle, kw)
                .orderByDesc(Video::getViewCount)
                .last("LIMIT " + n);
        videoMapper.selectList(videoWrap).forEach(v -> {
            if (StrUtil.isNotBlank(v.getTitle())) set.add(v.getTitle());
        });

        List<String> result = new ArrayList<>(set);
        return result.size() > n ? result.subList(0, n) : result;
    }

    /**
     * 埋点：ZSet ZINCRBY。过短/过长的关键词不计数。
     */
    private void recordKeyword(String keyword) {
        if (StrUtil.isBlank(keyword)) return;
        String kw = keyword.trim();
        if (kw.length() < MIN_HOT_KW_LEN || kw.length() > MAX_HOT_KW_LEN) return;
        try {
            redisTemplate.opsForZSet().incrementScore(HOT_KEYWORD_ZSET, kw, 1.0);
        } catch (Exception e) {
            log.warn("搜索词埋点失败: {}", e.getMessage());
        }
    }

    // ================ post ================

    private List<SearchItemDTO> searchPosts(String kw, int n) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1)
                .and(w -> w.like(Post::getTitle, kw).or().like(Post::getSummary, kw))
                .orderByDesc(Post::getCreatedAt)
                .last("LIMIT " + n);
        return postMapper.selectList(wrapper).stream()
                .map(this::toPostItem).collect(Collectors.toList());
    }

    private IPage<SearchItemDTO> pagePosts(String kw, int page, int size) {
        Page<Post> pager = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1)
                .and(w -> w.like(Post::getTitle, kw).or().like(Post::getSummary, kw))
                .orderByDesc(Post::getCreatedAt);
        return postMapper.selectPage(pager, wrapper).convert(this::toPostItem);
    }

    private SearchItemDTO toPostItem(Post post) {
        SearchItemDTO dto = new SearchItemDTO();
        dto.setType("post");
        dto.setId(post.getPostId());
        dto.setTitle(post.getTitle());
        dto.setSummary(post.getSummary());
        dto.setCoverImage(post.getCoverImage());
        dto.setCategory(post.getCategoryCode());
        dto.setPublishedAt(post.getCreatedAt());
        dto.setViewCount(post.getViewCount());
        return dto;
    }

    // ================ news ================

    private List<SearchItemDTO> searchNews(String kw, int n) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(News::getTitle, kw).or().like(News::getSummary, kw))
                .orderByDesc(News::getPublishedAt)
                .last("LIMIT " + n);
        return newsMapper.selectList(wrapper).stream()
                .map(this::toNewsItem).collect(Collectors.toList());
    }

    private IPage<SearchItemDTO> pageNews(String kw, int page, int size) {
        Page<News> pager = new Page<>(page, size);
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(News::getTitle, kw).or().like(News::getSummary, kw))
                .orderByDesc(News::getPublishedAt);
        return newsMapper.selectPage(pager, wrapper).convert(this::toNewsItem);
    }

    private SearchItemDTO toNewsItem(News news) {
        SearchItemDTO dto = new SearchItemDTO();
        dto.setType("news");
        dto.setId(news.getNewsId());
        dto.setTitle(news.getTitle());
        dto.setSummary(news.getSummary());
        dto.setCoverImage(news.getCoverImage());
        dto.setCategory(news.getCategory());
        dto.setPublishedAt(news.getPublishedAt());
        dto.setViewCount(news.getViewCount());
        return dto;
    }

    // ================ video ================

    private List<SearchItemDTO> searchVideos(String kw, int n) {
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getStatus, 1)
                .and(w -> w.like(Video::getTitle, kw).or().like(Video::getDescription, kw))
                .orderByDesc(Video::getViewCount)
                .last("LIMIT " + n);
        return videoMapper.selectList(wrapper).stream()
                .map(this::toVideoItem).collect(Collectors.toList());
    }

    private IPage<SearchItemDTO> pageVideos(String kw, int page, int size) {
        Page<Video> pager = new Page<>(page, size);
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getStatus, 1)
                .and(w -> w.like(Video::getTitle, kw).or().like(Video::getDescription, kw))
                .orderByDesc(Video::getViewCount);
        return videoMapper.selectPage(pager, wrapper).convert(this::toVideoItem);
    }

    private SearchItemDTO toVideoItem(Video video) {
        SearchItemDTO dto = new SearchItemDTO();
        dto.setType("video");
        dto.setId(video.getVideoId());
        dto.setTitle(video.getTitle());
        dto.setSummary(video.getDescription());
        dto.setCoverImage(video.getCoverImage());
        dto.setCategory(video.getCategory());
        dto.setPublishedAt(video.getCreatedAt());
        dto.setViewCount(video.getViewCount());
        return dto;
    }
}
