package cn.kong.cosmos.biz.community.service.impl;

import cn.kong.cosmos.auth.util.CurrentUserContext;
import cn.kong.cosmos.biz.community.entity.Like;
import cn.kong.cosmos.biz.community.entity.Post;
import cn.kong.cosmos.biz.community.entity.Comment;
import cn.kong.cosmos.biz.community.mapper.LikeMapper;
import cn.kong.cosmos.biz.community.mapper.PostMapper;
import cn.kong.cosmos.biz.community.mapper.CommentMapper;
import cn.kong.cosmos.biz.community.service.LikeService;
import cn.kong.cosmos.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 点赞服务实现 - 帖子/评论通用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeMapper likeMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(String targetType, String targetId) {
        validateTargetType(targetType);
        String userId = requireUserId();
        validateTargetExists(targetType, targetId);

        Like like = new Like();
        like.setUserId(userId);
        like.setTargetType(targetType);
        like.setTargetId(targetId);

        try {
            likeMapper.insert(like);
        } catch (DuplicateKeyException e) {
            // 幂等：已点赞直接返回
            log.debug("重复点赞，幂等返回: userId={}, {}={}", userId, targetType, targetId);
            return;
        }

        // 更新目标 like_count + 1
        updateTargetLikeCount(targetType, targetId, 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlike(String targetType, String targetId) {
        validateTargetType(targetType);
        String userId = requireUserId();

        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetType, targetType)
                .eq(Like::getTargetId, targetId);
        int deleted = likeMapper.delete(wrapper);
        if (deleted > 0) {
            updateTargetLikeCount(targetType, targetId, -1);
        }
    }

    @Override
    public boolean hasLiked(String targetType, String targetId) {
        String userId = CurrentUserContext.getUserIdStr();
        if (userId == null) {
            return false;
        }
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetType, targetType)
                .eq(Like::getTargetId, targetId);
        return likeMapper.selectCount(wrapper) > 0;
    }

    // ============ 私有方法 ============

    private void validateTargetType(String targetType) {
        if (!Like.TYPE_POST.equals(targetType) && !Like.TYPE_COMMENT.equals(targetType)) {
            throw BusinessException.badRequest("非法点赞类型: " + targetType);
        }
    }

    private String requireUserId() {
        String userId = CurrentUserContext.getUserIdStr();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return userId;
    }

    private void validateTargetExists(String targetType, String targetId) {
        if (Like.TYPE_POST.equals(targetType)) {
            Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>()
                    .eq(Post::getPostId, targetId));
            if (post == null) {
                throw BusinessException.notFound("帖子不存在");
            }
        } else {
            Comment comment = commentMapper.selectOne(new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getCommentId, targetId));
            if (comment == null) {
                throw BusinessException.notFound("评论不存在");
            }
        }
    }

    private void updateTargetLikeCount(String targetType, String targetId, int delta) {
        String op = delta > 0 ? "like_count + 1" : "like_count - 1";
        if (Like.TYPE_POST.equals(targetType)) {
            postMapper.update(null, new LambdaUpdateWrapper<Post>()
                    .eq(Post::getPostId, targetId)
                    .setSql(op));
        } else {
            commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getCommentId, targetId)
                    .setSql(op));
        }
    }
}
