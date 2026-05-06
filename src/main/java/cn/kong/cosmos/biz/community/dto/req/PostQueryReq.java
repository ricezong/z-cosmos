package cn.kong.cosmos.biz.community.dto.req;

import lombok.Data;

/**
 * 帖子查询条件
 */
@Data
public class PostQueryReq {

    /** 分类代码（可选） */
    private String categoryCode;

    /** 排序：latest / hot / most_commented */
    private String sort = "latest";

    /** 搜索关键词（可选） */
    private String keyword;

    private Integer page = 1;
    private Integer size = 10;
}
