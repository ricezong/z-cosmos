package cn.kong.cosmos.biz.community.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.kong.cosmos.auth.dto.resp.UserSimpleDTO;
import cn.kong.cosmos.auth.service.AuthUserProvider;
import cn.kong.cosmos.auth.util.CurrentUserContext;
import cn.kong.cosmos.biz.community.dto.req.CreateCommentReq;
import cn.kong.cosmos.biz.community.dto.resp.CommentTreeDTO;
import cn.kong.cosmos.biz.community.entity.Comment;
import cn.kong.cosmos.biz.community.entity.Like;
import cn.kong.cosmos.biz.community.entity.Post;
import cn.kong.cosmos.biz.community.mapper.CommentMapper;
import cn.kong.cosmos.biz.community.mapper.LikeMapper;
import cn.kong.cosmos.biz.community.mapper.PostMapper;
import cn.kong.cosmos.biz.community.service.CommentService;
import cn.kong.cosmos.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论服务实现 - 支持楼中楼树形
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final LikeMapper likeMapper;
    private final AuthUserProvider authUserProvider;

    @Override
    public IPage<CommentTreeDTO> listByPost(String postId, Integer page, Integer size) {
        validatePostExists(postId);
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : Math.min(size, 50);

        // 1. 分页查询顶层评论
        Page<Comment> rootPage = new Page<>(p, s);
        LambdaQueryWrapper<Comment> rootWrapper = new LambdaQueryWrapper<>();
        rootWrapper.eq(Comment::getPostId, postId)
                .isNull(Comment::getParentId)
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreatedAt);
        IPage<Comment> rootResult = commentMapper.selectPage(rootPage, rootWrapper);

        List<Comment> roots = rootResult.getRecords();
        if (roots.isEmpty()) {
            return rootResult.convert(c -> new CommentTreeDTO());
        }

        // 2. 批量查询这些顶层评论的所有楼中楼
        List<String> rootIds = roots.stream().map(Comment::getCommentId).collect(Collectors.toList());
        LambdaQueryWrapper<Comment> subWrapper = new LambdaQueryWrapper<>();
        subWrapper.in(Comment::getParentId, rootIds)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreatedAt);
        List<Comment> subComments = commentMapper.selectList(subWrapper);

        // 3. 组装树形
        String currentUserId = CurrentUserContext.getUserIdStr();
        Set<String> myLikedCommentIds = loadMyLikedCommentIds(currentUserId, roots, subComments);

        Map<String, List<Comment>> subMap = subComments.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));

        IPage<CommentTreeDTO> resultPage = new Page<>(rootResult.getCurrent(), rootResult.getSize(), rootResult.getTotal());
        List<CommentTreeDTO> trees = roots.stream().map(root -> {
            CommentTreeDTO dto = toTreeDTO(root, myLikedCommentIds);
            List<Comment> subs = subMap.getOrDefault(root.getCommentId(), Collections.emptyList());
            dto.setReplies(subs.stream().map(sub -> toTreeDTO(sub, myLikedCommentIds)).collect(Collectors.toList()));
            dto.setReplyCount(subs.size());
            return dto;
        }).collect(Collectors.toList());
        resultPage.setRecords(trees);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createComment(String postId, CreateCommentReq req) {
        String userId = requireUserId();
        Post post = validatePostExists(postId);

        // 楼中楼校验
        if (req.getParentId() != null && !req.getParentId().isBlank()) {
            Comment parent = commentMapper.selectOne(new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getCommentId, req.getParentId()));
            if (parent == null || !postId.equals(parent.getPostId())) {
                throw BusinessException.badRequest("父评论不存在或不属于当前帖子");
            }
        }

        String cleanContent = Jsoup.clean(req.getContent(), Safelist.basic());

        Comment comment = new Comment();
        comment.setCommentId(IdUtil.getSnowflakeNextIdStr());
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(req.getParentId());
        comment.setReplyToUserId(req.getReplyToUserId());
        comment.setContent(cleanContent);
        comment.setLikeCount(0L);
        comment.setStatus(1);

        // 填充被回复者昵称
        if (req.getReplyToUserId() != null) {
            UserSimpleDTO replyTo = authUserProvider.getUserByUserId(req.getReplyToUserId());
            if (replyTo != null) {
                comment.setReplyToNickname(replyTo.getNickname());
            }
        }

        commentMapper.insert(comment);

        // 更新帖子评论数
        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getPostId, postId)
                .setSql("comment_count = comment_count + 1"));

        log.info("评论发表: userId={}, postId={}, commentId={}", userId, postId, comment.getCommentId());
        return comment.getCommentId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(String commentId) {
        String userId = requireUserId();
        Comment comment = commentMapper.selectOne(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getCommentId, commentId));
        if (comment == null) {
            throw BusinessException.notFound("评论不存在");
        }
        if (!userId.equals(comment.getUserId())) {
            throw BusinessException.forbidden("只能删除自己的评论");
        }

        commentMapper.deleteById(comment.getId());

        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getPostId, comment.getPostId())
                .setSql("comment_count = GREATEST(comment_count - 1, 0)"));
    }

    // ============ 私有方法 ============

    private Post validatePostExists(String postId) {
        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>().eq(Post::getPostId, postId));
        if (post == null) {
            throw BusinessException.notFound("帖子不存在");
        }
        return post;
    }

    private String requireUserId() {
        String userId = CurrentUserContext.getUserIdStr();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return userId;
    }

    private Set<String> loadMyLikedCommentIds(String userId, List<Comment> roots, List<Comment> subs) {
        if (userId == null) {
            return Collections.emptySet();
        }
        List<String> allIds = new ArrayList<>();
        roots.forEach(c -> allIds.add(c.getCommentId()));
        subs.forEach(c -> allIds.add(c.getCommentId()));
        if (allIds.isEmpty()) {
            return Collections.emptySet();
        }
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetType, Like.TYPE_COMMENT)
                .in(Like::getTargetId, allIds);
        return likeMapper.selectList(wrapper).stream()
                .map(Like::getTargetId).collect(Collectors.toSet());
    }

    private CommentTreeDTO toTreeDTO(Comment c, Set<String> likedSet) {
        CommentTreeDTO dto = new CommentTreeDTO();
        dto.setCommentId(c.getCommentId());
        dto.setContent(c.getContent());
        dto.setLikeCount(c.getLikeCount());
        dto.setLiked(likedSet.contains(c.getCommentId()));
        dto.setCreatedAt(c.getCreatedAt());
        dto.setReplyToNickname(c.getReplyToNickname());

        CommentTreeDTO.AuthorInfo author = new CommentTreeDTO.AuthorInfo();
        author.setUserId(c.getUserId());
        UserSimpleDTO u = authUserProvider.getUserByUserId(c.getUserId());
        if (u != null) {
            author.setNickname(u.getNickname());
            author.setAvatarUrl(u.getAvatarUrl());
        }
        dto.setAuthor(author);
        return dto;
    }
}
