package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 推广点击日志表
 */
@Entity
@Table(name = "promotion_click_log", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_platform_id", columnList = "platform_id"),
    @Index(name = "idx_merchant_id", columnList = "merchant_id"),
    @Index(name = "idx_device_id", columnList = "device_id"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_promotion_click_merchant_created", columnList = "merchant_id,created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionClickLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "platform_id", nullable = false)
    private Long platformId;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
