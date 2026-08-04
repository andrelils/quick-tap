package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
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

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    /**
     * 上传图片
     * 限制: 最大 10MB
     * 支持: jpg, jpeg, png, gif, webp
     */
    @PostMapping("/image")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            log.warn("无效的图片上传请求");
            return ApiResponse.badRequest("文件无效");
        }

        log.info("图片上传请求: filename={}, size={}, contentType={}",
                 file.getOriginalFilename(), file.getSize(), file.getContentType());

        // 使用 FileUploadValidator 验证文件
        FileUploadValidator.ValidationResult result = FileUploadValidator.validateImage(file);
        if (!result.isValid()) {
            log.warn("图片文件验证失败: {}", result.getMessage());
            return ApiResponse.badRequest(result.getMessage());
        }

        return saveFile(file, "images");
    }

    /**
     * 上传文件
     * 限制: 最大 100MB
     * 支持: pdf, doc, docx, xls, xlsx, txt, csv, ppt, pptx 等
     */
    @PostMapping("/file")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            log.warn("无效的文件上传请求");
            return ApiResponse.badRequest("文件无效");
        }

        log.info("文件上传请求: filename={}, size={}, contentType={}",
                 file.getOriginalFilename(), file.getSize(), file.getContentType());

        // 使用 FileUploadValidator 验证文件
        FileUploadValidator.ValidationResult result = FileUploadValidator.validateDocument(file);
        if (!result.isValid()) {
            log.warn("文件验证失败: {}", result.getMessage());
            return ApiResponse.badRequest(result.getMessage());
        }

        return saveFile(file, "files");
    }

    /**
     * 上传视频
     * 限制: 最大 500MB
     * 支持: mp4, avi, mov, mkv, flv, wmv 等
     */
    @PostMapping("/video")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> uploadVideo(@RequestParam("file") MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            log.warn("无效的视频上传请求");
            return ApiResponse.badRequest("文件无效");
        }

        log.info("视频上传请求: filename={}, size={}, contentType={}",
                 file.getOriginalFilename(), file.getSize(), file.getContentType());

        // 使用 FileUploadValidator 验证文件
        FileUploadValidator.ValidationResult result = FileUploadValidator.validateVideo(file);
        if (!result.isValid()) {
            log.warn("视频文件验证失败: {}", result.getMessage());
            return ApiResponse.badRequest(result.getMessage());
        }

        return saveFile(file, "videos");
    }

    /**
     * 保存文件到磁盘
     * 使用 UUID 生成安全的文件名，防止目录遍历攻击
     */
    private ApiResponse<Map<String, Object>> saveFile(MultipartFile file, String subDir) {
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

            // 构建文件 URL（必须包含 context-path，否则后端无法识别）
            // 去除 uploadDir 和 contextPath 尾部斜杠，避免双斜杠
            String cleanContext = contextPath.endsWith("/") ? contextPath.substring(0, contextPath.length() - 1) : contextPath;
            String cleanUploadDir = uploadDir.endsWith("/") ? uploadDir.substring(0, uploadDir.length() - 1) : uploadDir;
            String fileUrl = String.format("%s/%s/%s/%s", cleanContext, cleanUploadDir, subDir, safeFileName);

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
