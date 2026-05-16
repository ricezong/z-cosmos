package cn.kong.cosmos.auth.service.impl;

import cn.kong.cosmos.auth.dto.resp.UnlockCodeDTO;
import cn.kong.cosmos.auth.dto.resp.UnlockStatusDTO;
import cn.kong.cosmos.auth.entity.AuthUnlock;
import cn.kong.cosmos.auth.mapper.AuthUnlockMapper;
import cn.kong.cosmos.auth.service.AuthUnlockService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 全局认证解锁服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUnlockServiceImpl implements AuthUnlockService {
    
    private final AuthUnlockMapper authUnlockMapper;
    private final StringRedisTemplate redisTemplate;
    
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private String generateUnlockCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CODE_CHARS.charAt((int) (Math.random() * CODE_CHARS.length())));
        }
        return sb.toString();
    }

    @Override
    @Transactional
    public UnlockCodeDTO requestUnlockCode(String deviceId, String moduleType) {
        // 5分钟防重复：检查该设备是否在5分钟内已生成过口令
        String cooldownKey = "unlock:cooldown:" + deviceId + ":" + moduleType;
        String existingCode = redisTemplate.opsForValue().get(cooldownKey);
        if (existingCode != null) {
            log.info("5分钟内重复请求，返回已有口令: deviceId={}, code={}", deviceId, existingCode);
            return new UnlockCodeDTO(existingCode, 43200);
        }

        // 生成6位混合口令（大写字母+数字，排除易混淆字符0/O/1/I/L）
        String unlockCode = generateUnlockCode();
        
        // 删除旧的未解锁记录
        authUnlockMapper.delete(
            new LambdaQueryWrapper<AuthUnlock>()
                .eq(AuthUnlock::getDeviceId, deviceId)
                .eq(AuthUnlock::getModuleType, moduleType)
                .eq(AuthUnlock::getStatus, 0)
        );
        
        // 保存到数据库（12小时过期）
        AuthUnlock authUnlock = new AuthUnlock();
        authUnlock.setDeviceId(deviceId);
        authUnlock.setModuleType(moduleType);
        authUnlock.setUnlockCode(unlockCode);
        authUnlock.setStatus(0);
        authUnlock.setExpiresAt(LocalDateTime.now().plusHours(12));
        authUnlockMapper.insert(authUnlock);
        
        // 缓存到Redis（快速查询，12小时过期）
        String redisKey = "unlock:code:" + unlockCode;
        String redisValue = deviceId + ":" + moduleType;
        redisTemplate.opsForValue().set(redisKey, redisValue, 12, TimeUnit.HOURS);

        // 设置5分钟防重复冷却
        redisTemplate.opsForValue().set(cooldownKey, unlockCode, 5, TimeUnit.MINUTES);
        
        log.info("生成全局解锁口令: deviceId={}, moduleType={}, code={}", deviceId, moduleType, unlockCode);
        return new UnlockCodeDTO(unlockCode, 43200);
    }
    
    @Override
    public UnlockStatusDTO checkUnlockStatus(String deviceId, String moduleType) {
        AuthUnlock authUnlock = authUnlockMapper.selectOne(
            new LambdaQueryWrapper<AuthUnlock>()
                .eq(AuthUnlock::getDeviceId, deviceId)
                .eq(AuthUnlock::getModuleType, moduleType)
                .eq(AuthUnlock::getStatus, 1)
                .gt(AuthUnlock::getExpiresAt, LocalDateTime.now())
        );
        return new UnlockStatusDTO(authUnlock != null);
    }
}