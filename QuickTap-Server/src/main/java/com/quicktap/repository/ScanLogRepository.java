package com.quicktap.repository;

import com.quicktap.entity.ScanLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 扫码日志Repository
 */
@Repository
public interface ScanLogRepository extends JpaRepository<ScanLog, Long> {
    
    /**
     * 根据设备ID查询扫码记录，按创建时间倒序
     */
    List<ScanLog> findByDeviceIdOrderByCreatedAtDesc(Long deviceId);

    /**
     * 根据用户ID查询扫码记录
     */
    List<ScanLog> findByUserId(Long userId);

    /**
     * 统计设备扫码次数
     */
    long countByDeviceId(Long deviceId);

    /**
     * 统计设备在指定时间范围内的扫码次数
     */
    long countByDeviceIdAndCreatedAtBetween(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计用户扫码次数
     */
    long countByUserId(Long userId);

    /**
     * 删除指定日期前的扫码记录
     */
    void deleteByCreatedAtBefore(LocalDateTime beforeDate);
}
