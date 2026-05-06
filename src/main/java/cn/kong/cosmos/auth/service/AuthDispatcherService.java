package cn.kong.cosmos.auth.service;

import cn.kong.cosmos.auth.dto.resp.AuthTokenResp;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 策略模式路由分发器：按 type 匹配 LoginStrategy，类型安全转换后执行
 */
@Service
public class AuthDispatcherService {

    private final Map<String, LoginStrategy<?>> strategyMap;

    public AuthDispatcherService(List<LoginStrategy> strategies) {
        this.strategyMap = new HashMap<>();
        for (LoginStrategy strategy : strategies) {
            this.strategyMap.putIfAbsent(strategy.type(), strategy);
        }
    }

    public AuthTokenResp dispatch(String type, Object param) {
        LoginStrategy<?> strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的登录类型：" + type);
        }
        // 类型安全转换
        Class<?> paramType = strategy.paramType();
        if (!paramType.isInstance(param)) {
            throw new IllegalArgumentException("登录参数类型不匹配，期望：" + paramType.getSimpleName());
        }
        return ((LoginStrategy<Object>) strategy).executeLogin(param);
    }
}
