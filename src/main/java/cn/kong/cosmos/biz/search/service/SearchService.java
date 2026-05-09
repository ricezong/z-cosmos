package cn.kong.cosmos.biz.search.service;

import cn.kong.cosmos.biz.search.dto.SearchResultDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 搜索服务接口
 */
public interface SearchService {
    
    /**
     * 全局搜索
     */
    IPage<SearchResultDTO> search(String keyword, String type, Integer page, Integer size);
}
