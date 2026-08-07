package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * RoleDTO - Data Transfer Object for Role
 *
 * Used for API responses and internal communication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDTO {

    private Long id;

    /**
     * Role code: super_admin, admin, merchant
     */
    private String code;

    /**
     * Role name in Chinese
     */
    private String name;

    /**
     * Role description
     */
    private String description;

    /**
     * Whether this is a system role
     */
    private Boolean isSystem;

    /**
     * Status: 1 = active, 0 = inactive
     */
    private Integer status;

    /**
     * Permissions assigned to this role
     * Contains permission codes
     */
    private Set<String> permissionCodes;

    /**
     * Full permission objects
     */
    private Set<PermissionDTO> permissions;

    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;
}
