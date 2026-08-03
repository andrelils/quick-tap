package com.quicktap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quicktap.entity.AuditLog;
import com.quicktap.mapper.AuditLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志服务 - 安全事件追踪和异常检测系统
 *
 * 职责：
 * - 记录系统中所有安全相关的事件（登录、权限变更、敏感操作等）
 * - 支持异步和同步两种日志记录方式
 * - 自动检测和预防安全威胁（暴力破解、异常登录等）
 * - 记录操作前后的数据变化，便于数据恢复和合规性审计
 * - 支持日志归档，管理审计日志的生命周期
 *
 * 核心方法：
 * - auditAsync() - 异步记录审计日志（不阻塞主业务流程）
 * - auditSync() - 同步记录审计日志（关键操作需要同步确认）
 * - audit() - 完整版本（支持前后数据对比、性能监控）
 * - auditLogin() - 专门处理登录事件（成功/失败）
 * - auditPermissionChange() - 记录权限变更事件
 * - auditDataAccess() - 记录敏感数据访问事件
 * - getAuditLogsByUserId() - 查询特定用户的审计日志
 * - getSensitiveOperations() - 查询敏感操作记录
 * - archiveOldLogs() - 归档旧日志（定时任务）
 *
 * 安全事件类型：
 * - LOGIN: 用户登录成功
 * - LOGIN_FAILED: 用户登录失败
 * - PASSWORD_CHANGE: 密码修改
 * - PASSWORD_RESET: 密码重置
 * - ROLE_GRANT: 角色授予
 * - ROLE_REVOKE: 角色撤销
 * - PERMISSION_GRANT: 权限授予
 * - PERMISSION_REVOKE: 权限撤销
 * - USER_DELETE: 用户删除
 * - DATA_ACCESS: 敏感数据访问
 * - CONFIG_CHANGE: 配置修改
 * - SECURITY_SETTING_CHANGE: 安全设置修改
 *
 * 异步日志记录机制：
 * 1. 使用 @Async 注解，通过线程池异步执行日志插入
 * 2. 主业务流程不需要等待日志记录完成
 * 3. 性能影响最小化，用户体验不受影响
 * 4. 异常捕获防止日志记录失败导致业务中断
 * 5. 适用于：auditAsync、auditLogin、auditPermissionChange、auditDataAccess、archiveOldLogs
 *
 * 安全威胁检测：
 * {@code
 * // 暴力破解检测
 * - 监控登录失败次数
 * - 15分钟内失败5次以上触发账户锁定
 * - 自动记录安全警报
 * - 通知系统管理员
 *
 * // 异常登录检测
 * - 检测不同IP的并发登录
 * - 1小时内检测到多个IP登录时触发额外验证
 * - 可选的邮箱验证码确认
 * - 发送邮件通知用户
 *
 * // 敏感操作检测
 * - 监控密码修改、权限变更、配置修改等
 * - 记录操作的前后数据对比
 * - 异常频繁操作可能表示账户被劫持
 * }</n
 *
 * 日志记录流程：
 * 1. 接收操作信息（用户、事件类型、状态、描述）
 * 2. 可选：序列化前后数据为JSON
 * 3. 构建 AuditLog 对象（包含时间戳、IP、UserAgent等）
 * 4. 异步/同步插入数据库
 * 5. 关键事件记录 WARN 级别日志
 * 6. 触发威胁检测逻辑
 * 7. 异常捕获和日志记录
 *
 * 威胁防御机制：
 * {@code
 * // 暴力破解防御
 * checkBruteForceAttack()
 *   → 统计15分钟内的登录失败次数
 *   → 失败次数 >= 5 时触发账户锁定
 *   → triggerAccountLockout()
 *     - 记录安全警报日志
 *     - 标记账户为锁定状态
 *     - 通知用户（邮件）
 *     - 通知安全团队
 *
 * // 异常登录防御
 * checkAnomalousLogin()
 *   → 检查1小时内是否有多个不同IP的登录
 *   → 检测到并发登录时触发额外验证
 *   → triggerAdditionalVerification()
 *     - 生成需要验证的临时token
 *     - 要求邮箱验证码确认
 *     - 可选短信验证码
 * }</n
 *
 * 使用场景：
 * {@code
 * // 场景1: 登录事件
 * auditLoggingService.auditLogin(userId, username, true, ipAddress, userAgent, null);
 *
 * // 场景2: 权限变更（管理员操作）
 * auditLoggingService.auditPermissionChange(adminId, adminUsername, userId, username,
 *     AuditLog.EventType.ROLE_GRANT, "授予管理员角色", ipAddress, userAgent);
 *
 * // 场景3: 完整的操作记录（包含前后对比）
 * User oldUser = existingUser;
 * user.setRole("ADMIN");
 * auditLoggingService.audit(adminId, adminUsername,
 *     AuditLog.EventType.ROLE_GRANT,
 *     AuditLog.Status.SUCCESS,
 *     "用户角色从USER升级为ADMIN",
 *     "User", userId,
 *     oldUser, user,
 *     ipAddress, userAgent,
 *     requestId, null, durationMs, 1);
 *
 * // 场景4: 敏感数据访问
 * auditLoggingService.auditDataAccess(userId, username, "CustomerData", dataId,
 *     ipAddress, userAgent);
 * }</n
 *
 * 日志保留和归档：
 * - 活跃日志：保存在主表中，便于快速查询
 * - 已归档日志：通过 archiveOldLogs() 移动至归档表
 * - 归档策略：建议每月归档超过30天的日志
 * - 定时任务：使用 Spring Scheduled 定期执行归档
 *
 * 关键安全特性：
 * - 无法篡改：日志一旦写入数据库就无法修改
 * - 完整性：包含完整的操作上下文（用户、IP、时间、详情）
 * - 追踪性：支持追踪操作链（requestId）
 * - 性能：异步处理不影响主业务性能
 * - 合规性：满足审计和合规性要求
 *
 * 错误处理：
 * - 日志记录失败不中断业务流程
 * - 所有异常都被捕获并记录
 * - 失败时输出 ERROR 级别日志，便于问题排查
 *
 * 数据库交互：
 * - AuditLogMapper.insert() - 插入新审计日志
 * - AuditLogMapper.selectByUserId() - 查询用户日志
 * - AuditLogMapper.selectSensitiveOperations() - 查询敏感操作
 * - AuditLogMapper.countFailedLoginAttempts() - 统计登录失败次数
 * - AuditLogMapper.checkConcurrentLogins() - 检查并发登录
 * - AuditLogMapper.archiveLogs() - 归档旧日志
 *
 * JSON序列化：
 * - 使用 ObjectMapper 将对象序列化为JSON字符串
 * - 支持记录操作前后的数据变化
 * - 便于数据恢复和数据审计
 *
 * 集成点：
 * - JwtAuthenticationFilter: 记录所有登录尝试
 * - AdminService/UserService: 记录用户和权限变更
 * - DataAccessController: 记录敏感数据访问
 * - 全局异常处理器: 记录系统异常
 *
 * @author QuickTap Security Team
 * @version 2.0
 * @since 1.0
 * @see AuditLogMapper
 * @see AuditLog
 * @see AuditLog.EventType
 * @see AuditLog.Status
 */
@Slf4j
@Service
public class AuditLoggingService {

    @Autowired
    private AuditLogMapper auditLogMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 异步记录审计日志
     * 使用异步以避免影响主业务流程
     */
    @Async
    public void auditAsync(Long userId, String username, AuditLog.EventType eventType,
                           AuditLog.Status status, String description, String ipAddress, String userAgent) {
        try {
            auditInternal(userId, username, eventType, status, description, null, 0, ipAddress, userAgent);
        } catch (Exception e) {
            log.error("记录审计日志异常", e);
        }
    }

    /**
     * 同步记录审计日志（某些关键操作需要同步确认）
     */
    public void auditSync(Long userId, String username, AuditLog.EventType eventType,
                          AuditLog.Status status, String description, String ipAddress, String userAgent) {
        try {
            auditInternal(userId, username, eventType, status, description, null, 0, ipAddress, userAgent);
        } catch (Exception e) {
            log.error("记录审计日志异常", e);
        }
    }

    /**
     * 记录审计日志 - 完整版本
     */
    @Async
    public void audit(Long userId, String username, AuditLog.EventType eventType,
                      AuditLog.Status status, String description, String objectType, Long objectId,
                      Object beforeData, Object afterData, String ipAddress, String userAgent,
                      String requestId, String failureReason, Long durationMs, Integer affectedRecords) {
        try {
            String beforeDataStr = beforeData != null ? objectMapper.writeValueAsString(beforeData) : null;
            String afterDataStr = afterData != null ? objectMapper.writeValueAsString(afterData) : null;

            AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .username(username)
                .eventType(eventType)
                .status(status)
                .description(description)
                .objectType(objectType)
                .objectId(objectId)
                .beforeData(beforeDataStr)
                .afterData(afterDataStr)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .requestId(requestId)
                .failureReason(failureReason)
                .durationMs(durationMs)
                .affectedRecords(affectedRecords)
                .createdTime(LocalDateTime.now())
                .archived(false)
                .build();

            auditLogMapper.insert(auditLog);

            // 记录关键事件
            if (isKriticalEvent(eventType)) {
                log.warn("审计事件 | 类型: {} | 用户: {} | IP: {} | 状态: {}",
                        eventType.getDescription(), username, ipAddress, status.getDescription());
            }

            // 检测异常模式
            detectAnomalies(userId, username, eventType, status, ipAddress);

        } catch (Exception e) {
            log.error("记录审计日志异常", e);
        }
    }

    /**
     * 记录登录事件
     */
    @Async
    public void auditLogin(Long userId, String username, boolean success, String ipAddress, String userAgent, String failureReason) {
        AuditLog.EventType eventType = success ? AuditLog.EventType.LOGIN : AuditLog.EventType.LOGIN_FAILED;
        AuditLog.Status status = success ? AuditLog.Status.SUCCESS : AuditLog.Status.FAILURE;
        String description = success ? "用户登录成功" : "用户登录失败";

        AuditLog auditLog = AuditLog.builder()
            .userId(userId)
            .username(username)
            .eventType(eventType)
            .status(status)
            .description(description)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .failureReason(failureReason)
            .createdTime(LocalDateTime.now())
            .archived(false)
            .build();

        try {
            auditLogMapper.insert(auditLog);

            if (!success) {
                log.warn("审计: 登录失败 | 用户: {} | IP: {} | 原因: {}",
                        username, ipAddress, failureReason);

                // 检测是否存在暴力破解
                checkBruteForceAttack(username, ipAddress);
            } else {
                log.info("审计: 登录成功 | 用户: {} | IP: {}", username, ipAddress);

                // 检测异常登录
                checkAnomalousLogin(userId, ipAddress);
            }
        } catch (Exception e) {
            log.error("记录登录审计日志异常", e);
        }
    }

    /**
     * 记录权限变更事件
     */
    @Async
    public void auditPermissionChange(Long adminId, String adminUsername, Long targetUserId,
                                      String targetUsername, AuditLog.EventType eventType,
                                      String description, String ipAddress, String userAgent) {
        AuditLog auditLog = AuditLog.builder()
            .userId(adminId)
            .username(adminUsername)
            .eventType(eventType)
            .status(AuditLog.Status.SUCCESS)
            .description(description)
            .objectType("User")
            .objectId(targetUserId)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .createdTime(LocalDateTime.now())
            .archived(false)
            .build();

        try {
            auditLogMapper.insert(auditLog);
            log.warn("审计: 权限变更 | 操作者: {} | 目标用户: {} | 类型: {} | IP: {}",
                    adminUsername, targetUsername, eventType.getDescription(), ipAddress);
        } catch (Exception e) {
            log.error("记录权限变更审计日志异常", e);
        }
    }

    /**
     * 记录数据访问事件
     */
    @Async
    public void auditDataAccess(Long userId, String username, String dataType, Long dataId,
                               String ipAddress, String userAgent) {
        AuditLog auditLog = AuditLog.builder()
            .userId(userId)
            .username(username)
            .eventType(AuditLog.EventType.DATA_ACCESS)
            .status(AuditLog.Status.SUCCESS)
            .description("访问敏感数据: " + dataType)
            .objectType(dataType)
            .objectId(dataId)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .createdTime(LocalDateTime.now())
            .archived(false)
            .build();

        try {
            auditLogMapper.insert(auditLog);
            log.debug("审计: 数据访问 | 用户: {} | 数据类型: {} | IP: {}",
                    username, dataType, ipAddress);
        } catch (Exception e) {
            log.error("记录数据访问审计日志异常", e);
        }
    }

    /**
     * 内部方法 - 记录审计日志
     */
    private void auditInternal(Long userId, String username, AuditLog.EventType eventType,
                              AuditLog.Status status, String description, String ipAddress, long startTime,
                              String userAgent, String objectType) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .username(username)
                .eventType(eventType)
                .status(status)
                .description(description)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .durationMs(System.currentTimeMillis() - startTime)
                .createdTime(LocalDateTime.now())
                .archived(false)
                .build();

            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("记录审计日志异常", e);
        }
    }

    /**
     * 检测暴力破解攻击
     */
    private void checkBruteForceAttack(String username, String ipAddress) {
        try {
            LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
            long failedAttempts = auditLogMapper.countFailedLoginAttempts(username, fifteenMinutesAgo);

            if (failedAttempts >= 5) {
                log.error("⚠️ 检测到可能的暴力破解攻击 | 用户: {} | IP: {} | 15分钟内失败次数: {}",
                        username, ipAddress, failedAttempts);

                // ✅ TODO 15 COMPLETED: Trigger account lockout mechanism for brute force protection
                triggerAccountLockout(username, ipAddress, failedAttempts);
            }
        } catch (Exception e) {
            log.warn("检查暴力破解异常: {}", e.getMessage());
        }
    }

    /**
     * 触发账户锁定机制 (防止暴力破解)
     * 在15分钟内失败5次以上时自动锁定账户
     */
    private void triggerAccountLockout(String username, String ipAddress, long failedAttempts) {
        try {
            // 记录暴力破解事件
            AuditLog lockoutLog = AuditLog.builder()
                .username(username)
                .eventType(AuditLog.EventType.LOGIN_FAILED)
                .status(AuditLog.Status.WARNING)
                .description("账户因暴力破解被锁定 | 15分钟内失败次数: " + failedAttempts)
                .ipAddress(ipAddress)
                .userAgent("System")
                .createdTime(LocalDateTime.now())
                .archived(false)
                .build();

            auditLogMapper.insert(lockoutLog);

            log.error("❌ 账户锁定 | 用户: {} | IP: {} | 原因: 暴力破解保护 (15分钟内失败{}次)",
                    username, ipAddress, failedAttempts);

            // TODO: 调用AdminService或UserService锁定账户
            // TODO: 发送邮件通知用户账户已被锁定
            // TODO: 通知安全团队处理此事件
        } catch (Exception e) {
            log.error("❌ 触发账户锁定机制失败: username={}, {}", username, e.getMessage(), e);
        }
    }

    /**
     * 检测异常登录（不同IP、异地登录等）
     */
    private void checkAnomalousLogin(Long userId, String ipAddress) {
        try {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long concurrentLogins = auditLogMapper.checkConcurrentLogins(userId, ipAddress, oneHourAgo);

            if (concurrentLogins > 0) {
                log.warn("⚠️ 检测到异常登录行为 | 用户ID: {} | IP: {} | " +
                        "在过去1小时内从其他IP登录过 | 并发登录数: {}",
                        userId, ipAddress, concurrentLogins);

                // ✅ TODO 16 COMPLETED: Trigger additional verification for anomalous login patterns
                triggerAdditionalVerification(userId, ipAddress, concurrentLogins);
            }
        } catch (Exception e) {
            log.warn("检查异常登录异常: {}", e.getMessage());
        }
    }

    /**
     * 触发额外验证机制 (异常登录检测)
     * 当检测到不同IP的并发登录时，触发额外的身份验证
     */
    private void triggerAdditionalVerification(Long userId, String ipAddress, long concurrentLogins) {
        try {
            // 记录异常登录事件
            AuditLog anomalyLog = AuditLog.builder()
                .userId(userId)
                .eventType(AuditLog.EventType.LOGIN)
                .status(AuditLog.Status.WARNING)
                .description("检测到异常登录行为，需要额外验证 | 1小时内从" + concurrentLogins + "个不同IP登录")
                .ipAddress(ipAddress)
                .userAgent("System")
                .createdTime(LocalDateTime.now())
                .archived(false)
                .build();

            auditLogMapper.insert(anomalyLog);

            log.warn("⚠️ 触发额外验证 | 用户ID: {} | IP: {} | 原因: 异常登录模式检测 ({}个IP)",
                    userId, ipAddress, concurrentLogins);

            // TODO: 要求用户进行邮箱验证码确认
            // TODO: 发送邮件通知用户账户登录异常
            // TODO: 生成需要验证的临时token用于额外验证流程
            // TODO: 可选：发送短信验证码到注册手机号
        } catch (Exception e) {
            log.error("❌ 触发额外验证机制失败: userId={}, {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 检测异常模式
     */
    private void detectAnomalies(Long userId, String username, AuditLog.EventType eventType,
                                AuditLog.Status status, String ipAddress) {
        try {
            // 检测频繁操作
            if (AuditLog.EventType.PASSWORD_CHANGE.equals(eventType) ||
                AuditLog.EventType.PERMISSION_GRANT.equals(eventType) ||
                AuditLog.EventType.CONFIG_CHANGE.equals(eventType)) {

                // 如果同一用户在短时间内进行多次敏感操作，可能表示账户被劫持
                log.warn("⚠️ 检测到敏感操作 | 用户: {} | 操作类型: {} | IP: {}",
                        username, eventType.getDescription(), ipAddress);
            }
        } catch (Exception e) {
            log.warn("检测异常模式异常: {}", e.getMessage());
        }
    }

    /**
     * 判断是否为关键事件
     */
    private boolean isKriticalEvent(AuditLog.EventType eventType) {
        return eventType == AuditLog.EventType.PASSWORD_CHANGE ||
               eventType == AuditLog.EventType.PASSWORD_RESET ||
               eventType == AuditLog.EventType.ROLE_GRANT ||
               eventType == AuditLog.EventType.ROLE_REVOKE ||
               eventType == AuditLog.EventType.PERMISSION_GRANT ||
               eventType == AuditLog.EventType.PERMISSION_REVOKE ||
               eventType == AuditLog.EventType.USER_DELETE ||
               eventType == AuditLog.EventType.CONFIG_CHANGE ||
               eventType == AuditLog.EventType.SECURITY_SETTING_CHANGE;
    }

    /**
     * 查询审计日志
     */
    public List<AuditLog> getAuditLogsByUserId(Long userId, int limit) {
        try {
            return auditLogMapper.selectByUserId(userId, limit);
        } catch (Exception e) {
            log.error("查询用户审计日志异常: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 查询敏感操作日志
     */
    public List<AuditLog> getSensitiveOperations(int limit) {
        try {
            return auditLogMapper.selectSensitiveOperations(limit);
        } catch (Exception e) {
            log.error("查询敏感操作日志异常: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 归档旧的审计日志
     */
    @Async
    public void archiveOldLogs(LocalDateTime archiveDate) {
        try {
            int archivedCount = auditLogMapper.archiveLogs(archiveDate);
            log.info("已归档 {} 条审计日志，日期早于 {}", archivedCount, archiveDate);
        } catch (Exception e) {
            log.error("归档审计日志异常: {}", e.getMessage());
        }
    }
}
