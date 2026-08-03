package com.quicktap.controller;

import com.quicktap.dto.ScanLogDTO;
import com.quicktap.dto.ApiResponse;
import com.quicktap.service.ScanLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 扫码日志Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/scan-logs")
@RequiredArgsConstructor
public class ScanLogController {

    private final ScanLogService scanLogService;

    /**
     * 记录扫码事件
     */
    @PostMapping("/scan")
    public ApiResponse<ScanLogDTO> recordScan(
            @RequestParam Long userId,
            @RequestParam Long deviceId,
            @RequestParam Long merchantId) {
        try {
            ScanLogDTO result = scanLogService.recordScan(userId, deviceId, merchantId);
            return ApiResponse.success("记录成功", result);
        } catch (Exception e) {
            log.error("Error recording scan", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取设备的扫码记录
     */
    @GetMapping("/device/{deviceId}")
    public ApiResponse<List<ScanLogDTO>> getDeviceScanLogs(@PathVariable Long deviceId) {
        try {
            List<ScanLogDTO> result = scanLogService.getDeviceScanLogs(deviceId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Error getting device scan logs", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户的扫码记录
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<ScanLogDTO>> getUserScanLogs(@PathVariable Long userId) {
        try {
            List<ScanLogDTO> result = scanLogService.getUserScanLogs(userId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Error getting user scan logs", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取设备扫码统计
     */
    @GetMapping("/device/{deviceId}/count")
    public ApiResponse<Long> getDeviceScanCount(@PathVariable Long deviceId) {
        try {
            Long count = scanLogService.getDeviceScanCount(deviceId);
            return ApiResponse.success(count);
        } catch (Exception e) {
            log.error("Error getting device scan count", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取设备在时间范围内的扫码统计
     */
    @GetMapping("/device/{deviceId}/count-by-time")
    public ApiResponse<Long> getDeviceScanCountByTime(
            @PathVariable Long deviceId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            Long count = scanLogService.getDeviceScanCountByTime(deviceId, start, end);
            return ApiResponse.success(count);
        } catch (Exception e) {
            log.error("Error getting device scan count by time", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户扫码统计
     */
    @GetMapping("/user/{userId}/count")
    public ApiResponse<Long> getUserScanCount(@PathVariable Long userId) {
        try {
            Long count = scanLogService.getUserScanCount(userId);
            return ApiResponse.success(count);
        } catch (Exception e) {
            log.error("Error getting user scan count", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
