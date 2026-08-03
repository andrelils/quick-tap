package com.quicktap.common.annotation;

import java.lang.annotation.*;

/**
 * 权限角色验证注解
 *
 * 用于在方法级别声明所需的角色权限
 * 被标注的方法仅允许拥有指定角色的用户访问
 * 如果用户权限不足，系统会抛出UnauthorizedException异常
 *
 * 功能特性：
 * - 支持单个角色验证
 * - 支持多个角色的OR逻辑（拥有任意一个即可访问）
 * - 与Spring Security @PreAuthorize结合使用
 * - 自动进行权限检查和日志记录
 *
 * 使用场景：
 * - 限制只有超级管理员可以访问的功能
 * - 限制只有管理员可以访问的功能
 * - 限制商户端和管理员可访问但用户端不可访问的功能
 * - 细粒度的权限控制
 *
 * 可用的角色值：
 * - "SUPER_ADMIN" - 超级管理员
 * - "ADMIN" - 普通管理员
 * - "MERCHANT" - 商户
 * - "USER" - 普通用户
 *
 * 使用示例：
 * {@code
 * // 仅超级管理员可访问
 * @RequireRole("SUPER_ADMIN")
 * @DeleteMapping("/admin/{id}")
 * public ApiResponse<Void> deleteAdmin(@PathVariable Integer id) {
 *     adminService.deleteAdmin(id);
 *     return ApiResponse.success("删除成功");
 * }
 *
 * // 超级管理员和普通管理员都可访问
 * @RequireRole({"SUPER_ADMIN", "ADMIN"})
 * @GetMapping("/admin/list")
 * public ApiResponse<PageResponse<Admin>> listAdmins(
 *         @RequestParam(defaultValue = "1") Integer pageNum,
 *         @RequestParam(defaultValue = "10") Integer pageSize) {
 *     PageResponse<Admin> page = adminService.getAdminList(pageNum, pageSize);
 *     return ApiResponse.success("查询成功", page);
 * }
 *
 * // 商户和管理员都可访问
 * @RequireRole({"MERCHANT", "ADMIN", "SUPER_ADMIN"})
 * @GetMapping("/quota/usage")
 * public ApiResponse<Map<String, Object>> getQuotaUsage(@RequestParam Integer merchantId) {
 *     Map<String, Object> usage = quotaService.getQuotaUsage(merchantId);
 *     return ApiResponse.success("查询成功", usage);
 * }
 * }
 *
 * 权限检查流程：
 * 1. 用户请求到达
 * 2. Spring Security过滤器验证JWT Token
 * 3. 拦截器提取用户权限信息
 * 4. 检查用户角色是否在@RequireRole指定的角色列表中
 * 5. 若不符合，抛出UnauthorizedException（403 Forbidden）
 * 6. 若符合，继续执行方法
 * 7. 同时记录权限相关的审计日志
 *
 * 与Spring Security的关系：
 * - @RequireRole是高层业务级别的权限控制
 * - @PreAuthorize是框架级别的权限控制
 * - 两者可配合使用实现多层防护
 * - @RequireRole更易于业务方理解
 *
 * @see com.quicktap.common.exception.UnauthorizedException
 * @see org.springframework.security.access.prepost.PreAuthorize
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 所需的角色集合
     * 如果指定多个角色，表示用户拥有其中任意一个即可访问（OR逻辑）
     *
     * 可选值：
     * - "SUPER_ADMIN" - 超级管理员（最高权限）
     * - "ADMIN" - 普通管理员
     * - "MERCHANT" - 商户用户
     * - "USER" - 普通用户
     *
     * 示例：
     * - @RequireRole("SUPER_ADMIN") - 仅超级管理员
     * - @RequireRole({"ADMIN", "SUPER_ADMIN"}) - 管理员或超级管理员
     * - @RequireRole({"MERCHANT", "ADMIN"}) - 商户或管理员
     *
     * @return 所需的角色数组
     */
    String[] value() default {};

    /**
     * 权限检查失败时的错误消息
     * 如果用户权限不足，会返回此消息
     *
     * @return 错误消息
     */
    String message() default "You don't have permission to perform this operation";

    /**
     * 是否在审计日志中记录此权限验证
     * 设置为true时，权限验证结果会被记录到审计日志
     *
     * @return 是否记录到审计日志
     */
    boolean auditLog() default true;
}
