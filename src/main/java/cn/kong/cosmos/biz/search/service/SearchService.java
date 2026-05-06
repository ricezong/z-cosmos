package cn.kong.cosmos.biz.search.service;

import cn.kong.cosmos.biz.search.dto.resp.GlobalSearchDTO;
import cn.kong.cosmos.biz.search.dto.resp.HotKeywordDTO;
import cn.kong.cosmos.biz.search.dto.resp.SearchItemDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 聚合搜索服务
 */
public interface SearchService {

    /**
     * 全局聚合搜索
     * @param keyword 关键词
     * @param topN    每组返回的条数（1-20）
     */
    GlobalSearchDTO globalSearch(String keyword, Integer topN);

    /**
     * 按类型分页搜索
     * @param type    post / news / video
     * @param keyword 关键词
     */
    IPage<SearchItemDTO> searchByType(String type, String keyword, Integer page, Integer size);

    /**
     * 热门搜索词 Top N（基于 Redis ZSet）
     */
    List<HotKeywordDTO> hotKeywords(Integer limit);

    /**
     * 搜索建议 - 基于前缀匹配帖/新闻/视频标题
     */
    List<String> suggestions(String keyword, Integer limit);
}
