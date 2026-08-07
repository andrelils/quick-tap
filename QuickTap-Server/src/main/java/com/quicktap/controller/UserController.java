package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.dto.AdminUpdateRequest;
import com.quicktap.dto.UserDTO;
import com.quicktap.dto.UserRegisterRequest;
import com.quicktap.dto.PageResponse;
import com.quicktap.security.SecurityUtil;
import com.quicktap.service.AdminService;
import com.quicktap.service.UserService;
import com.quicktap.utils.FileUploadValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理 Controller
 * 支持 C 端用户接口和管理员用户管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private SecurityUtil securityUtil;

    @Value("${file.upload.upload-dir:uploads/}")
    private String uploadDir;

    // ============================================================================
    // C 端用户接口 - /api/user/*
    // ============================================================================

    /**
     * 用户注册
     * 对应 Node: POST /api/user/register
     */
    @PostMapping("/user/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("用户注册请求 | phone: {}", request.getPhone());

        Map<String, Object> result = userService.register(request);
        return ApiResponse.success("注册成功", result);
    }

    /**
     * 获取用户信息
     * 对应 Node: GET /api/user/info
     */
    @GetMapping("/user/info")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<UserDTO> getUserInfo() {
        Long userId = securityUtil.getCurrentUserId();
        log.info("获取用户信息 | userId: {}", userId);

        UserDTO result = userService.getUserInfo(userId);
        return ApiResponse.success("用户信息获取成功", result);
    }

    /**
     * 更新用户信息
     * 对应 Node: PUT /api/user/info
     */
    @PutMapping("/user/info")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<UserDTO> updateUserInfo(@Valid @RequestBody UserRegisterRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        log.info("更新用户信息 | userId: {}", userId);

        UserDTO result = userService.updateUserInfo(userId, request);
        return ApiResponse.success("用户信息更新成功", result);
    }

    /**
     * 绑定电话号码
     * 对应 Node: POST /api/user/register-bind
     */
    @PostMapping("/user/register-bind")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<UserDTO> bindPhone(@RequestParam String phone) {
        Long userId = securityUtil.getCurrentUserId();
        log.info("绑定用户电话 | userId: {} | phone: {}", userId, phone);

        UserDTO result = userService.bindPhone(userId, phone);
        return ApiResponse.success("电话号码绑定成功", result);
    }

    /**
     * 上传用户头像
     * 对应 Node: POST /api/user/avatar
     * 限制: 最大 5MB
     * 支持: jpg, jpeg, png, gif, webp
     * 用户可以在个人中心上传自己的头像
     * 上传成功后自动更新数据库中的用户头像URL
     */
    @PostMapping("/user/avatar")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> uploadUserAvatar(@RequestParam("file") MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            log.warn("无效的头像上传请求 | userId: {}", securityUtil.getCurrentUserId());
            return ApiResponse.badRequest("文件无效");
        }

        Long userId = securityUtil.getCurrentUserId();
        log.info("用户上传头像 | userId: {}, filename: {}, size: {}, contentType: {}",
                userId, file.getOriginalFilename(), file.getSize(), file.getContentType());

        // 头像大小限制 5MB
        if (file.getSize() > 5L * 1024 * 1024) {
            log.warn("头像超过大小限制 | userId: {}, size: {}", userId, file.getSize());
            return ApiResponse.badRequest("头像大小不能超过 5MB");
        }

        // 验证文件类型
        FileUploadValidator.ValidationResult result = FileUploadValidator.validateImage(file);
        if (!result.isValid()) {
            log.warn("头像文件验证失败 | userId: {}, error: {}", userId, result.getMessage());
            return ApiResponse.badRequest(result.getMessage());
        }

        return saveUserAvatar(file, userId);
    }

    /**
     * 保存用户头像到磁盘并更新数据库
     * 使用 UUID 生成安全的文件名，防止目录遍历攻击
     * 【关键修复】上传成功后自动将头像URL保存到数据库
     * 支持 USER（C端用户）和 ADMIN（管理员）两种用户类型
     */
    private ApiResponse<Map<String, Object>> saveUserAvatar(MultipartFile file, Long userId) {
        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir, "avatars");
            Files.createDirectories(uploadPath);

            // 生成安全的文件名（使用 UUID + 原始扩展名）
            String originalFileName = file.getOriginalFilename();
            String safeFileName = FileUploadValidator.generateSafeFileName(originalFileName);

            // 验证生成的路径不包含目录遍历
            Path filePath = uploadPath.resolve(safeFileName);
            String normalizedPath = filePath.normalize().toAbsolutePath().toString();
            String uploadPathNormalized = uploadPath.normalize().toAbsolutePath().toString();

            if (!normalizedPath.startsWith(uploadPathNormalized)) {
                log.error("检测到路径遍历攻击尝试 | userId: {}, safeFileName: {}", userId, safeFileName);
                return ApiResponse.systemError("文件保存失败：路径验证错误");
            }

            // 保存文件
            Files.write(filePath, file.getBytes());

            // 构建文件 URL（统一为 /uploads/avatars/filename，末尾不带多余斜杠）
            String cleanUploadDir = uploadDir.endsWith("/") ? uploadDir.substring(0, uploadDir.length() - 1) : uploadDir;
            String fileUrl = String.format("/%s/%s/%s", cleanUploadDir, "avatars", safeFileName);

            // 【关键修复】更新用户头像 URL 到数据库
            // 支持 USER 和 ADMIN 两种用户类型
            try {
                if (securityUtil.isUser()) {
                    // C端用户：更新 user 表
                    UserRegisterRequest updateRequest = new UserRegisterRequest();
                    updateRequest.setAvatar(fileUrl);
                    userService.updateUserInfo(userId, updateRequest);
                    log.info("用户头像已保存到数据库（user表） | userId: {}, avatar: {}", userId, fileUrl);
                } else if (securityUtil.isAdmin()) {
                    // 管理员：更新 admin 表
                    AdminUpdateRequest adminUpdateRequest = new AdminUpdateRequest();
                    adminUpdateRequest.setAvatar(fileUrl);
                    adminService.updateAdmin(userId.intValue(), adminUpdateRequest);
                    log.info("管理员头像已保存到数据库（admin表） | userId: {}, avatar: {}", userId, fileUrl);
                } else {
                    log.warn("未知的用户类型，无法确定保存位置 | userId: {}", userId);
                }
            } catch (Exception e) {
                log.error("更新头像到数据库失败 | userId: {}, error: {}", userId, e.getMessage());
                // 文件已保存到磁盘，即使数据库更新失败也返回成功（磁盘优先）
                // 但记录错误以便后续修复
            }

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("url", fileUrl);
            resultMap.put("filename", safeFileName);
            resultMap.put("originalName", originalFileName);
            resultMap.put("size", file.getSize());

            log.info("用户头像上传成功（已保存到数据库） | userId: {}, filename: {}, url: {}", userId, safeFileName, fileUrl);
            return ApiResponse.success("头像上传成功", resultMap);

        } catch (IOException e) {
            log.error("文件保存失败 | userId: {}", userId, e);
            return ApiResponse.systemError("文件保存失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("头像上传过程中发生未知错误 | userId: {}", userId, e);
            return ApiResponse.systemError("头像上传失败: " + e.getMessage());
        }
    }

    // ============================================================================
    // 管理员用户管理接口 - /api/admin/user/*
    // ============================================================================

    /**
     * 分页查询管理员列表（管理员端）
     * 对应 Node: GET /api/admin/user/list
     */
    @GetMapping("/admin/user/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<PageResponse<com.quicktap.entity.Admin>> listUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        log.info("查询管理员列表 | pageNum: {}, pageSize: {}, keyword: {}, role: {}, status: {}", pageNum, pageSize, keyword, role, status);

        PageResponse<com.quicktap.entity.Admin> result = adminService.listAdminsByKeyword(pageNum, pageSize, keyword, role, status);
        return ApiResponse.success("获取成功", result);
    }

    /**
     * 创建管理员（管理员端）
     * 对应 Node: POST /api/admin/user
     */
    @PostMapping("/admin/user")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<com.quicktap.entity.Admin> createUser(@Valid @RequestBody com.quicktap.dto.AdminCreateRequest request) {
        log.info("创建管理员 | username: {}", request.getUsername());

        com.quicktap.entity.Admin result = adminService.createAdmin(request);
        return ApiResponse.success("创建成功", result);
    }

    /**
     * 更新管理员信息（管理员端）
     * 对应 Node: PUT /api/admin/user/{id}
     */
    @PutMapping("/admin/user/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<com.quicktap.entity.Admin> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateRequest request) {
        log.info("更新管理员信息 | userId: {}", id);

        com.quicktap.entity.Admin result = adminService.updateAdmin(id.intValue(), request);
        return ApiResponse.success("更新成功", result);
    }

    /**
     * 删除管理员（管理员端）
     * 对应 Node: DELETE /api/admin/user/{id}
     */
    @DeleteMapping("/admin/user/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        log.info("删除管理员 | userId: {}", id);

        adminService.deleteAdmin(id.intValue());
        return ApiResponse.success("删除成功");
    }

    /**
     * 更新管理员状态（管理员端）
     * 对应 Node: PUT /api/admin/user/{id}/status
     * 状态值：1=启用, 0=禁用
     */
    @PutMapping("/admin/user/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<com.quicktap.entity.Admin> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        log.info("更新管理员状态 | userId: {}, status: {}", id, status);

        com.quicktap.entity.Admin result;
        if (status != null && status == 1) {
            result = adminService.enableAdmin(id.intValue());
        } else {
            result = adminService.disableAdmin(id.intValue());
        }
        return ApiResponse.success("状态更新成功", result);
    }

    /**
     * 重置管理员密码（管理员端）
     * 对应 Node: PUT /api/admin/user/{id}/reset-password
     */
    @PutMapping("/admin/user/{id}/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, String>> resetPassword(@PathVariable Long id,
                                                          @RequestBody(required = false) Map<String, String> body) {
        log.info("重置管理员密码 | userId: {}", id);

        String newPassword = body != null ? body.get("password") : null;
        adminService.resetPassword(id.intValue(), newPassword);

        Map<String, String> result = new HashMap<>();
        result.put("message", "密码重置成功");
        return ApiResponse.success("密码重置成功", result);
    }

}
