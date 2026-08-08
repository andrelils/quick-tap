package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.security.SecurityUtil;
import com.quicktap.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MERCHANT')")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final SecurityUtil securityUtil;

    private boolean isMerchantRole() {
        return "merchant".equals(securityUtil.getCurrentRole());
    }

    /**
     * 获取系统概览统计
     * 包括: 总商户数、总用户数、总订单数、收入等
     * 商家角色仅返回自身数据，避免泄露全局指标
     */
    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> getOverview() {
        log.info("获取系统概览统计");
        if (isMerchantRole()) {
            Long merchantId = securityUtil.getCurrentMerchantId();
            if (merchantId != null) {
                Map<String, Object> own = statisticsService.getMerchantStatistics(merchantId.intValue());
                Map<String, Object> overview = new HashMap<>();
                overview.put("totalMerchants", 1);
                overview.put("totalUsers", own.get("users"));
                overview.put("totalOrders", own.get("orders"));
                overview.put("totalRevenue", own.get("revenue"));
                overview.put("totalScans", 0);
                return ApiResponse.success(overview);
            }
            return ApiResponse.success(emptyOverview());
        }
        Map<String, Object> overview = statisticsService.getSystemOverview();
        return ApiResponse.success(overview);
    }

    private Map<String, Object> emptyOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalMerchants", 0);
        overview.put("totalUsers", 0);
        overview.put("totalOrders", 0);
        overview.put("totalRevenue", 0.0);
        overview.put("totalScans", 0);
        return overview;
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

        if (isMerchantRole()) {
            // 商家仅返回自身订单/收入趋势（无全局用户维度）
            LocalDate start = startDate == null ? LocalDate.now().minusMonths(1) : LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate end = endDate == null ? LocalDate.now() : LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);
            Long merchantId = securityUtil.getCurrentMerchantId();
            if (merchantId == null) {
                return ApiResponse.success(emptyTrend(start, end));
            }
            Map<String, Object> trend = statisticsService.getMerchantTrend(start, end, merchantId.intValue());
            trend.putIfAbsent("startDate", start.toString());
            trend.putIfAbsent("endDate", end.toString());
            return ApiResponse.success(trend);
        }

        LocalDate start = startDate == null ?
                LocalDate.now().minusMonths(1) :
                LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate end = endDate == null ?
                LocalDate.now() :
                LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);

        Map<String, Object> trendData = statisticsService.getTrendData(start, end);
        return ApiResponse.success(trendData);
    }

    private Map<String, Object> emptyTrend(LocalDate start, LocalDate end) {
        Map<String, Object> trend = new HashMap<>();
        trend.put("startDate", start.toString());
        trend.put("endDate", end.toString());
        trend.put("dates", new ArrayList<>());
        trend.put("orders", new ArrayList<>());
        trend.put("revenue", new ArrayList<>());
        trend.put("newUsers", new ArrayList<>());
        return trend;
    }

    /**
     * 获取单个商户的统计数据
     */
    @GetMapping("/merchant/{merchantId}")
    public ApiResponse<Map<String, Object>> getMerchantStatistics(
            @PathVariable Integer merchantId) {
        log.info("获取商户统计: merchantId={}", merchantId);
        if (isMerchantRole()) {
            Long ownMerchantId = securityUtil.getCurrentMerchantId();
            if (ownMerchantId == null || ownMerchantId.intValue() != merchantId) {
                return ApiResponse.forbidden("无权访问其他商户的统计数据");
            }
        }
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
        if (isMerchantRole()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("limit", limit);
            empty.put("topByRevenue", new ArrayList<>());
            empty.put("topByOrders", new ArrayList<>());
            return ApiResponse.success(empty);
        }
        Map<String, Object> topMerchants = statisticsService.getTopMerchants(limit);
        return ApiResponse.success(topMerchants);
    }

    /**
     * 获取 AI 使用统计
     */
    @GetMapping("/ai-stats")
    public ApiResponse<Map<String, Object>> getAIStatistics() {
        log.info("获取 AI 使用统计");
        if (isMerchantRole()) {
            Map<String, Object> aiStats = new HashMap<>();
            aiStats.put("totalGenerations", 0);
            aiStats.put("generationsByType", new HashMap<>());
            aiStats.put("merchantAIUsage", new ArrayList<>());
            return ApiResponse.success(aiStats);
        }
        Map<String, Object> aiStats = new HashMap<>();
        aiStats.put("totalGenerations", statisticsService.getTotalAIGenerations());
        aiStats.put("generationsByType", statisticsService.getAIGenerationsByType());
        aiStats.put("merchantAIUsage", statisticsService.getMerchantAIUsage());
        return ApiResponse.success(aiStats);
    }
}
