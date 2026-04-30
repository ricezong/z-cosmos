package cn.kong.cosmos.biz.community.service.impl;

import cn.kong.cosmos.biz.community.dto.resp.CategoryDTO;
import cn.kong.cosmos.biz.community.entity.Category;
import cn.kong.cosmos.biz.community.mapper.CategoryMapper;
import cn.kong.cosmos.biz.community.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现 - 带 Redis 缓存
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final String CACHE_KEY_ALL = "biz:category:all";

    private final CategoryMapper categoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${community.cache.ttl:3600}")
    private long cacheTtlSeconds;

    @Override
    @SuppressWarnings("unchecked")
    public List<CategoryDTO> listAll() {
        // 1. Redis 读缓存
        Object cached = redisTemplate.opsForValue().get(CACHE_KEY_ALL);
        if (cached instanceof List<?> list && !list.isEmpty()) {
            try {
                // Jackson 反序列化时可能是 LinkedHashMap 列表，做一次强转
                return new com.fasterxml.jackson.databind.ObjectMapper()
                        .convertValue(list, new TypeReference<List<CategoryDTO>>() {});
            } catch (Exception ignore) {
                // 反序列化失败降级到查库
            }
        }

        // 2. 查库
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        List<Category> categories = categoryMapper.selectList(wrapper);
        List<CategoryDTO> dtos = categories.stream().map(this::toDTO).collect(Collectors.toList());

        // 3. 回写缓存
        try {
            redisTemplate.opsForValue().set(CACHE_KEY_ALL, dtos, Duration.ofSeconds(cacheTtlSeconds));
        } catch (Exception e) {
            log.warn("写入分类缓存失败: {}", e.getMessage());
        }

        return dtos;
    }

    @Override
    public Category findByCode(String code) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getCategoryCode, code);
        return categoryMapper.selectOne(wrapper);
    }

    private CategoryDTO toDTO(Category c) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCode(c.getCategoryCode());
        dto.setName(c.getName());
        dto.setDescription(c.getDescription());
        dto.setIcon(c.getIcon());
        dto.setColor(c.getColor());
        dto.setSort(c.getSort());
        dto.setPostCount(c.getPostCount());
        return dto;
    }
}
