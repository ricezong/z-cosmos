package cn.kong.cosmos.auth.controller;

import cn.kong.cosmos.auth.dto.req.UnlockRequestDTO;
import cn.kong.cosmos.auth.dto.resp.UnlockCodeDTO;
import cn.kong.cosmos.auth.dto.resp.UnlockStatusDTO;
import cn.kong.cosmos.auth.service.AuthUnlockService;
import cn.kong.cosmos.common.core.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 全局认证解锁控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/unlock")
@RequiredArgsConstructor
public class AuthUnlockController {
    
    private final AuthUnlockService authUnlockService;
    
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
    

}
