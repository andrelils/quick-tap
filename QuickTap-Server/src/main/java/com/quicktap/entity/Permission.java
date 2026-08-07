package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Permission Entity
 *
 * Represents a single permission in the system using resource.action format
 * Example: "merchant.view", "merchant.create", "device.delete"
 */
@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_code", columnList = "code", unique = true),
    @Index(name = "idx_resource", columnList = "resource"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Permission code: resource.action format
     * Examples: merchant.view, merchant.create, device.delete
     */
    @Column(nullable = false, unique = true, length = 100)
    private String code;

    /**
     * Resource name: merchant, device, ai, marketing, system, dashboard
     */
    @Column(nullable = false, length = 50)
    private String resource;

    /**
     * Action name: view, create, update, delete, etc.
     */
    @Column(nullable = false, length = 50)
    private String action;

    /**
     * Human-readable description (Chinese)
     */
    @Column(length = 200)
    private String description;

    /**
     * Permission category for grouping in UI
     */
    @Column(length = 50)
    private String category;

    /**
     * Status: 1 = active, 0 = inactive
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * Timestamp when permission was created
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when permission was last updated
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
