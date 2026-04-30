package cn.kong.cosmos.biz.community.mapper;

import cn.kong.cosmos.biz.community.entity.Post;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子 Mapper
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {
}
