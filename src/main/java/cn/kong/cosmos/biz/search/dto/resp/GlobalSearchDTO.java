package cn.kong.cosmos.biz.search.dto.resp;

import lombok.Data;

import java.util.List;

/**
 * 聚合搜索响应 - 按类型分组
 * 当请求 type=all 时返回三组，否则只返回对应组（其余为空列表）
 */
@Data
public class GlobalSearchDTO {

    /** 关键词回显 */
    private String keyword;

    /** 匹配到的帖子 */
    private List<SearchItemDTO> posts;

    /** 匹配到的新闻 */
    private List<SearchItemDTO> news;

    /** 匹配到的视频 */
    private List<SearchItemDTO> videos;

    /** 总命中数（三组之和） */
    private Integer total;
}
