package cn.kong.cosmos.biz.community.controller;

import cn.kong.cosmos.biz.community.dto.req.CreatePostReq;
import cn.kong.cosmos.biz.community.dto.req.PostQueryReq;
import cn.kong.cosmos.biz.community.dto.req.UpdatePostReq;
import cn.kong.cosmos.biz.community.dto.resp.PostDetailDTO;
import cn.kong.cosmos.biz.community.dto.resp.PostSummaryDTO;
import cn.kong.cosmos.biz.community.service.CollectService;
import cn.kong.cosmos.biz.community.service.LikeService;
import cn.kong.cosmos.biz.community.service.PostService;
import cn.kong.cosmos.biz.community.entity.Like;
import cn.kong.cosmos.common.core.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 帖子 Controller
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final LikeService likeService;
    private final CollectService collectService;

    /** 帖子列表（公开） */
    @GetMapping
    public Result<IPage<PostSummaryDTO>> list(PostQueryReq query) {
        return Result.success(postService.listPosts(query));
    }

    /** 帖子详情（公开） */
    @GetMapping("/{postId}")
    public Result<PostDetailDTO> detail(@PathVariable String postId) {
        return Result.success(postService.getDetail(postId));
    }

    /** 发布帖子（登录） */
    @PostMapping
    public Result<Map<String, String>> create(@Valid @RequestBody CreatePostReq req) {
        String postId = postService.createPost(req);
        Map<String, String> data = new HashMap<>();
        data.put("postId", postId);
        return Result.success(data);
    }

    /** 编辑帖子（作者） */
    @PutMapping("/{postId}")
    public Result<Void> update(@PathVariable String postId, @Valid @RequestBody UpdatePostReq req) {
        postService.updatePost(postId, req);
        return Result.success();
    }

    /** 删除帖子（作者） */
    @DeleteMapping("/{postId}")
    public Result<Void> delete(@PathVariable String postId) {
        postService.deletePost(postId);
        return Result.success();
    }

    /** 点赞帖子（登录） */
    @PostMapping("/{postId}/like")
    public Result<Void> like(@PathVariable String postId) {
        likeService.like(Like.TYPE_POST, postId);
        return Result.success();
    }

    /** 取消点赞（登录） */
    @DeleteMapping("/{postId}/like")
    public Result<Void> unlike(@PathVariable String postId) {
        likeService.unlike(Like.TYPE_POST, postId);
        return Result.success();
    }

    /** 收藏帖子（登录） */
    @PostMapping("/{postId}/collect")
    public Result<Void> collect(@PathVariable String postId) {
        collectService.collect(postId);
        return Result.success();
    }

    /** 取消收藏（登录） */
    @DeleteMapping("/{postId}/collect")
    public Result<Void> uncollect(@PathVariable String postId) {
        collectService.uncollect(postId);
        return Result.success();
    }
}
