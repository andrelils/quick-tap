package com.quicktap.common.annotation;

import java.lang.annotation.*;

/**
 * 操作审计日志注解
 *
 * 用于标注需要记录操作审计日志的方法
 * 被标注的方法执行前后，系统会自动记录该操作到audit_log表
 *
 * 功能特性：
 * - 自动捕获操作人（当前登录用户）
 * - 自动记录操作类型（如CRUD操作）
 * - 自动保存操作前后的数据变化
 * - 自动记录操作结果（成功/失败）
 * - 自动记录操作耗时
 * - 自动记录客户端IP和User-Agent
 *
 * 使用场景：
 * - 敏感信息修改（密码修改、权限变更）
 * - 重要数据操作（用户创建、订单修改、商户审核）
 * - 安全相关操作（登录、登出、授权）
 * - 配置变更（系统配置、功能开关）
 *
 * 使用示例：
 * {@code
 * @AuditLog(operation = "创建管理员", objectType = "ADMIN")
 * @PostMapping("/admin")
 * public ApiResponse<Admin> createAdmin(@RequestBody AdminCreateRequest request) {
 *     Admin admin = adminService.createAdmin(request);
 *     return ApiResponse.success("创建成功", admin);
 * }
 *
 * @AuditLog(operation = "修改管理员权限", objectType = "ADMIN")
 * @PutMapping("/admin/{id}/roles")
 * public ApiResponse<Void> updateAdminRoles(@PathVariable Integer id,
 *                                           @RequestBody List<String> roles) {
 *     adminService.updateRoles(id, roles);
 *     return ApiResponse.success("修改成功");
 * }
 *
 * @AuditLog(operation = "删除用户", objectType = "USER")
 * @DeleteMapping("/user/{id}")
 * public ApiResponse<Void> deleteUser(@PathVariable Long id) {
 *     userService.deleteUser(id);
 *     return ApiResponse.success("删除成功");
 * }
 * }
 *
 * 审计日志拦截器会自动：
 * 1. 拦截被@AuditLog标注的方法调用
 * 2. 记录方法执行前的参数（前置数据快照）
 * 3. 执行原方法
 * 4. 记录方法执行后的结果（后置数据快照）
 * 5. 保存审计日志记录到数据库
 *
 * @see com.quicktap.entity.AuditLog
 * @see com.quicktap.service.AuditLogService
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 操作描述
     * 用于说明这个操作做了什么，如"创建管理员"、"修改用户密码"、"审核商户"
     *
     * @return 操作描述
     */
    String operation() default "";

    /**
     * 操作对象类型
     * 用于分类审计日志，可选值包括：
     * - ADMIN: 管理员相关操作
     * - USER: 用户相关操作
     * - MERCHANT: 商户相关操作
     * - DEVICE: 设备相关操作
     * - ORDER: 订单相关操作
     * - COUPON: 优惠券相关操作
     * - PLAN: 套餐相关操作
     * - CONFIG: 系统配置相关操作
     * - PERMISSION: 权限相关操作
     * - ROLE: 角色相关操作
     *
     * @return 操作对象类型
     */
    String objectType() default "";

    /**
     * 是否记录请求体数据
     * 设置为true时会捕获请求参数作为"操作前数据"
     * 设置为false时则不记录（用于隐私敏感操作）
     *
     * @return 是否记录请求体
     */
    boolean recordRequestBody() default true;

    /**
     * 是否记录响应体数据
     * 设置为true时会捕获响应结果作为"操作后数据"
     * 设置为false时则不记录（用于隐私敏感操作）
     *
     * @return 是否记录响应体
     */
    boolean recordResponseBody() default true;

    /**
     * 敏感操作标记
     * 设置为true时表示这是一个敏感操作
     * 系统会对其进行额外的监控和告警
     *
     * @return 是否为敏感操作
     */
    boolean sensitive() default false;
}
