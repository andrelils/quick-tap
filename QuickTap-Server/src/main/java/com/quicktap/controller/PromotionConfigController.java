package com.quicktap.controller;

import com.quicktap.dto.MerchantPromotionConfigDTO;
import com.quicktap.dto.ApiResponse;
import com.quicktap.service.PromotionConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 商户推广配置Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/promotion-config")
@RequiredArgsConstructor
public class PromotionConfigController {

    private final PromotionConfigService promotionConfigService;

    /**
     * 创建推广配置
     */
    @PostMapping("/merchant/{merchantId}")
    public ApiResponse<MerchantPromotionConfigDTO> createConfig(
            @PathVariable Long merchantId,
            @Valid @RequestBody MerchantPromotionConfigDTO.CreateMerchantConfigRequest request) {
        try {
            MerchantPromotionConfigDTO result = promotionConfigService.createConfig(merchantId, request);
            return ApiResponse.success("创建成功", result);
        } catch (Exception e) {
            log.error("Error creating promotion config", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新推广配置
     */
    @PutMapping("/{configId}")
    public ApiResponse<MerchantPromotionConfigDTO> updateConfig(
            @PathVariable Long configId,
            @Valid @RequestBody MerchantPromotionConfigDTO.UpdateMerchantConfigRequest request) {
        try {
            MerchantPromotionConfigDTO result = promotionConfigService.updateConfig(configId, request);
            return ApiResponse.success("更新成功", result);
        } catch (Exception e) {
            log.error("Error updating promotion config", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除推广配置
     */
    @DeleteMapping("/{configId}")
    public ApiResponse<Void> deleteConfig(@PathVariable Long configId) {
        try {
            promotionConfigService.deleteConfig(configId);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            log.error("Error deleting promotion config", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取商户的激活推广配置
     */
    @GetMapping("/merchant/{merchantId}/active")
    public ApiResponse<List<MerchantPromotionConfigDTO>> getMerchantActiveConfigs(@PathVariable Long merchantId) {
        try {
            List<MerchantPromotionConfigDTO> result = promotionConfigService.getMerchantActiveConfigs(merchantId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Error getting merchant active configs", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取商户的平台推广配置
     */
    @GetMapping("/merchant/{merchantId}/platforms")
    public ApiResponse<List<MerchantPromotionConfigDTO>> getMerchantPlatformConfigs(@PathVariable Long merchantId) {
        try {
            List<MerchantPromotionConfigDTO> result = promotionConfigService.getMerchantPlatformConfigs(merchantId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Error getting merchant platform configs", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取商户的优惠券推广配置
     */
    @GetMapping("/merchant/{merchantId}/coupons")
    public ApiResponse<List<MerchantPromotionConfigDTO>> getMerchantCouponConfigs(@PathVariable Long merchantId) {
        try {
            List<MerchantPromotionConfigDTO> result = promotionConfigService.getMerchantCouponConfigs(merchantId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Error getting merchant coupon configs", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取配置详情
     */
    @GetMapping("/{configId}")
    public ApiResponse<MerchantPromotionConfigDTO> getConfigDetail(@PathVariable Long configId) {
        try {
            MerchantPromotionConfigDTO result = promotionConfigService.getConfigDetail(configId);
            if (result == null) {
                return ApiResponse.error("配置不存在");
            }
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Error getting config detail", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 启用配置
     */
    @PutMapping("/{configId}/enable")
    public ApiResponse<Void> enableConfig(@PathVariable Long configId) {
        try {
            promotionConfigService.enableConfig(configId);
            return ApiResponse.success("启用成功", null);
        } catch (Exception e) {
            log.error("Error enabling config", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 禁用配置
     */
    @PutMapping("/{configId}/disable")
    public ApiResponse<Void> disableConfig(@PathVariable Long configId) {
        try {
            promotionConfigService.disableConfig(configId);
            return ApiResponse.success("禁用成功", null);
        } catch (Exception e) {
            log.error("Error disabling config", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
