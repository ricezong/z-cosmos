package cn.kong.cosmos.auth.service.impl;

import cn.kong.cosmos.auth.entity.AuthUnlock;
import cn.kong.cosmos.auth.mapper.AuthUnlockMapper;
import cn.kong.cosmos.auth.service.WechatCallbackService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTextMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 微信公众号回调服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatCallbackServiceImpl implements WechatCallbackService {
    
    private final AuthUnlockMapper authUnlockMapper;
    private final StringRedisTemplate redisTemplate;
    
    @Override
    @Transactional
    public String handleWechatCallback(String xmlMessage) {
        try {
            WxMpXmlMessage wxMessage = WxMpXmlMessage.fromXml(xmlMessage);
            String content = wxMessage.getContent() != null ? wxMessage.getContent().trim() : "";
            
            if (content.length() != 6 || !content.matches("[A-Z0-9]{6}")) {
                return buildWechatReply(wxMessage, "口令格式不正确，请输入6位字母数字口令。");
            }
            
            String redisKey = "unlock:code:" + content;
            String redisValue = redisTemplate.opsForValue().get(redisKey);
            
            if (redisValue == null) {
                return buildWechatReply(wxMessage, "口令无效或已过期，请返回页面重新获取。");
            }
            
            String[] parts = redisValue.split(":");
            String deviceId = parts[0];
            String moduleType = parts[1];
            
            AuthUnlock updateEntity = new AuthUnlock();
            updateEntity.setStatus(1);
            boolean updated = authUnlockMapper.update(
                updateEntity,
                new LambdaQueryWrapper<AuthUnlock>()
                    .eq(AuthUnlock::getDeviceId, deviceId)
                    .eq(AuthUnlock::getModuleType, moduleType)
                    .eq(AuthUnlock::getUnlockCode, content)
                    .eq(AuthUnlock::getStatus, 0)
            ) > 0;
            
            if (updated) {
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
    
    private String buildWechatReply(WxMpXmlMessage wxMessage, String content) {
        WxMpXmlOutTextMessage outMessage = WxMpXmlOutMessage.TEXT()
            .content(content)
            .fromUser(wxMessage.getToUser())
            .toUser(wxMessage.getFromUser())
            .build();
        return outMessage.toXml();
    }
}