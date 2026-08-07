package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.dto.AiConfigDTO;
import com.quicktap.dto.CreateOrUpdateAiConfigRequest;
import com.quicktap.dto.PageResponse;
import com.quicktap.security.SecurityUtil;
import com.quicktap.service.AiConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * AI 配置 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class AiConfigController {

    @Autowired
    private AiConfigService aiConfigService;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 获取商户 AI 配置
     * - MERCHANT：不传 merchantId 时从 token 取
     * - ADMIN/SUPER_ADMIN：可通过 merchantId 参数指定查询的商户
     */
    @GetMapping("/merchant/ai-config")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<AiConfigDTO> getMerchantConfig(@RequestParam(required = false) Long merchantId) {
        Long mid = resolveMerchantId(merchantId);
        log.info("获取商户 AI 配置 | merchantId: {}", mid);

        AiConfigDTO result = aiConfigService.getConfigByMerchantId(mid);
        return ApiResponse.success("AI 配置获取成功", result);
    }

    /**
     * 设置商户 AI 配置
     * - MERCHANT：不传 merchantId 时从 token 取
     * - ADMIN/SUPER_ADMIN：可通过 merchantId 参数指定目标商户
     */
    @PutMapping("/merchant/ai-config")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<AiConfigDTO> updateMerchantConfig(
            @RequestParam(required = false) Long merchantId,
            @Valid @RequestBody CreateOrUpdateAiConfigRequest request) {
        Long mid = resolveMerchantId(merchantId);
        log.info("更新商户 AI 配置 | merchantId: {}", mid);
        if (mid == null) {
            return ApiResponse.badRequest("缺少 merchantId 参数");
        }

        AiConfigDTO result = aiConfigService.createOrUpdateMerchantConfig(mid, request);
        return ApiResponse.success("AI 配置更新成功", result);
    }

    /**
     * 辅助方法：解析 merchantId
     * 优先使用参数传入的 merchantId，否则从 token 中取当前登录的商户ID
     */
    private Long resolveMerchantId(Long merchantId) {
        if (merchantId != null && merchantId > 0) {
            return merchantId;
        }
        try {
            return securityUtil.getCurrentMerchantId();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取全局 AI 配置
     * 对应 Node: GET /api/admin/ai/config
     */
    @GetMapping("/admin/ai-config")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ApiResponse<AiConfigDTO> getGlobalConfig() {
        log.info("获取全局 AI 配置");

        AiConfigDTO result = aiConfigService.getGlobalConfig();
        return ApiResponse.success("全局 AI 配置获取成功", result);
    }

    /**
     * 管理员查询指定商户的 AI 配置
     * 前端 ai.js#getAiConfig(merchantId) 调用
     */
    @GetMapping("/admin/ai-config/{merchantId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<AiConfigDTO> getMerchantConfigByAdmin(@PathVariable Long merchantId) {
        log.info("管理员查询商户 AI 配置 | merchantId: {}", merchantId);
        if (merchantId == null || merchantId <= 0) {
            return ApiResponse.badRequest("merchantId 不合法");
        }
        AiConfigDTO result = aiConfigService.getConfigByMerchantId(merchantId);
        return ApiResponse.success("AI 配置获取成功", result);
    }

    /**
     * 商户 AI 配置总览（分页）
     * 前端 ai.js#getMerchantConfigList 调用，用于"商家配置总览"表格
     */
    @GetMapping("/admin/ai-config/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<PageResponse<AiConfigDTO>> getMerchantConfigList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取商户 AI 配置总览 | pageNum: {}, pageSize: {}", pageNum, pageSize);
        PageResponse<AiConfigDTO> result = aiConfigService.getMerchantConfigList(pageNum, pageSize);
        return ApiResponse.success("获取成功", result);
    }

    /**
     * 商家 AI 配置总览（所有商家，含 AI 配置与语料统计）
     * 前端"商家配置总览"表格使用
     */
    @GetMapping("/admin/ai-config/overview")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<PageResponse<Map<String, Object>>> getMerchantOverview(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取商家 AI 配置总览 | pageNum: {}, pageSize: {}", pageNum, pageSize);
        PageResponse<Map<String, Object>> result = aiConfigService.getMerchantOverview(pageNum, pageSize);
        return ApiResponse.success("获取成功", result);
    }

    /**
     * 更新全局 AI 配置
     * 对应 Node: PUT /api/admin/ai/config
     */
    @PutMapping("/admin/ai-config")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ApiResponse<AiConfigDTO> updateGlobalConfig(@Valid @RequestBody CreateOrUpdateAiConfigRequest request) {
        log.info("更新全局 AI 配置");

        AiConfigDTO result = aiConfigService.updateGlobalConfig(request);
        return ApiResponse.success("全局 AI 配置更新成功", result);
    }

    /**
     * 删除商户 AI 配置
     */
    @DeleteMapping("/admin/ai-config/{merchantId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ApiResponse<Void> deleteMerchantConfig(@PathVariable Long merchantId) {
        log.info("删除商户 AI 配置 | merchantId: {}", merchantId);

        aiConfigService.deleteMerchantConfig(merchantId);
        return ApiResponse.success("AI 配置删除成功");
    }
}
