package cn.kong.cosmos.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.kong.cosmos.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
