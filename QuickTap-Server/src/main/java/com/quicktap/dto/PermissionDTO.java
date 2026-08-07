package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * PermissionDTO - Data Transfer Object for Permission
 *
 * Used for API responses and internal communication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionDTO {

    private Long id;

    /**
     * Permission code: resource.action format
     */
    private String code;

    /**
     * Resource name
     */
    private String resource;

    /**
     * Action name
     */
    private String action;

    /**
     * Description in Chinese
     */
    private String description;

    /**
     * Category for grouping
     */
    private String category;

    /**
     * Status: 1 = active, 0 = inactive
     */
    private Integer status;

    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;
}
