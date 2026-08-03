package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户卡券关系表
 */
@Entity
@Table(name = "user_coupon", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "coupon_id"})
}, indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_coupon_id", columnList = "coupon_id"),
    @Index(name = "idx_merchant_id", columnList = "merchant_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_user_coupon_status", columnList = "user_id,status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "status")
    private Integer status; // 1=UNUSED, 2=USED, 3=EXPIRED

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
