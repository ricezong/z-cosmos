package cn.kong.cosmos.biz.hot.service;

import cn.kong.cosmos.biz.hot.dto.resp.NewsDetailDTO;
import cn.kong.cosmos.biz.hot.dto.resp.NewsSummaryDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 新闻/热点服务
 */
public interface NewsService {

    /**
     * 分页新闻列表
     * @param category 分类（可选）
     * @param keyword  关键词（可选，LIKE 匹配标题/摘要）
     * @param sort     排序：latest（默认） / hot
     */
    IPage<NewsSummaryDTO> listNews(String category, String keyword, String sort, Integer page, Integer size);

    /**
     * 新闻详情（副作用：view_count + 1）
     */
    NewsDetailDTO getDetail(String newsId);

    /**
     * 热门榜单（前 N 条，按 hot_score 降序）
     */
    List<NewsSummaryDTO> topHot(Integer limit);

    /**
     * 按时段的热门排行
     * @param period day / week / month（默认 day）
     * @param limit  条数 1-50，默认 10
     */
    List<NewsSummaryDTO> ranking(String period, Integer limit);
}
