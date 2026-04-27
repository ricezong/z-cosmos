package cn.kong.cosmos.auth.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 微信回调前置过滤：
 * GET  - 微信服务器验证（signature 校验）
 * POST - 验签 + XML 解密 → 解析为 WxMpXmlMessage → 注入 RequestAttribute 供 Controller 使用
 */
@Slf4j
@Component
public class WechatCallbackFilter implements Filter {

    private static final String WECHAT_MSG_ATTR = "wechatMessage";
    private static final String CALLBACK_PATH = "/auth/wechat/callback";

    private final WxMpService wxMpService;

    public WechatCallbackFilter(WxMpService wxMpService) {
        this.wxMpService = wxMpService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        if (!path.contains(CALLBACK_PATH)) {
            chain.doFilter(request, response);
            return;
        }

        String method = httpRequest.getMethod();

        if ("GET".equalsIgnoreCase(method)) {
            // GET 请求：微信服务器验证
            handleServerVerification(httpRequest, httpResponse);
        } else if ("POST".equalsIgnoreCase(method)) {
            // POST 请求：验签 + 解密 + 解析
            handleMessageProcessing(httpRequest, httpResponse, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * GET 请求处理：微信服务器地址验证
     * 微信会发送 signature, timestamp, nonce, echostr 四个参数
     * 校验 signature 通过后原样返回 echostr
     */
    private void handleServerVerification(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String signature = request.getParameter("signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String echoStr = request.getParameter("echostr");

        if (echoStr == null) {
            log.warn("微信服务器验证缺少 echoStr 参数");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 使用 WxJava SDK 校验签名
        if (wxMpService.checkSignature(timestamp, nonce, signature)) {
            log.info("微信服务器验证成功");
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write(echoStr);
            response.getWriter().flush();
        } else {
            log.warn("微信服务器验证失败：signature 校验不通过");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    /**
     * POST 请求处理：验签 + 解密 XML 消息体 → 注入 WxMpXmlMessage
     */
    private void handleMessageProcessing(HttpServletRequest request, HttpServletResponse response,
                                          FilterChain chain) throws IOException, ServletException {
        String signature = request.getParameter("signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String msgSignature = request.getParameter("msg_signature"); // 加密模式下的消息签名

        // 读取请求体 XML
        String requestBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);

        if (requestBody == null || requestBody.isBlank()) {
            log.warn("微信回调请求体为空");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            WxMpXmlMessage inMessage;

            // 判断是否加密模式（有 msg_signature 且配置了 aesKey）
            if (msgSignature != null && !msgSignature.isBlank()
                    && wxMpService.getWxMpConfigStorage().getAesKey() != null
                    && !wxMpService.getWxMpConfigStorage().getAesKey().isBlank()) {
                // 加密模式：先验签再解密
                inMessage = WxMpXmlMessage.fromEncryptedXml(requestBody, wxMpService.getWxMpConfigStorage(),
                        timestamp, nonce, msgSignature);
                log.debug("微信消息解密成功：MsgType={}", inMessage.getMsgType());
            } else {
                // 明文模式：仅验签
                if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
                    log.warn("微信消息验签失败");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                inMessage = WxMpXmlMessage.fromXml(requestBody);
                log.debug("微信消息解析成功（明文）：MsgType={}", inMessage.getMsgType());
            }

            // 注入解析后的消息到 RequestAttribute，供 Controller 使用
            request.setAttribute(WECHAT_MSG_ATTR, inMessage);
            chain.doFilter(request, response);

        } catch (Exception e) {
            log.error("微信消息解析失败：{}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
