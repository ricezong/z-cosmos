package cn.kong.cosmos.auth.service.helper;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 微信带参二维码 & 登录状态机：生成/轮询/事件绑定
 * 使用 WxMpService 生成真实带参二维码
 */
@Slf4j
@Service
public class WechatQrService {

    private static final String QR_PREFIX = "wechat:qr:";
    /** 二维码等待扫码 TTL：5 分钟 */
    private static final long QR_TTL_MINUTES = 5;
    /** 扫码成功后状态保留 1 分钟 */
    private static final long SCANNED_TTL_MINUTES = 1;
    /** 带参二维码场景前缀 */
    private static final String SCENE_PREFIX = "login_";

    private final RedisTemplate<String, Object> redisTemplate;
    private final WxMpService wxMpService;

    public WechatQrService(RedisTemplate<String, Object> redisTemplate, WxMpService wxMpService) {
        this.redisTemplate = redisTemplate;
        this.wxMpService = wxMpService;
    }

    /**
     * 生成带参二维码 Session
     * @return 包含 sessionId 和二维码 ticket URL 的结果
     */
    public QrSessionResult generateQrSession() {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        String sceneStr = SCENE_PREFIX + sessionId;

        // 写入 Redis 状态
        String key = QR_PREFIX + sessionId;
        redisTemplate.opsForValue().set(key, "WAIT_SCAN", QR_TTL_MINUTES, TimeUnit.MINUTES);

        String qrCodeUrl;
        try {
            // 调用微信 API 生成临时带参二维码（5 分钟过期，与 Redis TTL 一致）
            WxMpQrCodeTicket ticket = wxMpService.getQrcodeService()
                    .qrCodeCreateTmpTicket(sceneStr, (int) QR_TTL_MINUTES * 60);
            qrCodeUrl = String.format(
                    "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=%s",
                    ticket.getTicket());
            log.info("微信带参二维码创建成功：sessionId={}", sessionId);
        } catch (WxErrorException e) {
            log.error("微信带参二维码创建失败，回退为模拟 URL：{}", e.getMessage());
            // 回退：使用 sessionId 作为模拟 ticket（开发调试用）
            qrCodeUrl = String.format(
                    "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=%s",
                    sessionId);
        }

        return new QrSessionResult(sessionId, qrCodeUrl);
    }

    /**
     * 获取登录状态
     */
    public String getLoginStatus(String sessionId) {
        String key = QR_PREFIX + sessionId;
        Object status = redisTemplate.opsForValue().get(key);
        if (status == null) {
            return "EXPIRED";
        }
        return status.toString();
    }

    /**
     * 更新登录状态（扫码后）
     * 扫码成功后缩短 TTL，避免状态长期驻留
     */
    public void updateStatus(String sessionId, String status) {
        String key = QR_PREFIX + sessionId;
        long ttl = "SUCCESS".equals(status) ? SCANNED_TTL_MINUTES : QR_TTL_MINUTES;
        redisTemplate.opsForValue().set(key, status, ttl, TimeUnit.MINUTES);
    }

    /**
     * 绑定 OpenID 到 Session（扫码/关注事件触发时调用）
     */
    public void bindOpenId(String sessionId, String openId) {
        String key = QR_PREFIX + sessionId + ":openid";
        redisTemplate.opsForValue().set(key, openId, QR_TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 获取绑定的 OpenID
     */
    public String getOpenId(String sessionId) {
        String key = QR_PREFIX + sessionId + ":openid";
        Object openId = redisTemplate.opsForValue().get(key);
        if (openId == null) {
            return null;
        }
        return openId.toString();
    }

    /**
     * 从场景值中提取 Session ID
     * 场景值格式：login_{sessionId}
     */
    public String extractSessionIdFromScene(String sceneStr) {
        if (sceneStr != null && sceneStr.startsWith(SCENE_PREFIX)) {
            return sceneStr.substring(SCENE_PREFIX.length());
        }
        return null;
    }

    /**
     * 二维码 Session 结果
     */
    public record QrSessionResult(String sessionId, String qrCodeUrl) {}
}
