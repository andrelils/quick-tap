package com.quicktap.security;

import com.quicktap.common.ErrorCode;
import com.quicktap.exception.BusinessException;
import org.springframework.stereotype.Component;

/**
 * 资源所有权校验工具
 *
 * 用于防止商户横向越权：MERCHANT 角色只能操作自己名下的资源。
 * SUPER_ADMIN / ADMIN 拥有全局权限，直接放行。
 *
 * 典型用法（在 Service 层 update/delete 资源前调用）：
 *   ownershipChecker.checkMerchant(resource.getMerchantId());
 */
@Component
public class OwnershipChecker {

    private final SecurityUtil securityUtil;

    public OwnershipChecker(SecurityUtil securityUtil) {
        this.securityUtil = securityUtil;
    }

    /**
     * 校验当前登录用户是否有权操作指定商户名下的资源。
     * - SUPER_ADMIN / ADMIN：直接放行
     * - MERCHANT：resourceMerchantId 必须等于当前登录商户 ID
     * - 其他角色：拒绝
     *
     * @param resourceMerchantId 资源所属商户 ID
     * @throws BusinessException 当商户无权操作该资源时抛出 403
     */
    public void checkMerchant(Long resourceMerchantId) {
        String role = securityUtil.getCurrentRole();
        // 管理员拥有全局权限（角色大小写兼容：DB 中可能存小写）
        if ("SUPER_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
            return;
        }
        // 商户只能操作自己的资源
        if ("MERCHANT".equalsIgnoreCase(role)) {
            Long currentMerchantId = securityUtil.getCurrentMerchantId();
            if (currentMerchantId == null || !currentMerchantId.equals(resourceMerchantId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作其他商户的资源");
            }
            return;
        }
        // 其他角色一律拒绝
        throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该资源");
    }

    /**
     * 校验当前登录商户只能操作自己的商户信息。
     * 用于 MerchantController.updateMerchant：商户只能改自己。
     *
     * @param merchantId 路径上传入的商户 ID
     */
    public void checkSelfMerchant(Long merchantId) {
        String role = securityUtil.getCurrentRole();
        if ("SUPER_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
            return;
        }
        if ("MERCHANT".equalsIgnoreCase(role)) {
            Long currentMerchantId = securityUtil.getCurrentMerchantId();
            if (currentMerchantId == null || !currentMerchantId.equals(merchantId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "商户只能修改自己的信息");
            }
            return;
        }
        throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该商户");
    }

    /**
     * 获取当前登录用户 ID
     * 仅适用于 USER 角色用户
     *
     * @return 当前登录用户 ID，如果未登录或非 USER 角色则返回 null
     */
    public Long getCurrentUserId() {
        String role = securityUtil.getCurrentRole();
        if (!"USER".equalsIgnoreCase(role)) {
            return null;
        }
        try {
            return securityUtil.getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
