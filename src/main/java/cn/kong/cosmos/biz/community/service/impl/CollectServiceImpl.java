package cn.kong.cosmos.biz.community.service.impl;

import cn.kong.cosmos.auth.util.CurrentUserContext;
import cn.kong.cosmos.biz.community.entity.Collect;
import cn.kong.cosmos.biz.community.entity.Post;
import cn.kong.cosmos.biz.community.mapper.CollectMapper;
import cn.kong.cosmos.biz.community.mapper.PostMapper;
import cn.kong.cosmos.biz.community.service.CollectService;
import cn.kong.cosmos.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 收藏服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectServiceImpl implements CollectService {

    private final CollectMapper collectMapper;
    private final PostMapper postMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collect(String postId) {
        String userId = requireUserId();
        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>().eq(Post::getPostId, postId));
        if (post == null) {
            throw BusinessException.notFound("帖子不存在");
        }

        Collect c = new Collect();
        c.setUserId(userId);
        c.setPostId(postId);
        try {
            collectMapper.insert(c);
        } catch (DuplicateKeyException e) {
            log.debug("重复收藏，幂等返回: userId={}, postId={}", userId, postId);
            return;
        }
        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getPostId, postId)
                .setSql("collect_count = collect_count + 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uncollect(String postId) {
        String userId = requireUserId();
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId, userId).eq(Collect::getPostId, postId);
        int deleted = collectMapper.delete(wrapper);
        if (deleted > 0) {
            postMapper.update(null, new LambdaUpdateWrapper<Post>()
                    .eq(Post::getPostId, postId)
                    .setSql("collect_count = collect_count - 1"));
        }
    }

    @Override
    public boolean hasCollected(String postId) {
        String userId = CurrentUserContext.getUserIdStr();
        if (userId == null) {
            return false;
        }
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId, userId).eq(Collect::getPostId, postId);
        return collectMapper.selectCount(wrapper) > 0;
    }

    private String requireUserId() {
        String userId = CurrentUserContext.getUserIdStr();
        if (userId == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return userId;
    }
}
