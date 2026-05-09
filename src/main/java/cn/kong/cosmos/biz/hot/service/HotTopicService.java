package cn.kong.cosmos.biz.hot.service;

import cn.kong.cosmos.biz.hot.dto.resp.HotTopicDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 热点话题服务接口
 */
public interface HotTopicService {
    
    /**
     * 获取热点列表
     */
    IPage<HotTopicDTO> listHotTopics(Integer page, Integer size, String category);
    
    /**
     * 获取热点详情
     */
    HotTopicDTO getHotTopicDetail(String topicId);
}
