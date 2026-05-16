package cn.kong.cosmos.auth.service;

import cn.kong.cosmos.auth.dto.resp.UnlockCodeDTO;
import cn.kong.cosmos.auth.dto.resp.UnlockStatusDTO;

/**
 * 全局认证解锁服务接口
 */
public interface AuthUnlockService {
    
    /**
     * 生成动态口令
     */
    UnlockCodeDTO requestUnlockCode(String deviceId, String moduleType);
    
    /**
     * 检查解锁状态
     */
    UnlockStatusDTO checkUnlockStatus(String deviceId, String moduleType);

    /**
     * 微信公众号回调验证
     */
    String handleWechatCallback(String xmlMessage);
}
