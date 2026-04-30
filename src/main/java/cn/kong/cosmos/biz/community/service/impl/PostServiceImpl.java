package cn.kong.cosmos.biz.community.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.kong.cosmos.auth.dto.resp.UserSimpleDTO;
import cn.kong.cosmos.auth.service.AuthUserProvider;
import cn.kong.cosmos.auth.util.CurrentUserContext;
import cn.kong.cosmos.biz.community.dto.req.CreatePostReq;
import cn.kong.cosmos.biz.community.dto.req.PostQueryReq;
import cn.kong.cosmos.biz.community.dto.req.UpdatePostReq;
import cn.kong.cosmos.biz.community.dto.resp.PostDetailDTO;
import cn.kong.cosmos.biz.community.dto.resp.PostSummaryDTO;
import cn.kong.cosmos.biz.community.entity.Category;
import cn.kong.cosmos.biz.community.entity.Collect;
import cn.kong.cosmos.biz.community.entity.Like;
import cn.kong.cosmos.biz.community.entity.Post;
import cn.kong.cosmos.biz.community.mapper.CategoryMapper;
import cn.kong.cosmos.biz.community.mapper.CollectMapper;
import cn.kong.cosmos.biz.community.mapper.LikeMapper;
import cn.kong.cosmos.biz.community.mapper.PostMapper;
import cn.kong.cosmos.biz.community.service.PostService;
import cn.kong.cosmos.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子服务实现 - 社区核心业务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private static final int MIN_CONTENT_LENGTH_DEFAULT = 10;
    private static final int MAX_CONTENT_LENGTH_DEFAULT = 10000;
    private static final String DAILY_POST_LIMIT_KEY = "biz:post:daily_limit:%s:%s";

    private final PostMapper postMapper;
    private final LikeMapper likeMapper;
    private final CollectMapper collectMapper;
    private final CategoryMapper categoryMapper;
    private final AuthUserProvider authUserProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${community.post.daily-limit:50}")
    private int dailyLimit;

    @Value("${community.post.min-content-length:10}")
    private int minContentLength;

    @Value("${community.post.max-content-length:10000}")
    private int maxContentLength;

    // =================== 列表查询 ===================

    @Override
    public IPage<PostSummaryDTO> listPosts(PostQueryReq query) {
        int p = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int s = query.getSize() == null || query.getSize() < 1 ? 10 : Math.min(query.getSize(), 50);

        Page<Post> page = new Page<>(p, s);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1);
        if (StrUtil.isNotBlank(query.getCategoryCode())) {
            wrapper.eq(Post::getCategoryCode, query.getCategoryCode());
        }
        if (StrUtil.isNotBlank(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(Post::getTitle, kw).or().like(Post::getSummary, kw));
        }
        applySorting(wrapper, query.getSort());

        IPage<Post> result = postMapper.selectPage(page, wrapper);
        return result.convert(this::toSummaryDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDetailDTO getDetail(String postId) {
        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>().eq(Post::getPostId, postId));
        if (post == null || post.getStatus() != 1) {
            throw BusinessException.notFound("帖子不存在或已下架");
        }

        // view_count + 1（失败不影响主流程）
        try {
            postMapper.update(null, new LambdaUpdateWrapper<Post>()
                    .eq(Post::getPostId, postId)
                    .setSql("view_count = view_count + 1"));
        } catch (Exception e) {
            log.warn("帖子浏览数自增失败: {}", e.getMessage());
        }

        PostDetailDTO dto = new PostDetailDTO();
        fillSummary(dto, post);
        dto.setContent(post.getContent());

        String currentUserId = CurrentUserContext.getUserIdStr();
        if (currentUserId != null) {
            dto.setLiked(likeMapper.selectCount(new LambdaQueryWrapper<Like>()
                    .eq(Like::getUserId, currentUserId)
                    .eq(Like::getTargetType, Like.TYPE_POST)
                    .eq(Like::getTargetId, postId)) > 0);
            dto.setCollected(collectMapper.selectCount(new LambdaQueryWrapper<Collect>()
                    .eq(Collect::getUserId, currentUserId)
                    .eq(Collect::getPostId, postId)) > 0);
            dto.setIsAuthor(currentUserId.equals(post.getUserId()));
        } else {
            dto.setLiked(false);
            dto.setCollected(false);
            dto.setIsAuthor(false);
        }

        return dto;
    }

    // =================== 发帖/编辑/删除 ===================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createPost(CreatePostReq req) {
        String userId = requireUserId();

        // 校验分类
        Category category = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getCategoryCode, req.getCategoryCode()));
        if (category == null) {
            throw BusinessException.badRequest("分类不存在: " + req.getCategoryCode());
        }

        // 校验日限
        checkDailyLimit(userId);

        // 内容清洗与校验
        String cleanContent = Jsoup.clean(req.getContent(), Safelist.basicWithImages());
        int plainLen = Jsoup.parse(cleanContent).text().length();
        if (plainLen < Math.max(minContentLength, MIN_CONTENT_LENGTH_DEFAULT) ||
                plainLen > Math.min(maxContentLength, MAX_CONTENT_LENGTH_DEFAULT)) {
            throw BusinessException.badRequest(
                    String.format("内容长度需在 %d-%d 字之间，当前 %d 字", minContentLength, maxContentLength, plainLen));
        }

        Post post = new Post();
        post.setPostId(IdUtil.getSnowflakeNextIdStr());
        post.setUserId(userId);
        post.setCategoryCode(req.getCategoryCode());
        post.setTitle(req.getTitle().trim());
        post.setContent(cleanContent);
        post.setSummary(extractSummary(cleanContent));
        post.setCoverImage(req.getCoverImage());
        post.setImages(req.getImages() == null ? Collections.emptyList() : req.getImages());
        post.setViewCount(0L);
        post.setLikeCount(0L);
        post.setCommentCount(0L);
        post.setCollectCount(0L);
        post.setIsTop(0);
        post.setIsEssence(0);
        post.setStatus(1);

        postMapper.insert(post);

        // 分类 postCount + 1
        categoryMapper.update(null, new LambdaUpdateWrapper<Category>()
                .eq(Category::getCategoryCode, req.getCategoryCode())
                .setSql("post_count = post_count + 1"));

        // 用户 postCount + 1
        authUserProvider.incrementPostCount(userId);

        // 日限计数 + 1
        incrementDailyCount(userId);

        log.info("发帖成功: userId={}, postId={}", userId, post.getPostId());
        return post.getPostId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(String postId, UpdatePostReq req) {
        String userId = requireUserId();
        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>().eq(Post::getPostId, postId));
        if (post == null) {
            throw BusinessException.notFound("帖子不存在");
        }
        if (!userId.equals(post.getUserId())) {
            throw BusinessException.forbidden("只能编辑自己的帖子");
        }

        LambdaUpdateWrapper<Post> update = new LambdaUpdateWrapper<>();
        update.eq(Post::getPostId, postId);
        boolean changed = false;

        if (StrUtil.isNotBlank(req.getTitle())) {
            update.set(Post::getTitle, req.getTitle().trim());
            changed = true;
        }
        if (StrUtil.isNotBlank(req.getCategoryCode())) {
            Category cat = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                    .eq(Category::getCategoryCode, req.getCategoryCode()));
            if (cat == null) {
                throw BusinessException.badRequest("分类不存在");
            }
            update.set(Post::getCategoryCode, req.getCategoryCode());
            changed = true;
        }
        if (StrUtil.isNotBlank(req.getContent())) {
            String cleanContent = Jsoup.clean(req.getContent(), Safelist.basicWithImages());
            update.set(Post::getContent, cleanContent);
            update.set(Post::getSummary, extractSummary(cleanContent));
            changed = true;
        }
        if (req.getCoverImage() != null) {
            update.set(Post::getCoverImage, req.getCoverImage());
            changed = true;
        }
        if (req.getImages() != null) {
            // MyBatis-Plus JSON typeHandler 不支持 Lambda Wrapper 直接 set List
            // 改用 entity 路径
            Post partial = new Post();
            partial.setId(post.getId());
            partial.setImages(req.getImages());
            postMapper.updateById(partial);
        }

        if (changed) {
            postMapper.update(null, update);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(String postId) {
        String userId = requireUserId();
        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>().eq(Post::getPostId, postId));
        if (post == null) {
            throw BusinessException.notFound("帖子不存在");
        }
        if (!userId.equals(post.getUserId())) {
            throw BusinessException.forbidden("只能删除自己的帖子");
        }
        postMapper.deleteById(post.getId()); // 逻辑删除

        // 分类 postCount - 1
        categoryMapper.update(null, new LambdaUpdateWrapper<Category>()
                .eq(Category::getCategoryCode, post.getCategoryCode())
                .setSql("post_count = GREATEST(post_count - 1, 0)"));
    }

    // =================== 我的帖子 / 我的收藏 ===================

    @Override
    public IPage<PostSummaryDTO> listMyPosts(Integer page, Integer size) {
        String userId = requireUserId();
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : Math.min(size, 50);

        Page<Post> pager = new Page<>(p, s);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId)
                .orderByDesc(Post::getCreatedAt);
        IPage<Post> result = postMapper.selectPage(pager, wrapper);
        return result.convert(this::toSummaryDTO);
    }

    @Override
    public IPage<PostSummaryDTO> listMyCollections(Integer page, Integer size) {
        String userId = requireUserId();
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : Math.min(size, 50);

        // 先分页查收藏关系
        Page<Collect> pager = new Page<>(p, s);
        LambdaQueryWrapper<Collect> cWrapper = new LambdaQueryWrapper<>();
        cWrapper.eq(Collect::getUserId, userId).orderByDesc(Collect::getCreatedAt);
        IPage<Collect> cResult = collectMapper.selectPage(pager, cWrapper);

        if (cResult.getRecords().isEmpty()) {
            return cResult.convert(c -> new PostSummaryDTO());
        }

        // 批量查询帖子
        List<String> postIds = cResult.getRecords().stream()
                .map(Collect::getPostId).collect(Collectors.toList());
        List<Post> posts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .in(Post::getPostId, postIds)
                .eq(Post::getStatus, 1));
        Map<String, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::getPostId, p2 -> p2));

        IPage<PostSummaryDTO> resultPage = new Page<>(cResult.getCurrent(), cResult.getSize(), cResult.getTotal());
        List<PostSummaryDTO> records = cResult.getRecords().stream()
                .map(c -> postMap.get(c.getPostId()))
                .filter(Objects::nonNull)
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
        resultPage.setRecords(records);
        return resultPage;
    }

    // =================== 私有辅助 ===================

    private void applySorting(LambdaQueryWrapper<Post> wrapper, String sort) {
        switch (sort == null ? "latest" : sort) {
            case "hot":
                wrapper.orderByDesc(Post::getIsTop)
                        .orderByDesc(Post::getLikeCount)
                        .orderByDesc(Post::getCommentCount)
                        .orderByDesc(Post::getCreatedAt);
                break;
            case "most_commented":
                wrapper.orderByDesc(Post::getIsTop)
                        .orderByDesc(Post::getCommentCount)
                        .orderByDesc(Post::getCreatedAt);
                break;
            case "latest":
            default:
                wrapper.orderByDesc(Post::getIsTop).orderByDesc(Post::getCreatedAt);
        }
    }

    private String requireUserId() {
        String userId = CurrentUserContext.getUserIdStr();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return userId;
    }

    private void checkDailyLimit(String userId) {
        String key = String.format(DAILY_POST_LIMIT_KEY, userId, LocalDate.now());
        try {
            Object count = redisTemplate.opsForValue().get(key);
            long cnt = count == null ? 0L : Long.parseLong(count.toString());
            if (cnt >= dailyLimit) {
                throw BusinessException.tooManyRequests("今日发帖数已达上限（" + dailyLimit + " 条）");
            }
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.warn("读取发帖日限失败，跳过限制: {}", e.getMessage());
        }
    }

    private void incrementDailyCount(String userId) {
        String key = String.format(DAILY_POST_LIMIT_KEY, userId, LocalDate.now());
        try {
            Long cnt = redisTemplate.opsForValue().increment(key);
            if (cnt != null && cnt == 1L) {
                redisTemplate.expire(key, Duration.ofDays(1));
            }
        } catch (Exception e) {
            log.warn("更新发帖日限失败: {}", e.getMessage());
        }
    }

    private String extractSummary(String html) {
        String plain = Jsoup.parse(html).text();
        return plain.length() <= 120 ? plain : plain.substring(0, 120) + "...";
    }

    private PostSummaryDTO toSummaryDTO(Post post) {
        PostSummaryDTO dto = new PostSummaryDTO();
        fillSummary(dto, post);
        return dto;
    }

    private void fillSummary(PostSummaryDTO dto, Post post) {
        dto.setPostId(post.getPostId());
        dto.setTitle(post.getTitle());
        dto.setSummary(post.getSummary());
        dto.setCoverImage(post.getCoverImage());
        dto.setImages(post.getImages());
        dto.setViewCount(post.getViewCount());
        dto.setLikeCount(post.getLikeCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setCollectCount(post.getCollectCount());
        dto.setIsTop(post.getIsTop());
        dto.setIsEssence(post.getIsEssence());
        dto.setCreatedAt(post.getCreatedAt());

        PostSummaryDTO.AuthorInfo author = new PostSummaryDTO.AuthorInfo();
        author.setUserId(post.getUserId());
        UserSimpleDTO u = authUserProvider.getUserByUserId(post.getUserId());
        if (u != null) {
            author.setNickname(u.getNickname());
            author.setAvatarUrl(u.getAvatarUrl());
        }
        dto.setAuthor(author);

        PostSummaryDTO.CategoryInfo cat = new PostSummaryDTO.CategoryInfo();
        cat.setCode(post.getCategoryCode());
        Category category = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getCategoryCode, post.getCategoryCode()));
        if (category != null) {
            cat.setName(category.getName());
            cat.setColor(category.getColor());
        }
        dto.setCategory(cat);
    }
}
