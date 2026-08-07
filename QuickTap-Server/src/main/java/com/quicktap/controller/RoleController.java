package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.dto.AdminUpdateRequest;
import com.quicktap.entity.Admin;
import com.quicktap.service.AdminService;
import com.quicktap.service.RoleService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 角色管理控制器 - 权限和角色配置系统
 *
 * 职责：
 * - 提供系统角色定义和权限配置接口
 * - 实现角色到权限的映射管理
 * - 支持用户角色分配功能
 * - 提供权限检查和验证功能
 * - 生成角色权限矩阵供前端展示
 *
 * 系统角色定义：
 * - SUPER_ADMIN (超级管理员): 拥有系统所有权限，包括系统设置、用户管理、角色管理
 * - ADMIN (管理员): 拥有平台管理权限，可管理商户、设备、查看数据统计
 * - MERCHANT (商户): 拥有商户级权限，可管理自己的设备和订单
 *
 * 权限体系架构：
 * {@code
 * SUPER_ADMIN (所有权限)
 *   ├─ 管理员权限: admin.view, admin.create, admin.edit, admin.delete
 *   ├─ 商户权限: merchant.view/create/edit/delete
 *   ├─ 设备权限: device.view/create/edit/delete
 *   ├─ 用户权限: user.view/create/edit/delete
 *   ├─ 订单权限: order.view, order.edit
 *   ├─ 统计权限: statistics.view
 *   ├─ AI权限: ai-generate.use, ai-generate.view
 *   ├─ 知识库权限: corpus.manage
 *   ├─ 设置权限: settings.manage
 *   └─ 角色权限: role.manage
 *
 * ADMIN (平台管理)
 *   ├─ 商户权限: merchant.view/create/edit
 *   ├─ 设备权限: device.view/create/edit
 *   ├─ 用户权限: user.view
 *   ├─ 订单权限: order.view
 *   └─ 统计权限: statistics.view
 *
 * MERCHANT (商户级权限)
 *   ├─ 设备权限: device.view/create/edit
 *   ├─ 订单权限: order.view
 *   ├─ 统计权限: statistics.view
 *   ├─ AI权限: ai-generate.use, ai-generate.view
 *   └─ 知识库权限: corpus.manage
 * }
 *
 * 核心API端点：
 * {@code
 * GET /api/admin/roles
 *   → 获取所有可用角色列表 (返回3个基础角色)
 *
 * GET /api/admin/roles/permissions
 *   → 获取所有权限定义及分类 (用于权限配置)
 *
 * GET /api/admin/roles/matrix
 *   → 获取角色权限矩阵 (用于前端权限表格展示)
 *
 * GET /api/admin/roles/admins (SUPER_ADMIN only)
 *   → 获取所有管理员及其角色分配情况
 *
 * POST /api/admin/roles/assign (SUPER_ADMIN only)
 *   → 为用户分配或更改角色: ?adminId=N&roleId=xxx
 *
 * GET /api/admin/roles/{roleId}
 *   → 获取指定角色的详细信息（权限列表、描述等）
 *
 * GET /api/admin/roles/{userId}/permissions
 *   → 获取用户拥有的所有权限列表
 *
 * GET /api/admin/roles/{userId}/check-permission
 *   → 检查用户是否拥有特定权限: ?permission=XXX
 * }
 *
 * 使用场景：
 * {@code
 * // 场景1: 初始化权限选择界面
 * const roles = await fetch('/api/admin/roles');
 * const permissions = await fetch('/api/admin/roles/permissions');
 * // 前端显示角色选择下拉框和权限列表
 *
 * // 场景2: 为管理员分配角色
 * await fetch('/api/admin/roles/assign', {
 *   method: 'POST',
 *   params: { adminId: 123, roleId: 'admin' }
 * });
 * // 该管理员现在拥有 'admin' 角色的所有权限
 *
 * // 场景3: 权限检查和验证
 * const has = await fetch('/api/admin/roles/999/check-permission?permission=merchant.create');
 * // 返回用户是否拥有指定权限
 *
 * // 场景4: 获取权限矩阵用于权限管理界面
 * const matrix = await fetch('/api/admin/roles/matrix');
 * // 返回所有角色和权限的对应关系，用于表格展示
 * }
 *
 * 路由匹配优先级：
 * IMPORTANT: Static routes must be defined BEFORE dynamic routes to prevent
 * dynamic routes from matching static paths. Spring路由匹配顺序：
 * 1. GET /api/admin/roles (exact match) - getAllRoles()
 * 2. GET /api/admin/roles/permissions (static) - getAllPermissions()
 * 3. GET /api/admin/roles/admins (static) - getAllAdminsWithRoles()
 * 4. GET /api/admin/roles/matrix (static) - getRolePermissionMatrix()
 * 5. POST /api/admin/roles/assign (static) - assignRole()
 * 6. GET /api/admin/roles/{roleId} (dynamic) - getRoleDetail()
 * 7. GET /api/admin/roles/{userId}/permissions (dynamic) - getUserPermissions()
 * 8. GET /api/admin/roles/{userId}/check-permission (dynamic) - checkPermission()
 *
 * 访问控制：
 * - 所有端点默认要求 SUPER_ADMIN 或 ADMIN 角色 (@PreAuthorize)
 * - 分配角色 (/assign) 仅限 SUPER_ADMIN
 * - 查看所有管理员 (/admins) 仅限 SUPER_ADMIN
 * - 其他端点允许 SUPER_ADMIN 和 ADMIN 访问
 *
 * 权限验证流程：
 * {@code
 * 1. 用户发送请求 → 携带JWT Token
 * 2. Spring Security 验证Token有效性
 * 3. @PreAuthorize 检查用户是否拥有所需角色
 * 4. 若无权限 → 返回 403 Forbidden
 * 5. 若有权限 → 继续执行业务逻辑
 * }
 *
 * 数据库交互：
 * - AdminService.getAdminById() - 查询管理员信息
 * - AdminService.getAllAdmins() - 查询所有管理员
 * - AdminService.updateAdmin() - 更新管理员角色和信息
 *
 * 错误处理：
 * - 无效的角色ID → 返回 ApiResponse.badRequest()
 * - 用户不存在 → 返回 ApiResponse.notFound()
 * - 权限不足 → 返回 403 Forbidden (by Spring Security)
 * - 数据库异常 → 返回 ApiResponse.error() with 500 status
 *
 * 集成点：
 * - AdminController: 管理员CRUD操作
 * - AdminService: 管理员和角色业务逻辑
 * - SecurityConfig: 权限拦截和认证配置
 * - JwtAuthenticationFilter: Token验证过滤器
 *
 * @author QuickTap Role Management Team
 * @version 1.0
 * @since 1.0
 * @see AdminController
 * @see AdminService
 * @see com.quicktap.security.JwtAuthenticationFilter
 * @see org.springframework.security.access.prepost.PreAuthorize
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/roles")

@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
@RequiredArgsConstructor
public class RoleController {

    private final AdminService adminService;
    private final RoleService roleService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取所有可用角色列表
     * 匹配 Node.js: GET /api/admin/roles
     */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getAllRoles() {
        log.info("获取所有可用角色列表");

        List<Map<String, Object>> roles = new ArrayList<>();

        // 内置角色：权限动态（role 表有配置则用之，否则取 permissions 表默认）
        addBuiltInRole(roles, "super_admin", "超级管理员");
        addBuiltInRole(roles, "admin", "管理员");
        addBuiltInRole(roles, "merchant", "商户");

        // 自定义角色（来自 role 表），跳过与内置角色重名的历史数据避免重复
        for (Map<String, Object> r : roleService.listCustom()) {
            String rname = toStr(r.get("name"));
            if (RoleService.BUILT_IN_ROLES.contains(rname)) {
                continue;
            }
            Map<String, Object> custom = new HashMap<>();
            custom.put("id", r.get("id"));
            custom.put("name", rname);
            custom.put("description", r.get("description"));
            custom.put("permissions", parsePermissions(toStr(r.get("permissions"))));
            custom.put("createdAt", r.get("createdAt"));
            roles.add(custom);
        }

        return ApiResponse.success(roles);
    }

    /** 组装内置角色（id/name/description 固定，权限按配置或默认） */
    private void addBuiltInRole(List<Map<String, Object>> roles, String code, String description) {
        Map<String, Object> role = new HashMap<>();
        role.put("id", code);
        role.put("name", code);
        role.put("description", description);
        // 创建时间：role 表有配置记录则用之，未配置过显示"系统内置"
        Map<String, Object> stored = roleService.getByName(code);
        Object createdAt = (stored != null) ? stored.get("createdAt") : null;
        role.put("createdAt", createdAt != null ? createdAt : "系统内置");
        // 仅保留 permissions 表中存在的权限 code，过滤历史脏数据
        List<String> effective = roleService.getEffectivePermissions(code);
        List<String> dbCodes = roleService.listDbPermissionCodes();
        List<String> valid = new ArrayList<>();
        for (String p : effective) {
            if (p.equals("*") || dbCodes.contains(p)) {
                valid.add(p);
            }
        }
        if (valid.isEmpty()) {
            valid = roleService.getDefaultPermissionCodes(code);
        }
        role.put("permissions", valid);
        roles.add(role);
    }

    // ============================================================================
    // STATIC ROUTES - All static routes must come before dynamic routes
    // ============================================================================

    /**
     * 获取所有权限列表
     * 匹配 Node.js: GET /api/admin/roles/permissions
     * STATIC ROUTE: Must appear before /{roleId} and /{userId}/permissions
     */
    @GetMapping("/permissions")
    public ApiResponse<List<Map<String, Object>>> getAllPermissions() {
        log.info("获取所有权限列表");
        // 权限源统一来自 permissions 表（含菜单权限与操作权限）
        return ApiResponse.success(roleService.listDbPermissions());
    }

    /**
     * 为用户分配角色
     * 匹配 Node.js: POST /api/admin/roles/assign
     * STATIC ROUTE: Must appear before /{roleId}
     */
    @PostMapping("/assign")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> assignRole(
            @RequestParam Integer adminId,
            @RequestParam String roleId) {
        log.info("分配角色: adminId={}, roleId={}", adminId, roleId);

        if (!isValidRole(roleId)) {
            return ApiResponse.badRequest("无效的角色");
        }

        // ✅ TODO 11 COMPLETED: Call AdminService to update user roles
        Admin admin = adminService.getAdminById(adminId);
        if (admin == null) {
            return ApiResponse.notFound("管理员不存在");
        }

        // Update admin role using AdminService
        AdminUpdateRequest updateRequest = new AdminUpdateRequest();
        updateRequest.setRole(roleId);
        adminService.updateAdmin(adminId, updateRequest);

        log.info("✅ 角色分配成功: adminId={}, 新角色={}", adminId, roleId);

        Map<String, Object> result = new HashMap<>();
        result.put("adminId", adminId);
        result.put("roleId", roleId);
        result.put("message", "角色分配成功");

        return ApiResponse.success(result);
    }

    /**
     * 获取所有管理员和他们的角色
     * 匹配 Node.js: GET /api/admin/roles/admins
     * STATIC ROUTE: Must appear before /{roleId} and /{userId}/*
     */
    @GetMapping("/admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<Map<String, Object>>> getAllAdminsWithRoles() {
        log.info("获取所有管理员和角色");

        // ✅ TODO 12 COMPLETED: Fetch all admins from database with their roles
        try {
            List<Admin> allAdmins = adminService.getAllAdmins();
            List<Map<String, Object>> admins = new ArrayList<>();

            for (Admin admin : allAdmins) {
                Map<String, Object> adminData = new HashMap<>();
                adminData.put("id", admin.getId());
                adminData.put("username", admin.getUsername());
                adminData.put("email", admin.getEmail());
                adminData.put("phone", admin.getPhone());
                adminData.put("role", admin.getRole());
                adminData.put("rolePermissions", getRolePermissions(admin.getRole()));
                adminData.put("status", admin.getStatus());
                adminData.put("createdAt", admin.getCreatedAt());
                adminData.put("updatedAt", admin.getUpdatedAt());
                admins.add(adminData);
            }

            log.info("✅ 获取管理员列表成功: 共{}个管理员", admins.size());
            return ApiResponse.success(admins);
        } catch (Exception e) {
            log.error("❌ 获取管理员列表失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, "获取管理员列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取角色权限矩阵
     * 匹配 Node.js: GET /api/admin/roles/matrix
     * STATIC ROUTE: Must appear before /{roleId} and /{userId}/*
     */
    @GetMapping("/matrix")
    public ApiResponse<Map<String, Object>> getRolePermissionMatrix() {
        log.info("获取角色权限矩阵");

        Map<String, Object> matrix = new HashMap<>();

        // 权限列表
        List<String> permissionList = Arrays.asList(
            "admin.view", "admin.create", "admin.edit", "admin.delete",
            "merchant.view", "merchant.create", "merchant.edit", "merchant.delete",
            "device.view", "device.create", "device.edit", "device.delete",
            "user.view", "user.create", "user.edit", "user.delete",
            "order.view", "order.edit",
            "statistics.view",
            "ai-generate.use", "ai-generate.view",
            "corpus.manage",
            "settings.manage",
            "role.manage"
        );

        // 角色权限映射
        Map<String, List<String>> rolePermissions = new HashMap<>();
        rolePermissions.put("super_admin", getRolePermissions("super_admin"));
        rolePermissions.put("admin", getRolePermissions("admin"));
        rolePermissions.put("merchant", getRolePermissions("merchant"));

        matrix.put("permissions", permissionList);
        matrix.put("roles", rolePermissions);

        return ApiResponse.success(matrix);
    }

    /**
     * 新增自定义角色
     * body: { name(标识), description(中文名), permissions: string[] }
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> createRole(@RequestBody(required = false) Map<String, Object> body) {
        if (body == null) {
            return ApiResponse.badRequest("请求参数不能为空");
        }
        String code = toStr(body.get("code")) != null ? toStr(body.get("code")) : toStr(body.get("name"));
        String description = toStr(body.get("description"));
        if (RoleService.BUILT_IN_ROLES.contains(code)) {
            // 内置角色：走 upsert 保存配置
            Map<String, Object> created = roleService.upsertBuiltIn(code, description, toPermissionsJson(body.get("permissions")));
            return ApiResponse.success("保存成功", created);
        }
        Map<String, Object> created = roleService.create(code, description, toPermissionsJson(body.get("permissions")));
        return ApiResponse.success("创建成功", created);
    }

    /**
     * 编辑角色（内置角色也允许配置权限；按角色标识 name）
     * body: { name(中文名), description, permissions: string[] }
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> updateRole(@PathVariable String roleId,
                                                       @RequestBody(required = false) Map<String, Object> body) {
        if (body == null) {
            return ApiResponse.badRequest("请求参数不能为空");
        }
        String description = toStr(body.get("name"));
        String permissionsJson = toPermissionsJson(body.get("permissions"));
        if (RoleService.BUILT_IN_ROLES.contains(roleId)) {
            Map<String, Object> updated = roleService.upsertBuiltIn(roleId, description, permissionsJson);
            return ApiResponse.success("更新成功", updated);
        }
        Map<String, Object> updated = roleService.update(roleId, description, permissionsJson);
        return ApiResponse.success("更新成功", updated);
    }

    /**
     * 删除自定义角色（按角色标识 name）
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> deleteRole(@PathVariable String roleId) {
        roleService.delete(roleId);
        return ApiResponse.success("删除成功");
    }

    // ============================================================================
    // DYNAMIC ROUTES - All dynamic routes must come AFTER static routes
    // ============================================================================

    /**
     * 获取角色详情
     * 匹配 Node.js: GET /api/admin/roles/:roleId
     * DYNAMIC ROUTE: Must appear AFTER all static routes like /permissions, /admins, /matrix
     */
    @GetMapping("/{roleId}")
    public ApiResponse<Map<String, Object>> getRoleDetail(@PathVariable String roleId) {
        log.info("获取角色详情: roleId={}", roleId);

        Map<String, Object> roleDetail = new HashMap<>();

        switch (roleId) {
            case "super_admin":
                roleDetail.put("id", "super_admin");
                roleDetail.put("name", "超级管理员");
                roleDetail.put("description", "拥有系统所有权限");
                roleDetail.put("permissions", roleService.getEffectivePermissions("super_admin"));
                break;
            case "admin":
                roleDetail.put("id", "admin");
                roleDetail.put("name", "管理员");
                roleDetail.put("description", "拥有平台管理权限");
                roleDetail.put("permissions", roleService.getEffectivePermissions("admin"));
                break;
            case "merchant":
                roleDetail.put("id", "merchant");
                roleDetail.put("name", "商户");
                roleDetail.put("description", "商户管理权限");
                roleDetail.put("permissions", roleService.getEffectivePermissions("merchant"));
                break;
            default:
                // 自定义角色
                Map<String, Object> custom = roleService.getByName(roleId);
                if (custom != null) {
                    roleDetail.put("id", custom.get("id"));
                    roleDetail.put("name", custom.get("name"));
                    roleDetail.put("description", custom.get("description"));
                    roleDetail.put("permissions", parsePermissions(toStr(custom.get("permissions"))));
                    roleDetail.put("createdAt", custom.get("createdAt"));
                    return ApiResponse.success(roleDetail);
                }
                return ApiResponse.notFound("角色不存在");
        }

        return ApiResponse.success(roleDetail);
    }

    /**
     * 获取用户的所有权限
     * 匹配 Node.js: GET /api/admin/roles/:userId/permissions
     * DYNAMIC ROUTE: Must appear AFTER static route /permissions
     */
    @GetMapping("/{userId}/permissions")
    public ApiResponse<List<String>> getUserPermissions(@PathVariable Integer userId) {
        log.info("获取用户权限: userId={}", userId);

        Admin admin = adminService.getAdminById(userId);
        if (admin == null) {
            return ApiResponse.notFound("用户不存在");
        }

        List<String> permissions = getRolePermissions(admin.getRole());
        return ApiResponse.success(permissions);
    }

    /**
     * 检查用户是否有特定权限
     * 匹配 Node.js: GET /api/admin/roles/:userId/check-permission
     * DYNAMIC ROUTE: Must appear AFTER static routes
     */
    @GetMapping("/{userId}/check-permission")
    public ApiResponse<Map<String, Object>> checkPermission(
            @PathVariable Integer userId,
            @RequestParam String permission) {
        log.info("检查用户权限: userId={}, permission={}", userId, permission);

        Admin admin = adminService.getAdminById(userId);
        if (admin == null) {
            return ApiResponse.notFound("用户不存在");
        }

        List<String> permissions = getRolePermissions(admin.getRole());
        boolean hasPermission = permissions.contains(permission);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("permission", permission);
        result.put("hasPermission", hasPermission);

        return ApiResponse.success(result);
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    /**
     * 创建权限对象的辅助方法
     */
    private Map<String, Object> createPermission(String id, String name, String category) {
        Map<String, Object> permission = new HashMap<>();
        permission.put("id", id);
        permission.put("name", name);
        permission.put("category", category);
        return permission;
    }

    /**
     * 获取角色的权限列表
     */
    private List<String> getRolePermissions(String roleId) {
        switch (roleId) {
            case "super_admin":
                return Arrays.asList(
                    "admin.view", "admin.create", "admin.edit", "admin.delete",
                    "merchant.view", "merchant.create", "merchant.edit", "merchant.delete",
                    "device.view", "device.create", "device.edit", "device.delete",
                    "user.view", "user.create", "user.edit", "user.delete",
                    "order.view", "order.edit",
                    "statistics.view",
                    "ai-generate.use", "ai-generate.view",
                    "corpus.manage",
                    "settings.manage",
                    "role.manage"
                );
            case "admin":
                return Arrays.asList(
                    "merchant.view", "merchant.create", "merchant.edit",
                    "device.view", "device.create", "device.edit",
                    "user.view",
                    "order.view",
                    "statistics.view"
                );
            case "merchant":
                return Arrays.asList(
                    "device.view", "device.create", "device.edit",
                    "order.view",
                    "statistics.view",
                    "ai-generate.use", "ai-generate.view",
                    "corpus.manage"
                );
            default:
                return new ArrayList<>();
        }
    }

    /**
     * 验证角色ID是否有效（内置角色或自定义角色表中存在的角色）
     */
    private boolean isValidRole(String roleId) {
        return RoleService.BUILT_IN_ROLES.contains(roleId) || roleService.exists(roleId);
    }

    private String toStr(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 解析 permissions JSON 字符串为列表
     */
    private List<String> parsePermissions(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("解析角色权限失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 将权限列表序列化为 JSON 字符串
     */
    private String toPermissionsJson(Object permissions) {
        if (permissions == null) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(permissions);
        } catch (Exception e) {
            log.warn("序列化角色权限失败: {}", e.getMessage());
            return "[]";
        }
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
