package com.quicktap.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 权限矩阵配置
 * 定义各角色对资源的访问权限
 */
@Component
@ConfigurationProperties(prefix = "permission")
@Data
public class PermissionMatrixProperties {

    private Map<String, RolePermission> roles = new HashMap<>();

    @Data
    public static class RolePermission {
        // 资源权限
        private Map<String, ResourcePermission> resources = new HashMap<>();
    }

    @Data
    public static class ResourcePermission {
        // 可执行的操作: create, read, update, delete, list等
        private java.util.List<String> operations = new java.util.ArrayList<>();
        // 数据可见性范围: all, own, merchant_level
        private String visibility;
        // 是否需要审核
        private Boolean needsApproval = false;
        // 是否记录审计日志
        private Boolean auditLogging = true;
    }

    /**
     * 检查角色是否有权执行某个操作
     */
    public boolean hasPermission(String role, String resource, String operation) {
        RolePermission rolePermission = roles.get(role);
        if (rolePermission == null) {
            return false;
        }

        ResourcePermission resourcePermission = rolePermission.getResources().get(resource);
        if (resourcePermission == null) {
            return false;
        }

        return resourcePermission.getOperations().contains(operation);
    }

    /**
     * 获取资源的数据可见性范围
     */
    public String getResourceVisibility(String role, String resource) {
        RolePermission rolePermission = roles.get(role);
        if (rolePermission == null) {
            return "none";
        }

        ResourcePermission resourcePermission = rolePermission.getResources().get(resource);
        if (resourcePermission == null) {
            return "none";
        }

        return resourcePermission.getVisibility();
    }

    /**
     * 检查资源操作是否需要审核
     */
    public boolean needsApproval(String role, String resource) {
        RolePermission rolePermission = roles.get(role);
        if (rolePermission == null) {
            return false;
        }

        ResourcePermission resourcePermission = rolePermission.getResources().get(resource);
        if (resourcePermission == null) {
            return false;
        }

        return resourcePermission.getNeedsApproval();
    }
}
