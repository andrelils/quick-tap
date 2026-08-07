package com.quicktap.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * IP 地址工具类
 *
 * 功能：
 * - 从请求中提取客户端真实 IP 地址
 * - 处理代理场景（X-Forwarded-For, X-Real-IP 等）
 * - 支持负载均衡器配置
 * - IP 地址验证
 */
@Slf4j
@Component
public class IpUtil {

    /**
     * 获取客户端真实 IP 地址
     *
     * 优先级顺序：
     * 1. X-Forwarded-For（CDN/负载均衡器标准头）
     * 2. X-Real-IP（Nginx 常用）
     * 3. CF-Connecting-IP（Cloudflare）
     * 4. request.getRemoteAddr()（直连）
     *
     * @param request HTTP 请求对象
     * @return 客户端 IP 地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        // 1. 检查 X-Forwarded-For（可能包含多个 IP）
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // 取第一个 IP（客户端真实 IP）
            String[] ips = xForwardedFor.split(",");
            if (ips.length > 0) {
                String clientIp = ips[0].trim();
                if (isValidIp(clientIp)) {
                    return clientIp;
                }
            }
        }

        // 2. 检查 X-Real-IP（Nginx）
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            if (isValidIp(xRealIp)) {
                return xRealIp;
            }
        }

        // 3. 检查 CF-Connecting-IP（Cloudflare）
        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (cfConnectingIp != null && !cfConnectingIp.isEmpty()) {
            if (isValidIp(cfConnectingIp)) {
                return cfConnectingIp;
            }
        }

        // 4. 检查 Proxy-Client-IP（代理）
        String proxyClientIp = request.getHeader("Proxy-Client-IP");
        if (proxyClientIp != null && !proxyClientIp.isEmpty()) {
            if (isValidIp(proxyClientIp)) {
                return proxyClientIp;
            }
        }

        // 5. 检查 WL-Proxy-Client-IP（WebLogic）
        String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
        if (wlProxyClientIp != null && !wlProxyClientIp.isEmpty()) {
            if (isValidIp(wlProxyClientIp)) {
                return wlProxyClientIp;
            }
        }

        // 6. 直接从 request 获取（直连或最后一跳）
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null && !remoteAddr.isEmpty()) {
            return remoteAddr;
        }

        return "unknown";
    }

    /**
     * 验证 IP 地址是否有效
     *
     * @param ip IP 地址字符串
     * @return 是否为有效的 IPv4 或 IPv6 地址
     */
    private static boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }

        // IPv4 验证（简单检查）
        if (isValidIpv4(ip)) {
            return true;
        }

        // IPv6 验证（简单检查）
        return isValidIpv6(ip);
    }

    /**
     * 验证 IPv4 地址
     */
    private static boolean isValidIpv4(String ip) {
        // IPv4 格式：0.0.0.0 - 255.255.255.255
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        for (String part : parts) {
            if (part.isEmpty()) {
                return false;
            }
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    /**
     * 验证 IPv6 地址（简化检查）
     */
    private static boolean isValidIpv6(String ip) {
        // 简单检查：包含冒号即认为可能是 IPv6
        return ip.contains(":");
    }

    /**
     * 检查 IP 是否为本地地址
     */
    public static boolean isLocalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        return ip.startsWith("127.") || ip.startsWith("192.168.") || ip.startsWith("10.") ||
               ip.startsWith("172.") || "localhost".equalsIgnoreCase(ip) || "::1".equals(ip);
    }

    /**
     * 检查 IP 是否为私有网络地址
     */
    public static boolean isPrivateIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        return ip.startsWith("127.") ||      // 127.0.0.0 - 127.255.255.255
               ip.startsWith("10.") ||       // 10.0.0.0 - 10.255.255.255
               ip.startsWith("172.") ||      // 172.16.0.0 - 172.31.255.255
               ip.startsWith("192.168.") ||  // 192.168.0.0 - 192.168.255.255
               ip.startsWith("fc") ||        // IPv6 私有地址
               ip.startsWith("fd") ||        // IPv6 私有地址
               "::1".equals(ip);             // IPv6 环回
    }
}
