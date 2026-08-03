package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 统计分析控制器
 * 提供系统概览、趋势分析等统计功能
 * 匹配 Node.js: GET /api/admin/statistics/*
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/statistics")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取系统概览统计
     * 包括: 总商户数、总用户数、总订单数、收入等
     */
    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> getOverview() {
        log.info("获取系统概览统计");
        Map<String, Object> overview = statisticsService.getSystemOverview();
        return ApiResponse.success(overview);
    }

    /**
     * 获取趋势数据
     * 支持自定义时间范围统计
     */
    @GetMapping("/trend")
    public ApiResponse<Map<String, Object>> getTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("获取趋势数据: startDate={}, endDate={}", startDate, endDate);

        LocalDate start = startDate == null ?
                LocalDate.now().minusMonths(1) :
                LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate end = endDate == null ?
                LocalDate.now() :
                LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);

        Map<String, Object> trendData = statisticsService.getTrendData(start, end);
        return ApiResponse.success(trendData);
    }

    /**
     * 获取单个商户的统计数据
     */
    @GetMapping("/merchant/{merchantId}")
    public ApiResponse<Map<String, Object>> getMerchantStatistics(
            @PathVariable Integer merchantId) {
        log.info("获取商户统计: merchantId={}", merchantId);
        Map<String, Object> stats = statisticsService.getMerchantStatistics(merchantId);
        return ApiResponse.success(stats);
    }

    /**
     * 获取排名前N的商户
     */
    @GetMapping("/top/merchants")
    public ApiResponse<Map<String, Object>> getTopMerchants(
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取排名前{}的商户", limit);
        Map<String, Object> topMerchants = statisticsService.getTopMerchants(limit);
        return ApiResponse.success(topMerchants);
    }

    /**
     * 获取 AI 使用统计
     */
    @GetMapping("/ai-stats")
    public ApiResponse<Map<String, Object>> getAIStatistics() {
        log.info("获取 AI 使用统计");
        Map<String, Object> aiStats = new HashMap<>();
        aiStats.put("totalGenerations", statisticsService.getTotalAIGenerations());
        aiStats.put("generationsByType", statisticsService.getAIGenerationsByType());
        aiStats.put("merchantAIUsage", statisticsService.getMerchantAIUsage());
        return ApiResponse.success(aiStats);
    }
}
