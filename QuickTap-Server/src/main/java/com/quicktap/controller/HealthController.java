package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查接口
 * 不需要认证，用于监控应用状态
 */
@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HealthController {

    /**
     * 应用健康检查
     * 用于负载均衡器和监控系统检测应用状态
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Application is running");
    }

    /**
     * 获取应用基本信息
     */
    @GetMapping("/info")
    public ApiResponse<?> info() {
        return ApiResponse.success(
            "data",
            new Object() {
                public String version = "1.0.0";
                public String name = "QuickTap Server";
                public String description = "QuickTap Backend Service";
                public long timestamp = System.currentTimeMillis();
            }
        );
    }

    /**
     * 获取服务器时间
     */
    @GetMapping("/time")
    public ApiResponse<Long> getServerTime() {
        return ApiResponse.success(System.currentTimeMillis());
    }
}
