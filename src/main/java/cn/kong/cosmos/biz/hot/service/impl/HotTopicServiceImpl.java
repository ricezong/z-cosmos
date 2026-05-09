package cn.kong.cosmos.biz.hot.service.impl;

import cn.kong.cosmos.biz.hot.dto.resp.HotTopicDTO;
import cn.kong.cosmos.biz.hot.entity.HotTopic;
import cn.kong.cosmos.biz.hot.mapper.HotTopicMapper;
import cn.kong.cosmos.biz.hot.service.HotTopicService;
import cn.kong.cosmos.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 热点话题服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotTopicServiceImpl implements HotTopicService {
    
    private final HotTopicMapper hotTopicMapper;
    
    @Override
    public IPage<HotTopicDTO> listHotTopics(Integer page, Integer size, String category) {
        Page<HotTopic> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<HotTopic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotTopic::getIsActive, 1)
                   .orderByDesc(HotTopic::getPublishTime);
        
        if (category != null && !category.isEmpty()) {
            queryWrapper.eq(HotTopic::getCategory, category);
        }
        
        IPage<HotTopic> topicPage = hotTopicMapper.selectPage(pageParam, queryWrapper);
        return topicPage.convert(this::toDTO);
    }
    
    @Override
    public HotTopicDTO getHotTopicDetail(String topicId) {
        HotTopic topic = hotTopicMapper.selectOne(
            new LambdaQueryWrapper<HotTopic>().eq(HotTopic::getTopicId, topicId)
        );
        
        if (topic == null) {
            throw new BusinessException("热点话题不存在");
        }
        
        return toDTO(topic);
    }
    
    /**
     * 转换为DTO
     */
    private HotTopicDTO toDTO(HotTopic topic) {
        HotTopicDTO dto = new HotTopicDTO();
        dto.setTopicId(topic.getTopicId());
        dto.setTitle(topic.getTitle());
        dto.setSummary(topic.getSummary());
        dto.setSourceUrl(topic.getSourceUrl());
        dto.setSourceName(topic.getSourceName());
        dto.setPublishTime(topic.getPublishTime());
        dto.setCategory(topic.getCategory());
        dto.setCreatedAt(topic.getCreatedAt());
        return dto;
    }
}
