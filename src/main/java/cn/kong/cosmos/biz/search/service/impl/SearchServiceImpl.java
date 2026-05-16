package cn.kong.cosmos.biz.search.service.impl;

import cn.kong.cosmos.biz.search.dto.SearchResultDTO;
import cn.kong.cosmos.biz.search.entity.SearchIndex;
import cn.kong.cosmos.biz.search.mapper.SearchIndexMapper;
import cn.kong.cosmos.biz.search.service.SearchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 搜索服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchIndexMapper searchIndexMapper;

    @Override
    public IPage<SearchResultDTO> search(String keyword, String type, Integer page, Integer size) {
        // 判断是否使用全文索引搜索
        if (keyword != null && !keyword.isEmpty()) {
            // 计算分页参数
            int offset = (page - 1) * size;

            // 获取总数
            int total = searchIndexMapper.countFullTextSearch(
                    keyword, type != null && !type.isEmpty() ? type.toUpperCase() : null
            );

            // 使用数据库级分页查询
            List<SearchIndex> fullTextResults = searchIndexMapper.fullTextSearch(
                    keyword,
                    type != null && !type.isEmpty() ? type.toUpperCase() : null,
                    offset,
                    size
            );

            // 转换为 DTO
            List<SearchResultDTO> pageRecords = fullTextResults.stream()
                    .map(index -> toDTO(index, keyword))
                    .toList();

            // 构造分页结果
            Page<SearchResultDTO> resultPage = new Page<>(page, size, total);
            resultPage.setRecords(pageRecords);
            return resultPage;
        } else {
            // 无关键词时，按时间排序查询
            Page<SearchIndex> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<SearchIndex> queryWrapper = new LambdaQueryWrapper<>();
            if (type != null && !type.isEmpty()) {
                queryWrapper.eq(SearchIndex::getContentType, type.toUpperCase());
            }
            queryWrapper.orderByDesc(SearchIndex::getCreatedAt);

            IPage<SearchIndex> indexPage = searchIndexMapper.selectPage(pageParam, queryWrapper);
            return indexPage.convert(this::toDTO);
        }
    }

    /**
     * 转换为DTO（无高亮）
     */
    private SearchResultDTO toDTO(SearchIndex index) {
        SearchResultDTO dto = new SearchResultDTO();
        dto.setContentType(index.getContentType());
        dto.setContentId(index.getContentId());
        dto.setTitle(index.getTitle());
        dto.setHighlight(null);
        return dto;
    }

    /**
     * 转换为DTO（带高亮）
     */
    private SearchResultDTO toDTO(SearchIndex index, String keyword) {
        SearchResultDTO dto = new SearchResultDTO();
        dto.setContentType(index.getContentType());
        dto.setContentId(index.getContentId());
        dto.setTitle(index.getTitle());
        dto.setHighlight(generateHighlight(index.getTitle(), keyword));
        return dto;
    }

    /**
     * 生成搜索高亮片段
     * 在标题中将匹配的关键词用 <em> 标签包裹
     */
    private String generateHighlight(String title, String keyword) {
        if (title == null || keyword == null || keyword.isEmpty()) {
            return title;
        }
        // 简单的关键词高亮替换（不区分大小写）
        String lowerTitle = title.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        int idx = lowerTitle.indexOf(lowerKeyword);
        if (idx < 0) {
            return title;
        }
        // 返回包含关键词的片段，前后各取20个字符
        int start = Math.max(0, idx - 20);
        int end = Math.min(title.length(), idx + keyword.length() + 20);
        String snippet = (start > 0 ? "..." : "")
                + title.substring(start, idx)
                + "<em>" + title.substring(idx, idx + keyword.length()) + "</em>"
                + title.substring(idx + keyword.length(), end)
                + (end < title.length() ? "..." : "");
        return snippet;
    }
}