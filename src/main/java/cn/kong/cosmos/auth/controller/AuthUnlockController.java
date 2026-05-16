package cn.kong.cosmos.auth.controller;

import cn.kong.cosmos.auth.dto.req.UnlockRequestDTO;
import cn.kong.cosmos.auth.dto.req.UnlockValidateDTO;
import cn.kong.cosmos.auth.dto.resp.UnlockCodeDTO;
import cn.kong.cosmos.auth.dto.resp.UnlockStatusDTO;
import cn.kong.cosmos.auth.dto.resp.UnlockValidateResultDTO;
import cn.kong.cosmos.auth.service.AuthUnlockService;
import cn.kong.cosmos.common.core.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * 全局认证解锁控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/unlock")
@RequiredArgsConstructor
public class AuthUnlockController {
    
    private final AuthUnlockService authUnlockService;

    @Value("${wechat.mp.token:kong}")
    private String wechatToken;
    
    /**
     * 请求动态口令（限流：每分钟最多5次）
     */
    @PostMapping("/request")
    public Result<UnlockCodeDTO> requestUnlockCode(@RequestBody UnlockRequestDTO request) {
        UnlockCodeDTO code = authUnlockService.requestUnlockCode(
            request.getDeviceId(),
            request.getModuleType()
        );
        return Result.success(code);
    }
    
    /**
     * 检查解锁状态
     */
    @GetMapping("/status")
    public Result<UnlockStatusDTO> checkUnlockStatus(
            @RequestParam String deviceId,
            @RequestParam String moduleType) {
        UnlockStatusDTO status = authUnlockService.checkUnlockStatus(deviceId, moduleType);
        return Result.success(status);
    }
    

    /**
     * 微信公众号服务器配置验证（GET请求）
     * 微信会发送 signature/timestamp/nonce/echostr 四个参数
     * 验证流程：将token+timestamp+nonce排序后SHA1，与signature比对
     */
    @GetMapping("/wechat/callback")
    public String verifyWechatSignature(
            @RequestParam String signature,
            @RequestParam String timestamp,
            @RequestParam String nonce,
            @RequestParam String echostr) {
        log.info("微信验签: signature={}, timestamp={}, nonce={}", signature, timestamp, nonce);
        if (checkSignature(signature, timestamp, nonce)) {
            log.info("微信验签成功，返回echostr");
            return echostr;
        }
        log.warn("微信验签失败");
        return "failed";
    }

    /**
     * 微信公众号消息回调（POST请求）
     * 处理用户发送的口令消息
     */
    @PostMapping("/wechat/callback")
    public String handleWechatCallback(
            @RequestParam String signature,
            @RequestParam String timestamp,
            @RequestParam String nonce,
            @RequestBody String xmlMessage) {
        // 先验证签名
        if (!checkSignature(signature, timestamp, nonce)) {
            log.warn("微信消息回调签名验证失败");
            return "failed";
        }
        return authUnlockService.handleWechatCallback(xmlMessage);
    }

    /**
     * 微信签名验证
     * 将token、timestamp、nonce按字典序排列，拼接后SHA1，与signature比对
     */
    private boolean checkSignature(String signature, String timestamp, String nonce) {
        if (wechatToken == null || wechatToken.isEmpty()) {
            log.error("微信token未配置");
            return false;
        }
        String[] arr = {wechatToken, timestamp, nonce};
        Arrays.sort(arr);
        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            sb.append(s);
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            String computed = bytesToHex(digest);
            return computed.equals(signature);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-1算法不可用", e);
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
