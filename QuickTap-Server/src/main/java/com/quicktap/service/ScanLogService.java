package com.quicktap.service;

import com.quicktap.dto.ScanLogDTO;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 扫码日志Service
 */
public interface ScanLogService {

    /**
     * 记录扫码事件
     */
    ScanLogDTO recordScan(Long userId, Long deviceId, Long merchantId);

    /**
     * 获取设备的扫码记录
     */
    List<ScanLogDTO> getDeviceScanLogs(Long deviceId);

    /**
     * 获取用户的扫码记录
     */
    List<ScanLogDTO> getUserScanLogs(Long userId);

    /**
     * 获取设备的扫码统计
     */
    Long getDeviceScanCount(Long deviceId);

    /**
     * 获取设备在时间范围内的扫码统计
     */
    Long getDeviceScanCountByTime(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取用户的扫码统计
     */
    Long getUserScanCount(Long userId);

    /**
     * 清空指定时间之前的扫码日志
     */
    void deleteScanLogsBeforeDate(LocalDateTime beforeDate);
}
