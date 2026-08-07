package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Role Entity
 *
 * Represents a role that can have multiple permissions assigned
 * System has three built-in roles: super_admin, admin, merchant
 */
@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_role_code", columnList = "code", unique = true),
    @Index(name = "idx_role_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Role code: super_admin, admin, merchant
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Role name in Chinese
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Role description
     */
    @Column(length = 255)
    private String description;

    /**
     * Whether this is a built-in system role
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isSystem = false;

    /**
     * Status: 1 = active, 0 = inactive
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * Permissions assigned to this role
     * Uses JOIN table: role_permissions
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    /**
     * Timestamp when role was created
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when role was last updated
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

    /**
     * Add a permission to this role
     */
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    /**
     * Remove a permission from this role
     */
    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }

    /**
     * Get all permission codes for this role
     */
    public Set<String> getPermissionCodes() {
        Set<String> codes = new HashSet<>();
        for (Permission perm : permissions) {
            if (perm.getStatus() == 1) {
                codes.add(perm.getCode());
            }
        }
        return codes;
    }
}
