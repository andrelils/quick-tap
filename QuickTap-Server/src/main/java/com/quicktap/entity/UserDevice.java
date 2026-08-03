package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户-设备关系表
 */
@Entity
@Table(name = "user_device", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "device_id"})
}, indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_device_id", columnList = "device_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
