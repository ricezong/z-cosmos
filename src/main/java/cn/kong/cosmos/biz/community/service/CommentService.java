package cn.kong.cosmos.biz.community.service;

import cn.kong.cosmos.biz.community.dto.req.CreateCommentReq;
import cn.kong.cosmos.biz.community.dto.resp.CommentTreeDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 评论服务
 */
public interface CommentService {

    /** 帖子评论列表（楼中楼树形） */
    IPage<CommentTreeDTO> listByPost(String postId, Integer page, Integer size);

    /** 发表评论 / 回复 */
    String createComment(String postId, CreateCommentReq req);

    /** 删除评论（作者限定） */
    void deleteComment(String commentId);
}
