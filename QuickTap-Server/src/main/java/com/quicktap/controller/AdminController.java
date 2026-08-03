package com.quicktap.controller;

import com.quicktap.dto.AdminCreateRequest;
import com.quicktap.dto.AdminUpdateRequest;
import com.quicktap.dto.ApiResponse;
import com.quicktap.dto.PageResponse;
import com.quicktap.entity.Admin;
import com.quicktap.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员管理接口控制器 - 系统管理员的CRUD操作
 *
 * 职责：
 * - 提供管理员账户的完整生命周期管理（创建、读取、更新、删除）
 * - 实现管理员的启用和禁用功能
 * - 支持分页查询管理员列表
 * - 提供管理员详情查询接口
 * - 集成角色权限管理（通过RoleController实现）
 *
 * 访问控制：
 * - 所有端点默认要求 SUPER_ADMIN 或 ADMIN 角色
 * - 创建和删除管理员: 仅限 SUPER_ADMIN
 * - 更新、启用、禁用: SUPER_ADMIN 和 ADMIN 都可以
 * - 查询列表和详情: SUPER_ADMIN 和 ADMIN 都可以
 *
 * 核心API端点：
 * {@code
 * GET /api/admin/list
 *   → 分页获取管理员列表
 *   参数: pageNum (默认1), pageSize (默认10)
 *   返回: PageResponse<Admin> 分页对象
 *
 * GET /api/admin/{id}
 *   → 获取管理员详情
 *   参数: id (路径参数)
 *   返回: Admin 对象
 *
 * POST /api/admin
 *   → 创建新管理员 (SUPER_ADMIN only)
 *   请求体: AdminCreateRequest { username, password, email, phone, role }
 *   返回: 创建的Admin对象
 *
 * PUT /api/admin/{id}
 *   → 更新管理员信息 (SUPER_ADMIN/ADMIN)
 *   参数: id (路径参数)
 *   请求体: AdminUpdateRequest { username, email, phone, role }
 *   返回: 更新后的Admin对象
 *
 * DELETE /api/admin/{id}
 *   → 删除管理员 (SUPER_ADMIN only)
 *   参数: id (路径参数)
 *   返回: 空响应
 *
 * PUT /api/admin/{id}/disable
 *   → 禁用管理员 (SUPER_ADMIN/ADMIN)
 *   参数: id (路径参数)
 *   返回: 空响应，管理员将无法登录
 *
 * PUT /api/admin/{id}/enable
 *   → 启用已禁用的管理员 (SUPER_ADMIN/ADMIN)
 *   参数: id (路径参数)
 *   返回: 空响应，管理员可重新登录
 * }
 *
 * 使用场景：
 * {@code
 * // 场景1: 初始化管理员列表界面
 * const response = await fetch('/api/admin/list?pageNum=1&pageSize=20');
 * // 返回分页的管理员列表
 *
 * // 场景2: 创建新的超级管理员
 * await fetch('/api/admin', {
 *   method: 'POST',
 *   body: JSON.stringify({
 *     username: 'newadmin',
 *     password: 'SecurePassword123!',
 *     email: 'admin@example.com',
 *     phone: '13800138000',
 *     role: 'SUPER_ADMIN'
 *   })
 * });
 * // 新管理员账户创建成功
 *
 * // 场景3: 更新管理员信息
 * await fetch('/api/admin/5', {
 *   method: 'PUT',
 *   body: JSON.stringify({
 *     email: 'newemail@example.com',
 *     phone: '13900139000'
 *   })
 * });
 * // 管理员信息已更新
 *
 * // 场景4: 禁用不活跃的管理员账户
 * await fetch('/api/admin/7/disable', { method: 'PUT' });
 * // 该管理员现在无法登录，但账户数据保留
 *
 * // 场景5: 获取管理员详情
 * const admin = await fetch('/api/admin/3');
 * // 返回该管理员的完整信息
 * }
 *
 * 管理员属性说明：
 * {@code
 * {
 *   "id": 1,                              // 数据库主键
 *   "username": "admin",                  // 登录用户名（唯一）
 *   "email": "admin@example.com",         // 邮箱地址
 *   "phone": "13800138000",               // 联系电话
 *   "password": "encrypted_hash",         // 密码（加密存储，不对外暴露）
 *   "role": "SUPER_ADMIN",                // 角色: SUPER_ADMIN, ADMIN, MERCHANT
 *   "status": 1,                          // 状态: 1=启用, 0=禁用
 *   "createdAt": "2024-01-15T10:00:00Z",  // 创建时间
 *   "updatedAt": "2024-07-20T15:30:00Z"   // 最后更新时间
 * }
 * }
 *
 * 错误处理：
 * - 管理员不存在 → 返回 404 Not Found
 * - 权限不足 → 返回 403 Forbidden
 * - 用户名已存在 → 返回 400 Bad Request
 * - 数据验证失败 → 返回 400 Bad Request
 * - 数据库异常 → 返回 500 Internal Server Error
 *
 * 业务逻辑约束：
 * - 用户名必须唯一，创建时自动验证
 * - 密码必须满足安全规则（长度、复杂度等）
 * - 禁用的管理员无法登录，但数据保留可恢复
 * - 删除操作不可撤销，建议使用禁用而非删除
 * - 最后一个SUPER_ADMIN不能被删除
 *
 * 数据库交互：
 * - AdminService.getAdminList(int, int) - 分页查询
 * - AdminService.getAdminCount() - 统计总数
 * - AdminService.getAdminById(Integer) - 按ID查询
 * - AdminService.createAdmin(AdminCreateRequest) - 创建
 * - AdminService.updateAdmin(Integer, AdminUpdateRequest) - 更新
 * - AdminService.deleteAdmin(Integer) - 删除
 * - AdminService.enableAdmin(Integer) - 启用
 * - AdminService.disableAdmin(Integer) - 禁用
 *
 * 安全特性：
 * - 所有请求需JWT Token认证
 * - 所有敏感操作需要特定角色权限
 * - 密码在数据库中加密存储（不使用明文）
 * - 支持登录审计日志跟踪（通过AuditLoggingService）
 * - 所有数据库操作由Spring Security保护
 *
 * 日志记录：
 * - INFO: 获取列表、获取详情、更新等正常操作
 * - WARN: 权限拒绝、不存在的资源等警告情况
 * - ERROR: 数据库异常、系统错误等
 *
 * 集成点：
 * - RoleController: 角色权限配置
 * - AdminService: 业务逻辑实现
 * - AuthController: 登录认证
 * - AuditLoggingService: 操作审计日志
 * - SecurityConfig: 权限拦截配置
 *
 * @author QuickTap Admin Management Team
 * @version 1.0
 * @since 1.0
 * @see AdminService
 * @see RoleController
 * @see com.quicktap.dto.AdminCreateRequest
 * @see com.quicktap.dto.AdminUpdateRequest
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 获取管理员列表
     */
    @GetMapping("/list")
    public ApiResponse<PageResponse<Admin>> getAdminList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取管理员列表: pageNum={}, pageSize={}", pageNum, pageSize);
        List<Admin> list = adminService.getAdminList(pageNum, pageSize);
        Long total = adminService.getAdminCount();
        PageResponse<Admin> pageResponse = PageResponse.of(list, pageNum, pageSize, total);
        return ApiResponse.success("获取成功", pageResponse);
    }

    /**
     * 获取管理员详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Admin> getAdminById(@PathVariable Integer id) {
        log.info("获取管理员详情: id={}", id);
        Admin admin = adminService.getAdminById(id);
        return ApiResponse.success(admin);
    }

    /**
     * 创建管理员
     * 仅超级管理员可以创建
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Admin> createAdmin(@RequestBody AdminCreateRequest request) {
        log.info("创建管理员: username={}", request.getUsername());
        Admin admin = adminService.createAdmin(request);
        return ApiResponse.success("创建成功", admin);
    }

    /**
     * 更新管理员信息
     */
    @PutMapping("/{id}")
    public ApiResponse<Admin> updateAdmin(@PathVariable Integer id,
                                          @RequestBody AdminUpdateRequest request) {
        log.info("更新管理员: id={}", id);
        Admin admin = adminService.updateAdmin(id, request);
        return ApiResponse.success("更新成功", admin);
    }

    /**
     * 删除管理员
     * 仅超级管理员可以删除
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> deleteAdmin(@PathVariable Integer id) {
        log.info("删除管理员: id={}", id);
        adminService.deleteAdmin(id);
        return ApiResponse.success("删除成功");
    }

    /**
     * 禁用管理员
     */
    @PutMapping("/{id}/disable")
    public ApiResponse<Void> disableAdmin(@PathVariable Integer id) {
        log.info("禁用管理员: id={}", id);
        adminService.disableAdmin(id);
        return ApiResponse.success("禁用成功");
    }

    /**
     * 启用管理员
     */
    @PutMapping("/{id}/enable")
    public ApiResponse<Void> enableAdmin(@PathVariable Integer id) {
        log.info("启用管理员: id={}", id);
        adminService.enableAdmin(id);
        return ApiResponse.success("启用成功");
    }

    /**
     * 重置管理员密码
     */
    @PutMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Integer id,
                                            @RequestBody java.util.Map<String, String> body) {
        String password = body.get("password");
        log.info("重置管理员密码: id={}", id);
        adminService.resetPassword(id, password);
        return ApiResponse.success("密码重置成功");
    }
}
