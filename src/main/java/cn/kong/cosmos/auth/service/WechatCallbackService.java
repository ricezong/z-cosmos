package cn.kong.cosmos.auth.service;

/**
 * 微信公众号回调服务接口
 */
public interface WechatCallbackService {
    
    /**
     * 处理微信公众号消息回调
     * @param xmlMessage 微信XML消息体
     * @return 回复XML
     */
    String handleWechatCallback(String xmlMessage);
}