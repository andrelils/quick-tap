package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.exception.BusinessException;
import com.quicktap.utils.FileUploadValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 文件上传控制器
 * 处理图片、视频、文档等文件上传
 *
 * 安全特性:
 * - 文件大小限制
 * - MIME 类型验证
 * - 扩展名白名单验证
 * - 文件魔数（签名）验证
 * - 路径遍历防护
 * - 文件名安全性检查
 *
 * 匹配 Node.js: POST /api/admin/upload/*
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/upload")
public class UploadController {

    @Value("${file.upload.upload-dir:uploads/}")
    private String uploadDir;

    @org.springframework.beans.factory.annotation.Autowired
    private com.quicktap.service.MerchantQuotaService merchantQuotaService;

    /**
     * 上传图片
     * 限制: 最大 10MB
     * 支持: jpg, jpeg, png, gif, webp
     * 可选参数 merchantId：传入时校验该商户存储额度并在成功后累计已用空间
     */
    @PostMapping("/image")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MERCHANT')")
    public ApiResponse<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file,
                                                        @RequestParam(required = false) Integer merchantId) {
        if (file == null || file.getOriginalFilename() == null) {
            log.warn("无效的图片上传请求");
            return ApiResponse.badRequest("文件无效");
        }

        log.info("图片上传请求: filename={}, size={}, contentType={}, merchantId={}",
                 file.getOriginalFilename(), file.getSize(), file.getContentType(), merchantId);

        // 使用 FileUploadValidator 验证文件
        FileUploadValidator.ValidationResult result = FileUploadValidator.validateImage(file);
        if (!result.isValid()) {
            log.warn("图片文件验证失败: {}", result.getMessage());
            return ApiResponse.badRequest(result.getMessage());
        }

        return saveFile(file, "images", merchantId);
    }

    /**
     * 上传头像
     * 限制: 最大 5MB
     * 支持: jpg, jpeg, png, gif, webp
     * 存储子目录: uploads/avatars/
     */
    @PostMapping("/avatar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MERCHANT', 'USER')")
    public ApiResponse<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file,
                                                         @RequestParam(required = false) Integer merchantId) {
        if (file == null || file.getOriginalFilename() == null) {
            log.warn("无效的头像上传请求");
            return ApiResponse.badRequest("文件无效");
        }

        log.info("头像上传请求: filename={}, size={}, contentType={}, merchantId={}",
                 file.getOriginalFilename(), file.getSize(), file.getContentType(), merchantId);

        // 头像额外限制 5MB
        if (file.getSize() > 5L * 1024 * 1024) {
            return ApiResponse.badRequest("头像大小不能超过 5MB");
        }

        FileUploadValidator.ValidationResult result = FileUploadValidator.validateImage(file);
        if (!result.isValid()) {
            log.warn("头像文件验证失败: {}", result.getMessage());
            return ApiResponse.badRequest(result.getMessage());
        }

        return saveFile(file, "avatars", merchantId);
    }

    /**
     * 上传文件
     * 限制: 最大 100MB
     * 支持: pdf, doc, docx, xls, xlsx, txt, csv, ppt, pptx 等
     */
    @PostMapping("/file")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file,
                                                       @RequestParam(required = false) Integer merchantId) {
        if (file == null || file.getOriginalFilename() == null) {
            log.warn("无效的文件上传请求");
            return ApiResponse.badRequest("文件无效");
        }

        log.info("文件上传请求: filename={}, size={}, contentType={}, merchantId={}",
                 file.getOriginalFilename(), file.getSize(), file.getContentType(), merchantId);

        // 使用 FileUploadValidator 验证文件
        FileUploadValidator.ValidationResult result = FileUploadValidator.validateDocument(file);
        if (!result.isValid()) {
            log.warn("文件验证失败: {}", result.getMessage());
            return ApiResponse.badRequest(result.getMessage());
        }

        return saveFile(file, "files", merchantId);
    }

    /**
     * 上传视频
     * 限制: 最大 500MB
     * 支持: mp4, avi, mov, mkv, flv, wmv 等
     */
    @PostMapping("/video")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> uploadVideo(@RequestParam("file") MultipartFile file,
                                                        @RequestParam(required = false) Integer merchantId) {
        if (file == null || file.getOriginalFilename() == null) {
            log.warn("无效的视频上传请求");
            return ApiResponse.badRequest("文件无效");
        }

        log.info("视频上传请求: filename={}, size={}, contentType={}, merchantId={}",
                 file.getOriginalFilename(), file.getSize(), file.getContentType(), merchantId);

        // 使用 FileUploadValidator 验证文件
        FileUploadValidator.ValidationResult result = FileUploadValidator.validateVideo(file);
        if (!result.isValid()) {
            log.warn("视频文件验证失败: {}", result.getMessage());
            return ApiResponse.badRequest(result.getMessage());
        }

        return saveFile(file, "videos", merchantId);
    }

    /**
     * 保存文件到磁盘，可选校验并累计商户存储额度
     * merchantId 不为空时：先校验剩余存储空间，不足则拒绝；成功后累计已用空间
     */
    private ApiResponse<Map<String, Object>> saveFile(MultipartFile file, String subDir, Integer merchantId) {
        // 存储额度校验（可选）
        if (merchantId != null && merchantId > 0) {
            try {
                boolean hasQuota = merchantQuotaService.checkStorageQuota(merchantId, file.getSize());
                if (!hasQuota) {
                    Map<String, Object> usage = merchantQuotaService.getQuotaUsage(merchantId);
                    Map<?, ?> storage = (Map<?, ?>) usage.get("storage");
                    long limit = ((Number) storage.get("limit")).longValue();
                    long used = ((Number) storage.get("used")).longValue();
                    String limitText = limit <= 0 ? "不限" : limit + "MB";
                    return ApiResponse.badRequest("存储空间不足，当前已使用 " + used + "MB / 上限 " + limitText +
                            "，无法上传（文件大小 " + (file.getSize() / 1024 / 1024.0) + "MB）");
                }
            } catch (BusinessException e) {
                // 商户不存在等场景，不阻塞上传（管理员可正常操作）
                log.warn("存储额度校验跳过: merchantId={}, reason={}", merchantId, e.getMessage());
            } catch (Exception e) {
                log.warn("存储额度校验异常，跳过: {}", e.getMessage());
            }
        }

        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir, subDir);
            Files.createDirectories(uploadPath);

            // 生成安全的文件名（使用 UUID + 原始扩展名）
            String originalFileName = file.getOriginalFilename();
            String safeFileName = FileUploadValidator.generateSafeFileName(originalFileName);

            // 验证生成的路径不包含目录遍历
            Path filePath = uploadPath.resolve(safeFileName);
            String normalizedPath = filePath.normalize().toAbsolutePath().toString();
            String uploadPathNormalized = uploadPath.normalize().toAbsolutePath().toString();

            if (!normalizedPath.startsWith(uploadPathNormalized)) {
                log.error("检测到路径遍历攻击尝试: {}", safeFileName);
                return ApiResponse.systemError("文件保存失败：路径验证错误");
            }

            // 保存文件
            Files.write(filePath, file.getBytes());

            // 累计商户存储使用量（上传成功后）
            if (merchantId != null && merchantId > 0) {
                try {
                    long sizeInMB = Math.max(1, (long) Math.ceil(file.getSize() / (1024.0 * 1024.0)));
                    merchantQuotaService.updateStorageUsage(merchantId, sizeInMB);
                } catch (Exception e) {
                    log.error("累计存储使用量失败: merchantId={}, err={}", merchantId, e.getMessage());
                }
            }

            // 构建文件 URL（统一为 /uploads/subDir/filename，末尾不带多余斜杠）
            String cleanUploadDir = uploadDir.endsWith("/") ? uploadDir.substring(0, uploadDir.length() - 1) : uploadDir;
            String fileUrl = String.format("/%s/%s/%s", cleanUploadDir, subDir, safeFileName);

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", safeFileName);
            result.put("originalName", originalFileName);
            result.put("size", file.getSize());

            log.info("文件保存成功: filename={}, url={}", safeFileName, fileUrl);
            return ApiResponse.success("上传成功", result);

        } catch (IOException e) {
            log.error("文件保存失败", e);
            return ApiResponse.systemError("文件保存失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("文件上传过程中发生未知错误", e);
            return ApiResponse.systemError("文件上传失败: " + e.getMessage());
        }
    }
}
