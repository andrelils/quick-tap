package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.dto.MerchantCreateRequest;
import com.quicktap.dto.MerchantUpdateRequest;
import com.quicktap.dto.PageResponse;
import com.quicktap.entity.Merchant;
import com.quicktap.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商户管理接口控制器
 * IMPORTANT: Static routes must be defined BEFORE dynamic routes
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant")

public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    // ============================================================================
    // STATIC ROUTES - All static routes must come before dynamic routes
    // ============================================================================

    /**
     * 获取商户列表（管理员可见）
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<PageResponse<Merchant>> getMerchantList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取商户列表: pageNum={}, pageSize={}", pageNum, pageSize);
        List<Merchant> list = merchantService.getMerchantList(pageNum, pageSize);
        Long total = merchantService.getMerchantCount();
        PageResponse<Merchant> pageResponse = PageResponse.of(list, pageNum, pageSize, total);
        return ApiResponse.success("获取成功", pageResponse);
    }

    /**
     * 按审核状态获取商户列表
     * STATIC ROUTE: Must appear before /{id}
     */
    @GetMapping("/audit-status/{auditStatus}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<PageResponse<Merchant>> getMerchantByAuditStatus(
            @PathVariable Integer auditStatus,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("按审核状态获取商户: auditStatus={}, pageNum={}, pageSize={}", auditStatus, pageNum, pageSize);
        List<Merchant> list = merchantService.getMerchantByAuditStatus(auditStatus, pageNum, pageSize);
        Long total = merchantService.getMerchantCount();
        PageResponse<Merchant> pageResponse = PageResponse.of(list, pageNum, pageSize, total);
        return ApiResponse.success("获取成功", pageResponse);
    }

    /**
     * 创建商户（仅管理员可操作；商户自助入驻请走专门的公开入驻接口）
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Merchant> createMerchant(@RequestBody MerchantCreateRequest request) {
        log.info("创建商户: name={}", request.getName());
        Merchant merchant = merchantService.createMerchant(request);
        return ApiResponse.success("创建成功", merchant);
    }

    // ============================================================================
    // DYNAMIC ROUTES - All dynamic routes must come AFTER static routes
    // ============================================================================

    /**
     * 获取商户详情
     * DYNAMIC ROUTE: Must appear AFTER all static routes like /list, /audit-status/*
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    public ApiResponse<Merchant> getMerchantById(@PathVariable Integer id) {
        log.info("获取商户详情: id={}", id);
        Merchant merchant = merchantService.getMerchantById(id);
        return ApiResponse.success(merchant);
    }

    /**
     * 更新商户信息
     * DYNAMIC ROUTE: Must appear AFTER all static routes
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    public ApiResponse<Merchant> updateMerchant(@PathVariable Integer id,
                                                @RequestBody MerchantUpdateRequest request) {
        log.info("更新商户: id={}", id);
        Merchant merchant = merchantService.updateMerchant(id, request);
        return ApiResponse.success("更新成功", merchant);
    }

    /**
     * 审核商户（统一端点）
     * 支持 status 参数: approve/reject
     * DYNAMIC ROUTE: Must appear AFTER all static routes
     */
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> auditMerchant(
            @PathVariable Integer id,
            @RequestParam String status) {
        log.info("审核商户: id={}, status={}", id, status);

        if ("approve".equalsIgnoreCase(status)) {
            merchantService.approveMerchant(id);
            return ApiResponse.success("审核通过");
        } else if ("reject".equalsIgnoreCase(status)) {
            merchantService.rejectMerchant(id);
            return ApiResponse.success("已拒绝");
        } else {
            return ApiResponse.badRequest("无效的审核状态");
        }
    }

    /**
     * 审核通过商户
     * 仅管理员可以操作
     * DYNAMIC ROUTE: Must appear AFTER all static routes
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> approveMerchant(@PathVariable Integer id) {
        log.info("审核通过商户: id={}", id);
        merchantService.approveMerchant(id);
        return ApiResponse.success("审核通过");
    }

    /**
     * 审核拒绝商户
     * 仅管理员可以操作
     * DYNAMIC ROUTE: Must appear AFTER all static routes
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> rejectMerchant(@PathVariable Integer id) {
        log.info("审核拒绝商户: id={}", id);
        merchantService.rejectMerchant(id);
        return ApiResponse.success("已拒绝");
    }

    /**
     * 禁用商户
     * 仅管理员可以操作
     * DYNAMIC ROUTE: Must appear AFTER all static routes
     */
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> disableMerchant(@PathVariable Integer id) {
        log.info("禁用商户: id={}", id);
        merchantService.disableMerchant(id);
        return ApiResponse.success("禁用成功");
    }

    /**
     * 启用商户
     * 仅管理员可以操作
     * DYNAMIC ROUTE: Must appear AFTER all static routes
     */
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> enableMerchant(@PathVariable Integer id) {
        log.info("启用商户: id={}", id);
        merchantService.enableMerchant(id);
        return ApiResponse.success("启用成功");
    }

    /**
     * 删除商户
     * 仅超级管理员可以操作
     * DYNAMIC ROUTE: Must appear AFTER all static routes
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> deleteMerchant(@PathVariable Integer id) {
        log.info("删除商户: id={}", id);
        merchantService.deleteMerchant(id);
        return ApiResponse.success("删除成功");
    }
}
