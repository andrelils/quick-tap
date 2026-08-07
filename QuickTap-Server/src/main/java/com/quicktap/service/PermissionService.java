package com.quicktap.service;

import com.quicktap.dto.PermissionDTO;
import com.quicktap.dto.RoleDTO;
import com.quicktap.entity.Permission;
import com.quicktap.entity.Role;
import com.quicktap.repository.PermissionRepository;
import com.quicktap.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Permission Service
 *
 * Manages permissions and roles in the system
 * Provides methods for permission checking, role management, and permission caching
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    /**
     * Get all active permissions (cached)
     */
    @Cacheable(value = "permissions:all", unless = "#result == null || #result.isEmpty()")
    public List<PermissionDTO> getAllPermissions() {
        log.debug("Fetching all permissions from database");
        List<Permission> permissions = permissionRepository.findAllActive();
        return permissions.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get permissions by resource
     */
    public List<PermissionDTO> getPermissionsByResource(String resource) {
        List<Permission> permissions = permissionRepository.findByResource(resource);
        return permissions.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get all resources
     */
    @Cacheable(value = "permissions:resources", unless = "#result == null || #result.isEmpty()")
    public List<String> getAllResources() {
        return permissionRepository.findAllResources();
    }

    /**
     * Get permission by code
     */
    public PermissionDTO getPermissionByCode(String code) {
        return permissionRepository.findByCode(code)
            .map(this::convertToDTO)
            .orElse(null);
    }

    /**
     * Create a new permission
     */
    @CacheEvict(value = {"permissions:all", "permissions:resources"}, allEntries = true)
    public PermissionDTO createPermission(String code, String resource, String action, String description, String category) {
        if (permissionRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Permission with code " + code + " already exists");
        }

        Permission permission = Permission.builder()
            .code(code)
            .resource(resource)
            .action(action)
            .description(description)
            .category(category)
            .status(1)
            .build();

        Permission saved = permissionRepository.save(permission);
        log.info("Created permission: {}", code);
        return convertToDTO(saved);
    }

    /**
     * Update permission
     */
    @CacheEvict(value = {"permissions:all", "permissions:resources"}, allEntries = true)
    public PermissionDTO updatePermission(Long id, String description, String category, Integer status) {
        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found"));

        if (description != null) {
            permission.setDescription(description);
        }
        if (category != null) {
            permission.setCategory(category);
        }
        if (status != null) {
            permission.setStatus(status);
        }

        Permission updated = permissionRepository.save(permission);
        log.info("Updated permission: {}", permission.getCode());
        return convertToDTO(updated);
    }

    /**
     * Delete permission
     */
    @CacheEvict(value = {"permissions:all", "permissions:resources"}, allEntries = true)
    public void deletePermission(Long id) {
        permissionRepository.deleteById(id);
        log.info("Deleted permission with id: {}", id);
    }

    /**
     * Initialize system permissions (called on startup)
     */
    public void initializeSystemPermissions() {
        Map<String, Map<String, String>> permissions = new LinkedHashMap<>();

        // Dashboard
        permissions.put("dashboard", Map.of(
            "view", "仪表盘访问权限"
        ));

        // Merchant
        permissions.put("merchant", Map.of(
            "view", "查看商家",
            "create", "创建商家",
            "update", "编辑商家",
            "delete", "删除商家",
            "quota", "额度管理"
        ));

        // Device
        permissions.put("device", Map.of(
            "view", "查看设备",
            "create", "创建设备",
            "update", "编辑设备",
            "delete", "删除设备"
        ));

        // AI
        permissions.put("ai", Map.of(
            "generate", "AI创作",
            "corpus", "语料管理",
            "config", "创作配置",
            "merchant_config", "商家配置总览"
        ));

        // Marketing
        permissions.put("marketing", Map.of(
            "platforms", "推广平台总配置",
            "promotion", "推广管理",
            "coupons", "优惠券管理",
            "plans", "套餐管理",
            "orders", "订单管理"
        ));

        // System
        permissions.put("system", Map.of(
            "settings", "系统配置",
            "user", "用户管理",
            "role", "角色管理",
            "access", "商家权限配置"
        ));

        // Initialize each permission
        permissions.forEach((resource, actions) -> {
            actions.forEach((action, description) -> {
                String code = resource + "." + action;
                if (!permissionRepository.existsByCode(code)) {
                    Permission permission = Permission.builder()
                        .code(code)
                        .resource(resource)
                        .action(action)
                        .description(description)
                        .category(resource)
                        .status(1)
                        .build();
                    permissionRepository.save(permission);
                    log.debug("Initialized permission: {}", code);
                }
            });
        });
    }

    /**
     * Initialize system roles with default permissions
     */
    public void initializeSystemRoles() {
        // Get all permissions
        List<Permission> allPermissions = permissionRepository.findAllActive();
        Map<String, Permission> permissionMap = allPermissions.stream()
            .collect(Collectors.toMap(Permission::getCode, p -> p));

        // Super Admin - has all permissions
        if (!roleRepository.existsByCode("super_admin")) {
            Role superAdmin = Role.builder()
                .code("super_admin")
                .name("超级管理员")
                .description("拥有系统所有权限")
                .isSystem(true)
                .status(1)
                .permissions(new HashSet<>(allPermissions))
                .build();
            roleRepository.save(superAdmin);
            log.info("Initialized role: super_admin");
        }

        // Admin - has most permissions except some system config
        if (!roleRepository.existsByCode("admin")) {
            Set<Permission> adminPermissions = allPermissions.stream()
                .filter(p -> !p.getCode().equals("system.access"))
                .collect(Collectors.toSet());

            Role admin = Role.builder()
                .code("admin")
                .name("管理员")
                .description("拥有除商家权限配置外的大部分权限")
                .isSystem(true)
                .status(1)
                .permissions(adminPermissions)
                .build();
            roleRepository.save(admin);
            log.info("Initialized role: admin");
        }

        // Merchant - limited permissions
        if (!roleRepository.existsByCode("merchant")) {
            Set<Permission> merchantPermissions = new HashSet<>();
            String[] merchantPerms = {
                "dashboard.view",
                "merchant.view",
                "device.view",
                "device.create",
                "device.update",
                "device.delete",
                "ai.generate",
                "ai.corpus",
                "marketing.promotion",
                "marketing.coupons",
                "marketing.plans",
                "marketing.orders"
            };

            for (String code : merchantPerms) {
                Permission perm = permissionMap.get(code);
                if (perm != null) {
                    merchantPermissions.add(perm);
                }
            }

            Role merchant = Role.builder()
                .code("merchant")
                .name("商家")
                .description("商家用户角色")
                .isSystem(true)
                .status(1)
                .permissions(merchantPermissions)
                .build();
            roleRepository.save(merchant);
            log.info("Initialized role: merchant");
        }
    }

    /**
     * Get role by code
     */
    public RoleDTO getRoleByCode(String code) {
        return roleRepository.findByCode(code)
            .map(this::convertToRoleDTO)
            .orElse(null);
    }

    /**
     * Get all roles
     */
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAllActive();
        return roles.stream()
            .map(this::convertToRoleDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get role permissions
     */
    @Cacheable(value = "role:permissions", key = "#roleCode")
    public Set<String> getRolePermissions(String roleCode) {
        log.debug("Fetching permissions for role: {}", roleCode);
        return roleRepository.findByCode(roleCode)
            .map(Role::getPermissionCodes)
            .orElse(new HashSet<>());
    }

    /**
     * Assign permission to role
     */
    @CacheEvict(value = "role:permissions", key = "#roleCode", allEntries = true)
    public void assignPermissionToRole(String roleCode, String permissionCode) {
        Role role = roleRepository.findByCode(roleCode)
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleCode));

        Permission permission = permissionRepository.findByCode(permissionCode)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionCode));

        role.addPermission(permission);
        roleRepository.save(role);
        log.info("Assigned permission {} to role {}", permissionCode, roleCode);
    }

    /**
     * Remove permission from role
     */
    @CacheEvict(value = "role:permissions", key = "#roleCode", allEntries = true)
    public void removePermissionFromRole(String roleCode, String permissionCode) {
        Role role = roleRepository.findByCode(roleCode)
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleCode));

        Permission permission = permissionRepository.findByCode(permissionCode)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionCode));

        role.removePermission(permission);
        roleRepository.save(role);
        log.info("Removed permission {} from role {}", permissionCode, roleCode);
    }

    /**
     * Convert Permission entity to DTO
     */
    private PermissionDTO convertToDTO(Permission permission) {
        return PermissionDTO.builder()
            .id(permission.getId())
            .code(permission.getCode())
            .resource(permission.getResource())
            .action(permission.getAction())
            .description(permission.getDescription())
            .category(permission.getCategory())
            .status(permission.getStatus())
            .createdAt(permission.getCreatedAt())
            .updatedAt(permission.getUpdatedAt())
            .build();
    }

    /**
     * Convert Role entity to RoleDTO
     */
    private RoleDTO convertToRoleDTO(Role role) {
        return RoleDTO.builder()
            .id(role.getId())
            .code(role.getCode())
            .name(role.getName())
            .description(role.getDescription())
            .isSystem(role.getIsSystem())
            .status(role.getStatus())
            .permissionCodes(role.getPermissionCodes())
            .permissions(role.getPermissions().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toSet()))
            .createdAt(role.getCreatedAt())
            .updatedAt(role.getUpdatedAt())
            .build();
    }
}
