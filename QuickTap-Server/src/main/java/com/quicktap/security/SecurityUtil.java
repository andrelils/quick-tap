package com.quicktap.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 安全工具类 - 用户上下文访问工具
 *
 * 职责：
 * - 从 Spring Security 上下文中提取当前认证用户的信息
 * - 提供便利方法访问用户 ID、用户名、商户 ID 和角色
 * - 提供基于角色的权限检查便利方法
 *
 * 注意事项：
 * - 所有方法都是线程安全的（基于 Spring Security 的 ThreadLocal 实现）
 * - 当用户未认证时，所有 get 方法返回 null
 * - 应该始终检查 null 值，避免 NullPointerException
 * - UserPrincipal 中的 id/merchantId 为 Integer，本工具类统一返回 Long 以兼容调用方
 */
@Component
public class SecurityUtil {

    /**
     * 获取当前用户的 ID
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal) {
                Integer id = ((UserPrincipal) principal).getId();
                return id != null ? id.longValue() : null;
            }
        }
        return null;
    }

    /**
     * 获取当前用户的用户名
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * 获取当前用户的商户 ID
     */
    public Long getCurrentMerchantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal) {
                Integer merchantId = ((UserPrincipal) principal).getMerchantId();
                return merchantId != null ? merchantId.longValue() : null;
            }
        }
        return null;
    }

    /**
     * 获取当前用户的角色
     */
    public String getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal) {
                return ((UserPrincipal) principal).getRole();
            }
        }
        return null;
    }

    /**
     * 检查当前用户是否是超级管理员
     */
    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(getCurrentRole());
    }

    /**
     * 检查当前用户是否是管理员
     */
    public boolean isAdmin() {
        String role = getCurrentRole();
        return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
    }

    /**
     * 检查当前用户是否是商户
     */
    public boolean isMerchant() {
        return "MERCHANT".equals(getCurrentRole());
    }

    /**
     * 检查当前用户是否是普通用户
     */
    public boolean isUser() {
        return "USER".equals(getCurrentRole());
    }
}
