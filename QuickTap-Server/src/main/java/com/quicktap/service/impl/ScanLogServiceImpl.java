package com.quicktap.service.impl;

import com.quicktap.entity.ScanLog;
import com.quicktap.dto.ScanLogDTO;
import com.quicktap.repository.ScanLogRepository;
import com.quicktap.service.ScanLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 扫码日志Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ScanLogServiceImpl implements ScanLogService {

    private final ScanLogRepository scanLogRepository;

    @Override
    public ScanLogDTO recordScan(Long userId, Long deviceId, Long merchantId) {
        ScanLog logs = ScanLog.builder()
                .userId(userId)
                .deviceId(deviceId)
                .merchantId(merchantId)
                .createdAt(LocalDateTime.now())
                .build();

        ScanLog saved = scanLogRepository.save(logs);
        log.info("Recorded scan: userId={}, deviceId={}, merchantId={}", userId, deviceId, merchantId);
        return convertToDTO(saved);
    }

    @Override
    public List<ScanLogDTO> getDeviceScanLogs(Long deviceId) {
        return scanLogRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScanLogDTO> getUserScanLogs(Long userId) {
        // 需要在Repository中添加查询方法
        throw new UnsupportedOperationException("需要在Repository中添加查询方法支持");
    }

    @Override
    public Long getDeviceScanCount(Long deviceId) {
        return scanLogRepository.countByDeviceId(deviceId);
    }

    @Override
    public Long getDeviceScanCountByTime(Long deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return scanLogRepository.countByDeviceIdAndCreatedAtBetween(deviceId, startTime, endTime);
    }

    @Override
    public Long getUserScanCount(Long userId) {
        return scanLogRepository.countByUserId(userId);
    }

    @Override
    public void deleteScanLogsBeforeDate(LocalDateTime beforeDate) {
        log.info("Deleting scan logs before: {}", beforeDate);
        // 需要在Repository中添加删除方法
    }

    private ScanLogDTO convertToDTO(ScanLog scanLog) {
        return ScanLogDTO.builder()
                .id(scanLog.getId())
                .userId(scanLog.getUserId())
                .deviceId(scanLog.getDeviceId())
                .merchantId(scanLog.getMerchantId())
                .createdAt(scanLog.getCreatedAt())
                .build();
    }
}
