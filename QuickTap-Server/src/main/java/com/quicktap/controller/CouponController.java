package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.dto.PageResponse;
import com.quicktap.dto.CouponCreateRequest;
import com.quicktap.entity.Coupon;
import com.quicktap.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 卡券管理控制器
 * IMPORTANT: Static routes must be defined BEFORE dynamic routes
 */
@Slf4j
@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
@Validated
public class CouponController {
    private final CouponService couponService;

    // ============================================================================
    // STATIC ROUTES - All static routes must come before dynamic routes
    // ============================================================================

    @GetMapping("/list")
    public ApiResponse<PageResponse<Coupon>> listCoupons(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer merchantId,
            @RequestParam(required = false) String keyword) {
        // 兼容 ADMIN 端按商家筛选优惠券的能力
        if (merchantId != null && merchantId > 0) {
            PageResponse<Coupon> data = couponService.getMerchantCouponList(merchantId, pageNum, pageSize);
            return ApiResponse.success("获取成功", data);
        }
        PageResponse<Coupon> data = couponService.getCouponList(pageNum, pageSize);
        return ApiResponse.success("获取成功", data);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    public ApiResponse<Coupon> createCoupon(@Valid @RequestBody CouponCreateRequest request) {
        Coupon coupon = new Coupon();
        coupon.setMerchantId(request.getMerchantId());
        coupon.setTitle(request.getTitle());
        coupon.setType(request.getType());
        coupon.setAmount(request.getAmount());
        coupon.setMinAmount(request.getMinAmount());
        coupon.setTotalCount(request.getTotalCount());
        coupon.setStartTime(request.getStartTime());
        coupon.setEndTime(request.getEndTime());
        coupon.setLink(request.getLink());
        coupon.setStatus(1);
        Coupon created = couponService.createCoupon(coupon);
        return ApiResponse.success("创建成功", created);
    }

    // ============================================================================
    // DYNAMIC ROUTES - All dynamic routes must come AFTER static routes
    // ============================================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    public ApiResponse<Coupon> getCoupon(@PathVariable @NotNull Integer id) {
        Coupon coupon = couponService.getCouponById(id);
        return ApiResponse.success("获取成功", coupon);
    }

    @GetMapping("/merchant/{merchantId}")
    public ApiResponse<PageResponse<Coupon>> getMerchantCoupons(
            @PathVariable @NotNull Integer merchantId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<Coupon> data = couponService.getMerchantCouponList(merchantId, pageNum, pageSize);
        return ApiResponse.success("获取成功", data);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    public ApiResponse<Coupon> updateCoupon(
            @PathVariable @NotNull Integer id,
            @Valid @RequestBody CouponCreateRequest request) {
        Coupon coupon = new Coupon();
        coupon.setTitle(request.getTitle());
        coupon.setAmount(request.getAmount());
        coupon.setMinAmount(request.getMinAmount());
        coupon.setStartTime(request.getStartTime());
        coupon.setEndTime(request.getEndTime());
        coupon.setLink(request.getLink());
        Coupon updated = couponService.updateCoupon(id, coupon);
        return ApiResponse.success("更新成功", updated);
    }

    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    public ApiResponse<Coupon> disableCoupon(@PathVariable @NotNull Integer id) {
        Coupon coupon = couponService.disableCoupon(id);
        return ApiResponse.success("禁用成功", coupon);
    }

    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    public ApiResponse<Coupon> enableCoupon(@PathVariable @NotNull Integer id) {
        Coupon coupon = couponService.enableCoupon(id);
        return ApiResponse.success("启用成功", coupon);
    }

    @PutMapping("/{id}/claim")
    @PreAuthorize("hasAnyRole('USER', 'MERCHANT')")
    public ApiResponse<Coupon> claimCoupon(@PathVariable @NotNull Integer id) {
        Coupon coupon = couponService.claimCoupon(id);
        return ApiResponse.success("领取成功", coupon);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> deleteCoupon(@PathVariable @NotNull Integer id) {
        couponService.deleteCoupon(id);
        return ApiResponse.success("删除成功");
    }
}
