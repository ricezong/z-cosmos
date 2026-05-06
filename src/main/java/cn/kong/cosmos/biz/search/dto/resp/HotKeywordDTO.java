package cn.kong.cosmos.biz.search.dto.resp;

import lombok.Data;

/**
 * 热门搜索词项
 */
@Data
public class HotKeywordDTO {

    private String keyword;

    /** 搜索次数（Redis ZSet score） */
    private Long count;
}
