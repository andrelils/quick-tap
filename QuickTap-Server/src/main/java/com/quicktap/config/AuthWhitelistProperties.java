package com.quicktap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限白名单配置
 * 用于配置不需要 JWT Token 验证的公开接口
 */
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthWhitelistProperties {

    /**
     * 白名单 URL 列表
     */
    private List<String> whitelist = new ArrayList<>();

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    /**
     * 判断 URL 是否在白名单中
     * 支持路径模式匹配（如 /api/health 匹配 /api/health/check）
     */
    public boolean isWhitelisted(String requestUri) {
        if (whitelist == null || whitelist.isEmpty()) {
            return false;
        }
        return whitelist.stream().anyMatch(uri -> {
            // 精确匹配或路径前缀匹配
            return requestUri.equals(uri) || requestUri.startsWith(uri);
        });
    }
}
