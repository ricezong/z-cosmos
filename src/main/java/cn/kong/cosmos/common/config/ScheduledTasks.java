package cn.kong.cosmos.common.config;

import cn.kong.cosmos.auth.mapper.AuthUnlockMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

/**
 * 定时任务配置
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledTasks {

    private final AuthUnlockMapper authUnlockMapper;

    /**
     * 每小时清理过期的解锁记录
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanExpiredUnlockRecords() {
        int deleted = authUnlockMapper.delete(
            new LambdaQueryWrapper<cn.kong.cosmos.auth.entity.AuthUnlock>()
                .lt(cn.kong.cosmos.auth.entity.AuthUnlock::getExpiresAt, LocalDateTime.now())
        );
        if (deleted > 0) {
            log.info("清理过期解锁记录: {} 条", deleted);
        }
    }
}