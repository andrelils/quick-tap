package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.dto.PageResponse;
import com.quicktap.entity.AiGenerateRecord;
import com.quicktap.security.SecurityUtil;
import com.quicktap.service.AiGenerateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * AI 内容生成控制器
 * 提供文本、图片、视频生成功能
 * 匹配 Node.js: POST /api//ai-generate/*
 */
@Slf4j
@RestController
@RequestMapping("/api")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AiGenerateController {

    private final AiGenerateService aiGenerateService;
    private final SecurityUtil securityUtil;

    /**
     * 文本内容生成（商户端/管理员端）
     * 匹配 Node.js: POST /api/merchant/ai-generate/text
     */
    @PostMapping("/merchant/ai-generate/text")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<AiGenerateRecord> generateText(
            @RequestParam(required = false) Integer merchantId,
            @NotBlank(message = "提示词不能为空") @RequestParam String prompt) {
        Integer mid = resolveMerchantId(merchantId);
        log.info("文本生成请求: merchantId={}, prompt={}", mid, prompt);
        if (mid == null) {
            return ApiResponse.badRequest("缺少 merchantId 参数");
        }
        AiGenerateRecord result = aiGenerateService.generateText(mid, prompt);
        return ApiResponse.success("生成成功", result);
    }

    /**
     * 图片内容生成（商户端/管理员端）
     * 匹配 Node.js: POST /api/merchant/ai-generate/image
     */
    @PostMapping("/merchant/ai-generate/image")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<AiGenerateRecord> generateImage(
            @RequestParam(required = false) Integer merchantId,
            @NotBlank(message = "提示词不能为空") @RequestParam String prompt) {
        Integer mid = resolveMerchantId(merchantId);
        log.info("图片生成请求: merchantId={}, prompt={}", mid, prompt);
        if (mid == null) {
            return ApiResponse.badRequest("缺少 merchantId 参数");
        }
        AiGenerateRecord result = aiGenerateService.generateImage(mid, prompt);
        return ApiResponse.success("生成成功", result);
    }

    /**
     * 视频内容生成（商户端/管理员端）
     * 匹配 Node.js: POST /api/merchant/ai-generate/video
     */
    @PostMapping("/merchant/ai-generate/video")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<AiGenerateRecord> generateVideo(
            @RequestParam(required = false) Integer merchantId,
            @NotBlank(message = "提示词不能为空") @RequestParam String prompt) {
        Integer mid = resolveMerchantId(merchantId);
        log.info("视频生成请求: merchantId={}, prompt={}", mid, prompt);
        if (mid == null) {
            return ApiResponse.badRequest("缺少 merchantId 参数");
        }
        AiGenerateRecord result = aiGenerateService.generateVideo(mid, prompt);
        return ApiResponse.success("生成成功", result);
    }

    /**
     * 获取生成历史（商户端/管理员端）
     * 匹配 Node.js: GET /api/merchant/ai-generate/history
     */
    @GetMapping("/merchant/ai-generate/history")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<PageResponse<AiGenerateRecord>> getHistory(
            @RequestParam(required = false) Integer merchantId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Integer mid = resolveMerchantId(merchantId);
        log.info("获取生成历史: merchantId={}, type={}", mid, type);
        if (mid == null) {
            PageResponse<AiGenerateRecord> empty = PageResponse.<AiGenerateRecord>builder()
                    .list(java.util.Collections.emptyList()).pageNum(pageNum).pageSize(pageSize).total(0L).build();
            return ApiResponse.success("获取成功", empty);
        }
        PageResponse<AiGenerateRecord> result = aiGenerateService.getGenerationHistory(
                mid, type, pageNum, pageSize);
        return ApiResponse.success("获取成功", result);
    }

    /**
     * 获取生成记录详情（商户端/管理员端）
     * 匹配 Node.js: GET /api/merchant/ai-generate/:recordId
     */
    @GetMapping("/merchant/ai-generate/{recordId}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<AiGenerateRecord> getRecord(
            @NotBlank(message = "记录ID不能为空") @PathVariable String recordId) {
        log.info("获取生成记录: recordId={}", recordId);
        AiGenerateRecord record = aiGenerateService.getRecord(recordId);
        return ApiResponse.success("获取成功", record);
    }

    /**
     * 删除生成记录（商户端/管理员端）
     * 匹配 Node.js: DELETE /api/merchant/ai-generate/:recordId
     */
    @DeleteMapping("/merchant/ai-generate/{recordId}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Void> deleteRecord(
            @NotBlank(message = "记录ID不能为空") @PathVariable String recordId) {
        log.info("删除生成记录: recordId={}", recordId);
        aiGenerateService.deleteRecord(recordId);
        return ApiResponse.success("删除成功");
    }

    /**
     * 获取 AI 生成统计（管理员端）
     * 匹配 Node.js: GET /api/admin/ai-generate/statistics
     */
    @GetMapping("/admin/ai-generate/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> getStatistics() {
        log.info("获取 AI 生成统计");
        Map<String, Object> stats = aiGenerateService.getStatistics();
        return ApiResponse.success("获取成功", stats);
    }

    /**
     * 辅助方法：解析 merchantId
     * 优先使用参数传入的 merchantId，否则从 token 中取当前登录的商户ID
     */
    private Integer resolveMerchantId(Integer merchantId) {
        if (merchantId != null && merchantId > 0) {
            return merchantId;
        }
        try {
            Long fromToken = securityUtil.getCurrentMerchantId();
            return fromToken != null ? fromToken.intValue() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
