package com.quicktap.controller;

import com.quicktap.dto.*;
import com.quicktap.entity.Admin;
import com.quicktap.service.AuthService;
import com.quicktap.service.AuditLoggingService;
import com.quicktap.service.AdminService;
import com.quicktap.service.RoleService;
import com.quicktap.security.JwtTokenProvider;
import com.quicktap.security.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 认证接口控制器
 * 处理登录、注册、token 刷新等操作
 * 集成JWT事件驱动认证流程
 * 集成审计日志记录
 */
@Slf4j
@RestController
@RequestMapping("/api")

public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuditLoggingService auditLoggingService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private RoleService roleService;

    /**
     * 管理员登录
     * 匹配 Node.js: POST /api/admin/auth/login
     */
    @PostMapping("/admin/auth/login")
    public ApiResponse<LoginResponse> adminLogin(@Valid @RequestBody LoginRequest request,
                                                  HttpServletRequest httpRequest) {
        String username = request.getUsername();
        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        log.info("管理员登录请求: username={}, IP={}", username, ipAddress);

        try {
            LoginResponse response = authService.adminLogin(request);

            // 记录成功登录
            auditLoggingService.auditLogin(
                    Long.valueOf(response.getUserId()),
                username,
                true,
                ipAddress,
                userAgent,
                null
            );

            return ApiResponse.success("登录成功", response);
        } catch (Exception e) {
            String failureReason = e.getMessage();

            // 记录失败登录
            auditLoggingService.auditLogin(
                null,
                username,
                false,
                ipAddress,
                userAgent,
                failureReason
            );

            throw e;
        }
    }

    /**
     * 普通用户登录（用户名密码）
     */
    @PostMapping("/user/login")
    public ApiResponse<LoginResponse> userLogin(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        String username = request.getUsername();
        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        log.info("用户登录请求: username={}, IP={}", username, ipAddress);

        try {
            LoginResponse response = authService.userLogin(request);

            // 记录成功登录
            auditLoggingService.auditLogin(
                    Long.valueOf(response.getUserId()),
                username,
                true,
                ipAddress,
                userAgent,
                null
            );

            return ApiResponse.success("登录成功", response);
        } catch (Exception e) {
            // 记录失败登录
            auditLoggingService.auditLogin(
                null,
                username,
                false,
                ipAddress,
                userAgent,
                e.getMessage()
            );

            throw e;
        }
    }

    /**
     * WeChat 小程序登录
     * 匹配 Node.js: POST /api/user/auth/wechat-mini
     */
    @PostMapping("/user/auth/wechat-mini")
    public ApiResponse<LoginResponse> wechatMiniLogin(@RequestBody WechatLoginRequest request) {
        log.info("WeChat小程序登录请求: code={}", request.getCode());
        LoginResponse response = authService.wechatMiniLogin(request.getCode());
        return ApiResponse.success("登录成功", response);
    }

    /**
     * 刷新 JWT Token
     */
    @PostMapping("/refresh-token")
    public ApiResponse<LoginResponse> refreshToken(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ApiResponse.unauthorized("Token 格式不正确");
        }

        String token = authorizationHeader.substring(7);
        log.info("刷新 Token 请求");
        LoginResponse response = authService.refreshToken(token);
        return ApiResponse.success("Token 刷新成功", response);
    }

    /**
     * 验证 Token 有效性
     */
    @GetMapping("/validate-token")
    public ApiResponse<Boolean> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ApiResponse.success(false);
        }

        String token = authorizationHeader.substring(7);
        boolean isValid = authService.validateToken(token);
        return ApiResponse.success(isValid);
    }

    /**
     * 获取当前登录用户信息
     * 需要 JWT 认证
     */
    @GetMapping("/admin/user/info")
    public ApiResponse<?> getCurrentUserInfo() {
        Long userId = securityUtil.getCurrentUserId();
        String username = securityUtil.getCurrentUsername();
        String role = securityUtil.getCurrentRole();
        log.info("获取当前用户信息 | userId: {}, username: {}, role: {}", userId, username, role);

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", userId);
        info.put("username", username);
        info.put("role", role);

        // 查询 admin 表补充昵称等信息
        if (userId != null) {
            try {
                Admin admin = adminService.getAdminById(userId.intValue());
                if (admin != null) {
                    info.put("nickname", admin.getNickname());
                    info.put("avatar", admin.getAvatar());
                    info.put("email", admin.getEmail());
                    info.put("phone", admin.getPhone());
                    info.put("merchantId", admin.getMerchantId());
                    info.put("status", admin.getStatus());
                }
            } catch (Exception e) {
                log.warn("查询管理员信息失败: {}", e.getMessage());
            }
        }

        // 根据角色获取对应的权限列表（而不是硬编码）
        java.util.List<String> permissions = getRolePermissions(role);
        info.put("permissions", permissions);

        return ApiResponse.success("获取成功", info);
    }

    /**
     * 登出 - 发布用户登出事件并清理token
     * 需要提供token用于事件发布和黑名单处理
     */
    @PostMapping("/admin/auth/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                   HttpServletRequest httpRequest) {
        String token = null;
        Integer userId = null;
        String username = null;
        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        // 尝试从Authorization header中提取token和用户信息
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            try {
                userId = jwtTokenProvider.getUserIdFromToken(token);
                username = jwtTokenProvider.getUsernameFromToken(token);
            } catch (Exception e) {
                log.debug("无法从token中提取用户信息: {}", e.getMessage());
            }
        }

        // 发送登出事件（即使无token也发送，便于审计）
        if (token != null || userId != null) {
            authService.logout(token, userId, username);

            // 记录登出事件
            if (userId != null && username != null) {
                auditLoggingService.auditAsync(
                    userId.longValue(),
                    username,
                    com.quicktap.entity.AuditLog.EventType.LOGOUT,
                    com.quicktap.entity.AuditLog.Status.SUCCESS,
                    "用户登出",
                    ipAddress,
                    userAgent
                );
            }

            log.info("用户登出请求已处理 - userId: {}, username: {}, IP: {}", userId, username, ipAddress);
        } else {
            log.info("用户登出请求 - 无有效token");
        }

        return ApiResponse.success("登出成功");
    }

    // ========================================================================
    // 个人中心接口（当前登录用户操作自己的账号）
    // ========================================================================

    /**
     * 当前登录管理员修改自己的密码
     * 前端 auth.js: PUT /api/admin/user/password
     */
    @PutMapping("/admin/user/password")
    public ApiResponse<Void> updateCurrentUserPassword(@RequestBody java.util.Map<String, String> body) {
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.unauthorized("未登录");
        }
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return ApiResponse.badRequest("新密码长度至少 6 个字符");
        }
        log.info("管理员修改自己的密码: userId={}", userId);
        adminService.updatePasswordBySelf(userId.intValue(), oldPassword, newPassword);
        return ApiResponse.success("密码修改成功");
    }

    /**
     * 当前登录管理员修改自己的基础资料（昵称/邮箱/手机号/头像）
     * 前端 auth.js: PUT /api/admin/user/info
     */
    @PutMapping("/admin/user/info")
    public ApiResponse<?> updateCurrentUserInfo(@RequestBody java.util.Map<String, Object> body) {
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.unauthorized("未登录");
        }
        String nickname = body.get("nickname") != null ? String.valueOf(body.get("nickname")) : null;
        String email = body.get("email") != null ? String.valueOf(body.get("email")) : null;
        String phone = body.get("phone") != null ? String.valueOf(body.get("phone")) : null;
        String avatar = body.get("avatar") != null ? String.valueOf(body.get("avatar")) : null;
        String remark = body.get("remark") != null ? String.valueOf(body.get("remark")) : null;
        log.info("管理员修改自己的资料: userId={}", userId);
        Admin updated = adminService.updateInfoBySelf(userId.intValue(), nickname, email, phone, avatar, remark);
        updated.setPassword(null);
        return ApiResponse.success("资料修改成功", updated);
    }

    /**
     * 获取客户端 IP 地址
     */
    private String getClientIP(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        // 检查代理头
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }

        // 检查 X-Real-IP 头（Nginx）
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        // 检查 Cloudflare 头
        ip = request.getHeader("CF-Connecting-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        // 使用远程地址作为最后的选择
        return request.getRemoteAddr();
    }

    /**
     * 获取角色的权限列表
     * 与RoleController中的getRolePermissions方法保持一致
     */
    private java.util.List<String> getRolePermissions(String roleId) {
        // 权限来自 role 表配置（内置角色可页面配置，自定义角色同样），未配置时用 permissions 表默认
        try {
            String roleName = roleId == null ? "" : roleId.trim().toLowerCase();
            if (RoleService.BUILT_IN_ROLES.contains(roleName)
                    || roleService.exists(roleName)) {
                return roleService.getEffectivePermissions(roleName);
            }
        } catch (Exception e) {
            log.warn("读取角色权限失败: {}", e.getMessage());
        }
        return new java.util.ArrayList<>();
    }
}
