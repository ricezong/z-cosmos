package cn.kong.cosmos.biz.community.service;

import cn.kong.cosmos.biz.community.dto.req.CreatePostReq;
import cn.kong.cosmos.biz.community.dto.req.PostQueryReq;
import cn.kong.cosmos.biz.community.dto.req.UpdatePostReq;
import cn.kong.cosmos.biz.community.dto.resp.PostDetailDTO;
import cn.kong.cosmos.biz.community.dto.resp.PostSummaryDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 帖子服务
 */
public interface PostService {

    /** 分页查询帖子列表（公开） */
    IPage<PostSummaryDTO> listPosts(PostQueryReq query);

    /** 获取帖子详情（公开，副作用：view_count+1） */
    PostDetailDTO getDetail(String postId);

    /** 发布帖子 */
    String createPost(CreatePostReq req);

    /** 编辑帖子（作者限定） */
    void updatePost(String postId, UpdatePostReq req);

    /** 删除帖子（作者或管理员） */
    void deletePost(String postId);

    /** 我的帖子（当前登录用户） */
    IPage<PostSummaryDTO> listMyPosts(Integer page, Integer size);

    /** 用户收藏帖子（当前登录用户） */
    IPage<PostSummaryDTO> listMyCollections(Integer page, Integer size);
}
