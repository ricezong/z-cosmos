package cn.kong.cosmos.biz.search.dto.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聚合搜索结果项 - 统一轻量结构
 * type 枚举：post / news / video
 */
@Data
public class SearchItemDTO {

    /** 结果类型：post / news / video */
    private String type;

    /** 业务 ID（postId / newsId / videoId） */
    private String id;

    private String title;

    /** 摘要或描述 */
    private String summary;

    private String coverImage;

    /** 分类代码或名称 */
    private String category;

    /** 参考时间：发表/发布时间 */
    private LocalDateTime publishedAt;

    /** 浏览数（便于前端排序/展示） */
    private Long viewCount;
}
