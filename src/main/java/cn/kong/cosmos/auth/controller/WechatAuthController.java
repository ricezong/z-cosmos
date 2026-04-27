package cn.kong.cosmos.auth.controller;

import cn.kong.cosmos.auth.dto.req.WechatLoginStatusReq;
import cn.kong.cosmos.auth.dto.resp.AuthTokenResp;
import cn.kong.cosmos.auth.service.AuthDispatcherService;
import cn.kong.cosmos.auth.service.helper.WechatQrService;
import cn.kong.cosmos.auth.util.CookieTokenHelper;
import cn.kong.cosmos.common.core.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信扫码登录控制器：公众号带参二维码方案
 *
 * 流程：
 * 1. 前端调用 /qr/generate 获取二维码 URL + sessionId
 * 2. 前端展示二维码并轮询 /qr/status 检查登录状态
 * 3. 用户扫码后，微信推送事件到 /callback（由 WechatCallbackFilter 验签解密）
 * 4. Controller 处理扫码事件，更新状态 + 绑定 OpenID
 * 5. 前端轮询到 SUCCESS 后获取 Token
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/wechat")
public class WechatAuthController {

    private final WechatQrService wechatQrService;
    private final AuthDispatcherService authDispatcherService;
    private final CookieTokenHelper cookieTokenHelper;

    public WechatAuthController(WechatQrService wechatQrService,
                                AuthDispatcherService authDispatcherService,
                                CookieTokenHelper cookieTokenHelper) {
        this.wechatQrService = wechatQrService;
        this.authDispatcherService = authDispatcherService;
        this.cookieTokenHelper = cookieTokenHelper;
    }

    /**
     * 生成微信登录二维码
     */
    @PostMapping("/qr/generate")
    public Result<Map<String, String>> generateQrCode() {
        WechatQrService.QrSessionResult session = wechatQrService.generateQrSession();

        Map<String, String> result = new HashMap<>();
        result.put("sessionId", session.sessionId());
        result.put("qrCodeUrl", session.qrCodeUrl());

        return Result.success(result);
    }

    /**
     * 轮询微信登录状态
     * 状态流转：WAIT_SCAN → SCANNED → SUCCESS / EXPIRED
     */
    @PostMapping("/qr/status")
    public Result<Map<String, Object>> checkLoginStatus(@RequestBody WechatLoginStatusReq req,
                                                         HttpServletResponse response) {
        String sessionId = req.getSessionId();
        String status = wechatQrService.getLoginStatus(sessionId);

        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("sessionId", sessionId);

        if ("SUCCESS".equals(status)) {
            // 登录成功：获取绑定的 OpenID，执行微信登录策略签发 Token
            String openId = wechatQrService.getOpenId(sessionId);
            if (openId != null) {
                Map<String, String> params = new HashMap<>();
                params.put("openId", openId);
                AuthTokenResp tokenResp = authDispatcherService.dispatch("WECHAT", params);
                cookieTokenHelper.setTokenCookie(response, tokenResp.getAccessToken());
                result.put("userId", tokenResp.getUserId());
                result.put("nickname", tokenResp.getNickname());
            }
        } else if ("EXPIRED".equals(status)) {
            result.put("message", "二维码已过期，请刷新重试");
        }

        return Result.success(result);
    }

    /**
     * 微信事件回调接收（公众号推送）
     * GET 请求由 WechatCallbackFilter 处理服务器验证
     * POST 请求已由 Filter 验签解密，此处处理扫码/关注业务逻辑
     */
    @PostMapping("/callback")
    public String handleWechatCallback(HttpServletRequest request) {
        Object messageObj = request.getAttribute("wechatMessage");
        if (messageObj == null) {
            log.warn("未找到微信消息，可能验签失败");
            return "error";
        }

        WxMpXmlMessage inMessage = (WxMpXmlMessage) messageObj;
        String msgType = inMessage.getMsgType();
        String event = inMessage.getEvent();
        String openId = inMessage.getFromUser();

        log.info("微信回调：msgType={}, event={}, openId={}", msgType, event, openId);

        try {
            switch (msgType != null ? msgType : "") {
                case "event" -> handleEvent(event, inMessage);
                default -> log.debug("忽略非事件消息：msgType={}", msgType);
            }
        } catch (Exception e) {
            log.error("微信回调处理失败：{}", e.getMessage(), e);
        }

        return "success";
    }

    /**
     * 处理微信事件
     */
    private void handleEvent(String event, WxMpXmlMessage inMessage) {
        String openId = inMessage.getFromUser();

        switch (event != null ? event : "") {
            case "SCAN" -> {
                // 已关注用户扫码：EventKey 为场景值
                String sceneStr = inMessage.getEventKey();
                String sessionId = wechatQrService.extractSessionIdFromScene(sceneStr);
                if (sessionId != null) {
                    wechatQrService.updateStatus(sessionId, "SUCCESS");
                    wechatQrService.bindOpenId(sessionId, openId);
                    log.info("已关注用户扫码登录：sessionId={}, openId={}", sessionId, openId);
                }
            }
            case "subscribe" -> {
                // 新关注用户扫码：EventKey 格式为 qrscene_{sceneStr}
                String eventKey = inMessage.getEventKey();
                if (eventKey != null && eventKey.startsWith("qrscene_")) {
                    String sceneStr = eventKey.substring("qrscene_".length());
                    String sessionId = wechatQrService.extractSessionIdFromScene(sceneStr);
                    if (sessionId != null) {
                        wechatQrService.updateStatus(sessionId, "SUCCESS");
                        wechatQrService.bindOpenId(sessionId, openId);
                        log.info("新用户关注扫码登录：sessionId={}, openId={}", sessionId, openId);
                    }
                }
            }
            default -> log.debug("忽略事件：event={}", event);
        }
    }
}
