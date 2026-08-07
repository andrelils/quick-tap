package com.quicktap.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统设置实体（key-value 结构，存储域名、URL 等全局配置）
 */
@Data
public class SystemSetting {
    private Integer id;
    private String keyName;
    private String value;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
