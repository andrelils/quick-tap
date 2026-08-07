package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.dto.PermissionDTO;
import com.quicktap.dto.RoleDTO;
import com.quicktap.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Permission Controller
 *
 * API endpoints for managing permissions and roles
 * Only accessible by super_admin
 */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Permissions", description = "Permission and role management APIs")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * Get all permissions
     * Accessible by: super_admin
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get all permissions")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getAllPermissions() {
        List<PermissionDTO> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    /**
     * Get permissions by resource
     * Accessible by: super_admin
     */
    @GetMapping("/resource/{resource}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get permissions by resource")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getPermissionsByResource(
            @PathVariable String resource) {
        List<PermissionDTO> permissions = permissionService.getPermissionsByResource(resource);
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    /**
     * Get all resources
     * Accessible by: super_admin
     */
    @GetMapping("/resources")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get all permission resources")
    public ResponseEntity<ApiResponse<List<String>>> getAllResources() {
        List<String> resources = permissionService.getAllResources();
        return ResponseEntity.ok(ApiResponse.success(resources));
    }

    /**
     * Get permission by code
     * Accessible by: super_admin, admin, authenticated users
     */
    @GetMapping("/{code}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get permission by code")
    public ResponseEntity<ApiResponse<PermissionDTO>> getPermissionByCode(
            @PathVariable String code) {
        PermissionDTO permission = permissionService.getPermissionByCode(code);
        if (permission == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Permission not found"));
        }
        return ResponseEntity.ok(ApiResponse.success(permission));
    }

    /**
     * Create permission
     * Accessible by: super_admin only
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create new permission")
    public ResponseEntity<ApiResponse<PermissionDTO>> createPermission(
            @RequestParam String code,
            @RequestParam String resource,
            @RequestParam String action,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String category) {
        try {
            PermissionDTO permission = permissionService.createPermission(code, resource, action, description, category);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(permission));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update permission
     * Accessible by: super_admin only
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update permission")
    public ResponseEntity<ApiResponse<PermissionDTO>> updatePermission(
            @PathVariable Long id,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        try {
            PermissionDTO permission = permissionService.updatePermission(id, description, category, status);
            return ResponseEntity.ok(ApiResponse.success(permission));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete permission
     * Accessible by: super_admin only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete permission")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {
        try {
            permissionService.deletePermission(id);
            return ResponseEntity.ok(ApiResponse.success());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete permission"));
        }
    }

    /**
     * Get all roles
     * Accessible by: super_admin
     */
    @GetMapping("/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get all roles")
    public ResponseEntity<ApiResponse<List<RoleDTO>>> getAllRoles() {
        List<RoleDTO> roles = permissionService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    /**
     * Get role by code
     * Accessible by: authenticated users
     */
    @GetMapping("/roles/{code}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get role by code")
    public ResponseEntity<ApiResponse<RoleDTO>> getRoleByCode(@PathVariable String code) {
        RoleDTO role = permissionService.getRoleByCode(code);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Role not found"));
        }
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    /**
     * Get permissions for a role
     * Accessible by: authenticated users
     */
    @GetMapping("/roles/{roleCode}/permissions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get role permissions")
    public ResponseEntity<ApiResponse<Set<String>>> getRolePermissions(
            @PathVariable String roleCode) {
        Set<String> permissions = permissionService.getRolePermissions(roleCode);
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    /**
     * Assign permission to role
     * Accessible by: super_admin only
     */
    @PostMapping("/roles/{roleCode}/permissions/{permissionCode}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Assign permission to role")
    public ResponseEntity<ApiResponse<Void>> assignPermissionToRole(
            @PathVariable String roleCode,
            @PathVariable String permissionCode) {
        try {
            permissionService.assignPermissionToRole(roleCode, permissionCode);
            return ResponseEntity.ok(ApiResponse.success());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Remove permission from role
     * Accessible by: super_admin only
     */
    @DeleteMapping("/roles/{roleCode}/permissions/{permissionCode}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Remove permission from role")
    public ResponseEntity<ApiResponse<Void>> removePermissionFromRole(
            @PathVariable String roleCode,
            @PathVariable String permissionCode) {
        try {
            permissionService.removePermissionFromRole(roleCode, permissionCode);
            return ResponseEntity.ok(ApiResponse.success());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
