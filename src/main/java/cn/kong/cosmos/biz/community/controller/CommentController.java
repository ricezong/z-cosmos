package cn.kong.cosmos.biz.community.controller;

import cn.kong.cosmos.biz.community.dto.req.CreateCommentReq;
import cn.kong.cosmos.biz.community.dto.resp.CommentTreeDTO;
import cn.kong.cosmos.biz.community.entity.Like;
import cn.kong.cosmos.biz.community.service.CommentService;
import cn.kong.cosmos.biz.community.service.LikeService;
import cn.kong.cosmos.common.core.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 评论 Controller
 */
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final LikeService likeService;

    /** 帖子评论列表（公开，楼中楼树形） */
    @GetMapping("/api/posts/{postId}/comments")
    public Result<IPage<CommentTreeDTO>> listByPost(
            @PathVariable String postId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(commentService.listByPost(postId, page, size));
    }

    /** 发表评论 / 回复（登录） */
    @PostMapping("/api/posts/{postId}/comments")
    public Result<Map<String, String>> create(@PathVariable String postId,
                                              @Valid @RequestBody CreateCommentReq req) {
        String commentId = commentService.createComment(postId, req);
        Map<String, String> data = new HashMap<>();
        data.put("commentId", commentId);
        return Result.success(data);
    }

    /** 删除评论（作者） */
    @DeleteMapping("/api/comments/{commentId}")
    public Result<Void> delete(@PathVariable String commentId) {
        commentService.deleteComment(commentId);
        return Result.success();
    }

    /** 评论点赞（登录） */
    @PostMapping("/api/comments/{commentId}/like")
    public Result<Void> like(@PathVariable String commentId) {
        likeService.like(Like.TYPE_COMMENT, commentId);
        return Result.success();
    }

    /** 取消评论点赞（登录） */
    @DeleteMapping("/api/comments/{commentId}/like")
    public Result<Void> unlike(@PathVariable String commentId) {
        likeService.unlike(Like.TYPE_COMMENT, commentId);
        return Result.success();
    }
}
