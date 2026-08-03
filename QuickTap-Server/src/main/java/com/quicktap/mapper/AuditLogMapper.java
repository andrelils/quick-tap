package com.quicktap.mapper;

import com.quicktap.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志 Mapper - XML文件形式
 * SQL语句定义在: src/main/resources/mapper/AuditLogMapper.xml
 */
@Mapper
public interface AuditLogMapper {

    /**
     * 插入审计日志
     */
    void insert(AuditLog auditLog);

    /**
     * 根据ID查询审计日志
     */
    AuditLog selectById(Long id);

    /**
     * 查询用户的审计日志
     */
    List<AuditLog> selectByUserId(Long userId, int limit);

    /**
     * 查询特定事件类型的审计日志
     */
    List<AuditLog> selectByEventType(String eventType, int limit);

    /**
     * 查询失败的登录尝试
     */
    List<AuditLog> selectFailedLoginAttempts(LocalDateTime startTime);

    /**
     * 查询特定IP的操作记录
     */
    List<AuditLog> selectByIpAddress(String ipAddress, int limit);

    /**
     * 查询时间范围内的审计日志
     */
    List<AuditLog> selectByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询敏感操作记录
     */
    List<AuditLog> selectSensitiveOperations(int limit);

    /**
     * 统计指定时间范围内失败的登录尝试次数
     */
    long countFailedLoginAttempts(String username, LocalDateTime startTime);

    /**
     * 检查是否存在异常的并发登录
     */
    long checkConcurrentLogins(Long userId, String currentIpAddress, LocalDateTime recentTime);

    /**
     * 删除过期的审计日志（档案目的保留特定天数的记录）
     */
    int deleteExpiredLogs(LocalDateTime expiryDate);

    /**
     * 归档旧的审计日志
     */
    int archiveLogs(LocalDateTime archiveDate);
}
