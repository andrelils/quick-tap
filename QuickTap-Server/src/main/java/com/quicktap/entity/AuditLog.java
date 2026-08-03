package com.quicktap.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审计日志实体类
 * 记录系统中所有安全相关的事件（登录、权限变更、敏感操作等）
 *
 * 审计要素:
 * - 谁操作（用户ID、用户名）
 * - 什么时候操作（操作时间）
 * - 操作了什么（操作类型、操作对象）
 * - 从哪里操作（IP地址）
 * - 操作结果（成功/失败及失败原因）
 * - 操作详情（变更前后数据）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    /**
     * 审计事件类型枚举
     */
    public enum EventType {
        // 认证相关
        LOGIN("登录"),
        LOGOUT("登出"),
        LOGIN_FAILED("登录失败"),
        PASSWORD_RESET("密码重置"),
        PASSWORD_CHANGE("密码修改"),

        // 用户管理
        USER_CREATE("创建用户"),
        USER_UPDATE("更新用户"),
        USER_DELETE("删除用户"),
        USER_DISABLE("禁用用户"),
        USER_ENABLE("启用用户"),

        // 权限管理
        ROLE_GRANT("授予角色"),
        ROLE_REVOKE("撤销角色"),
        PERMISSION_GRANT("授予权限"),
        PERMISSION_REVOKE("撤销权限"),

        // 数据访问
        DATA_ACCESS("访问敏感数据"),
        DATA_EXPORT("导出数据"),
        DATA_DOWNLOAD("下载数据"),

        // 系统配置
        CONFIG_CHANGE("系统配置变更"),
        SECURITY_SETTING_CHANGE("安全设置变更"),

        // 文件操作
        FILE_UPLOAD("文件上传"),
        FILE_DOWNLOAD("文件下载"),
        FILE_DELETE("文件删除"),

        // 其他敏感操作
        ACCOUNT_LOCK("账户锁定"),
        ACCOUNT_UNLOCK("账户解锁"),
        SENSITIVE_OPERATION("敏感操作");

        private final String description;

        EventType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 操作结果状态
     */
    public enum Status {
        SUCCESS("成功"),
        FAILURE("失败"),
        DENIED("被拒绝"),
        WARNING("警告");

        private final String description;

        Status(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 用户ID（谁操作）
     */
    private Long userId;

    /**
     * 用户名（谁操作）
     */
    private String username;

    /**
     * 审计事件类型
     */
    private EventType eventType;

    /**
     * 操作结果状态
     */
    private Status status;

    /**
     * 操作详描述
     */
    private String description;

    /**
     * 操作对象类型（如：User、Role、Document等）
     */
    private String objectType;

    /**
     * 操作对象ID
     */
    private Long objectId;

    /**
     * 操作前的数据（JSON格式）
     */
    private String beforeData;

    /**
     * 操作后的数据（JSON格式）
     */
    private String afterData;

    /**
     * 客户端IP地址
     */
    private String ipAddress;

    /**
     * 用户代理（User-Agent）
     */
    private String userAgent;

    /**
     * 请求ID（用于关联日志）
     */
    private String requestId;

    /**
     * 失败原因（仅当status为FAILURE时）
     */
    private String failureReason;

    /**
     * 操作耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 受影响的记录数
     */
    private Integer affectedRecords;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 是否已归档
     */
    private Boolean archived = false;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 创建审计日志
     */
    public static AuditLog of(Long userId, String username, EventType eventType,
                              Status status, String description) {
        return AuditLog.builder()
            .userId(userId)
            .username(username)
            .eventType(eventType)
            .status(status)
            .description(description)
            .createdTime(LocalDateTime.now())
            .archived(false)
            .build();
    }
}
