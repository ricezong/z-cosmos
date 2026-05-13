package cn.kong.cosmos.auth.service.impl;

import cn.kong.cosmos.auth.dto.resp.UnlockCodeDTO;
import cn.kong.cosmos.auth.dto.resp.UnlockStatusDTO;
import cn.kong.cosmos.auth.entity.AuthUnlock;
import cn.kong.cosmos.auth.mapper.AuthUnlockMapper;
import cn.kong.cosmos.auth.service.AuthUnlockService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTextMessage;
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
    
    @Override
    @Transactional
    public UnlockCodeDTO requestUnlockCode(String deviceId, String moduleType) {
        // 生成6位随机口令（数字）
        String unlockCode = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        
        // 删除旧的未解锁记录（全局解锁，不需要target_id）
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
        
        // 缓存到Redis（快速查询）
        String redisKey = "unlock:code:" + unlockCode;
        String redisValue = deviceId + ":" + moduleType;
        redisTemplate.opsForValue().set(redisKey, redisValue, 12, TimeUnit.HOURS);
        
        log.info("生成全局解锁口令: deviceId={}, moduleType={}, code={}", 
            deviceId, moduleType, unlockCode);
        
        return new UnlockCodeDTO(unlockCode, 43200); // 43200秒 = 12小时
    }
    
    @Override
    public UnlockStatusDTO checkUnlockStatus(String deviceId, String moduleType) {
        // 检查全局解锁状态（不需要target_id）
        AuthUnlock authUnlock = authUnlockMapper.selectOne(
            new LambdaQueryWrapper<AuthUnlock>()
                .eq(AuthUnlock::getDeviceId, deviceId)
                .eq(AuthUnlock::getModuleType, moduleType)
                .eq(AuthUnlock::getStatus, 1)
                .gt(AuthUnlock::getExpiresAt, LocalDateTime.now())
        );
        
        return new UnlockStatusDTO(authUnlock != null);
    }
    
    @Override
    @Transactional
    public boolean validateUnlockCode(String deviceId, String unlockCode) {
        // 从Redis中查找口令
        String redisKey = "unlock:code:" + unlockCode;
        String redisValue = redisTemplate.opsForValue().get(redisKey);

        if (redisValue == null) {
            return false;
        }

        // 解析Redis中的设备和模块信息
        String[] parts = redisValue.split(":");
        String redisDeviceId = parts[0];
        String moduleType = parts[1];

        // 验证设备ID匹配
        if (!deviceId.equals(redisDeviceId)) {
            return false;
        }

        // 验证口令并更新数据库状态
        boolean updated = authUnlockMapper.update(
            null,
            new LambdaQueryWrapper<AuthUnlock>()
                .eq(AuthUnlock::getDeviceId, deviceId)
                .eq(AuthUnlock::getModuleType, moduleType)
                .eq(AuthUnlock::getUnlockCode, unlockCode)
                .eq(AuthUnlock::getStatus, 0)
        ) > 0;

        if (updated) {
            // 删除Redis中的口令缓存（口令验证后立即失效）
            redisTemplate.delete(redisKey);
            log.info("解锁成功: deviceId={}, moduleType={}, code={}", deviceId, moduleType, unlockCode);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public String handleWechatCallback(String xmlMessage) {
        try {
            // 使用 WxJava 库解析微信XML消息
            WxMpXmlMessage wxMessage = WxMpXmlMessage.fromXml(xmlMessage);
            
            // 提取用户发送的口令
            String content = wxMessage.getContent() != null ? wxMessage.getContent().trim() : "";
            
            if (content.length() != 6 || !content.matches("\\d{6}")) {
                // 口令格式不正确
                return buildWechatReply(wxMessage, "口令格式不正确，请输入6位数字口令。");
            }
            
            // 从Redis中查找口令
            String redisKey = "unlock:code:" + content;
            String redisValue = redisTemplate.opsForValue().get(redisKey);
            
            if (redisValue == null) {
                return buildWechatReply(wxMessage, "口令无效或已过期，请返回页面重新获取。");
            }
            
            // 解析Redis中的设备和模块信息
            String[] parts = redisValue.split(":");
            String deviceId = parts[0];
            String moduleType = parts[1];
            
            // 验证口令并更新数据库状态
            boolean updated = authUnlockMapper.update(
                null,
                new LambdaQueryWrapper<AuthUnlock>()
                    .eq(AuthUnlock::getDeviceId, deviceId)
                    .eq(AuthUnlock::getModuleType, moduleType)
                    .eq(AuthUnlock::getUnlockCode, content)
                    .eq(AuthUnlock::getStatus, 0)
            ) > 0;
            
            if (updated) {
                // 删除Redis中的口令缓存（口令验证后立即失效）
                redisTemplate.delete(redisKey);
                log.info("解锁成功: deviceId={}, moduleType={}, code={}", deviceId, moduleType, content);
                return buildWechatReply(wxMessage, "解锁成功！12小时内可访问所有内容。");
            } else {
                return buildWechatReply(wxMessage, "口令已被使用或不存在，请返回页面重新获取。");
            }
            
        } catch (Exception e) {
            log.error("处理微信回调失败", e);
            return "";
        }
    }
    
    /**
     * 构建微信回复消息
     */
    private String buildWechatReply(WxMpXmlMessage wxMessage, String content) {
        WxMpXmlOutTextMessage outMessage = WxMpXmlOutMessage.TEXT()
            .content(content)
            .fromUser(wxMessage.getToUser())
            .toUser(wxMessage.getFromUser())
            .build();
        return outMessage.toXml();
    }
}
