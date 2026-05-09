package cn.kong.cosmos.auth.mapper;

import cn.kong.cosmos.auth.entity.AuthUnlock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 全局认证解锁 Mapper
 */
@Mapper
public interface AuthUnlockMapper extends BaseMapper<AuthUnlock> {
}
