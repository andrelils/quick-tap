package com.quicktap.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * 文件上传验证工具
 *
 * 安全特性:
 * - 文件大小限制
 * - 扩展名白名单验证
 * - MIME 类型验证
 * - 魔数（文件签名）验证 - 防止伪装文件
 * - 路径遍历防护 - 防止目录遍历攻击
 * - 文件名安全性检查
 */
@Slf4j
public class FileUploadValidator {

    // 文件大小限制（字节）
    public static final long IMAGE_MAX_SIZE = 10L * 1024 * 1024;      // 10MB
    public static final long FILE_MAX_SIZE = 100L * 1024 * 1024;      // 100MB
    public static final long VIDEO_MAX_SIZE = 500L * 1024 * 1024;     // 500MB

    // 允许的文件扩展名白名单
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(
        Arrays.asList("jpg", "jpeg", "png", "gif", "webp")
    );

    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = new HashSet<>(
        Arrays.asList("mp4", "avi", "mov", "mkv", "flv", "wmv")
    );

    private static final Set<String> ALLOWED_DOCUMENT_EXTENSIONS = new HashSet<>(
        Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "txt", "csv", "ppt", "pptx")
    );

    // MIME 类型白名单
    private static final Set<String> ALLOWED_IMAGE_MIMES = new HashSet<>(
        Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp")
    );

    private static final Set<String> ALLOWED_VIDEO_MIMES = new HashSet<>(
        Arrays.asList("video/mp4", "video/avi", "video/quicktime", "video/x-msvideo",
                     "video/x-matroska", "video/x-flv", "video/x-ms-wmv")
    );

    private static final Set<String> ALLOWED_DOCUMENT_MIMES = new HashSet<>(
        Arrays.asList("application/pdf", "application/msword",
                     "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                     "application/vnd.ms-excel",
                     "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                     "text/plain", "text/csv",
                     "application/vnd.ms-powerpoint",
                     "application/vnd.openxmlformats-officedocument.presentationml.presentation")
    );

    // 文件魔数（签名）- 用于验证文件真实类型
    private static final Map<String, byte[][]> FILE_SIGNATURES = new HashMap<String, byte[][]>() {{
        // JPEG: FF D8 FF
        put("jpg", new byte[][]{{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}});
        put("jpeg", new byte[][]{{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}});

        // PNG: 89 50 4E 47
        put("png", new byte[][]{{(byte) 0x89, 0x50, 0x4E, 0x47}});

        // GIF: 47 49 46 (GIF87a or GIF89a)
        put("gif", new byte[][]{{0x47, 0x49, 0x46}});

        // WebP: RIFF ... WEBP
        put("webp", new byte[][]{{0x52, 0x49, 0x46, 0x46}, {0x57, 0x45, 0x42, 0x50}});

        // PDF: 25 50 44 46 (%PDF)
        put("pdf", new byte[][]{{0x25, 0x50, 0x44, 0x46}});

        // MP4: 00 00 00 18 66 74 79 70 (ftyp)
        put("mp4", new byte[][]{{0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70}});

        // AVI: 52 49 46 46 (RIFF)
        put("avi", new byte[][]{{0x52, 0x49, 0x46, 0x46}});

        // MOV/QT: 00 00 00 (varies)
        put("mov", new byte[][]{{0x00, 0x00, 0x00}});
    }};

    /**
     * 验证图片文件
     */
    public static ValidationResult validateImage(MultipartFile file) {
        return validate(file, IMAGE_MAX_SIZE, ALLOWED_IMAGE_EXTENSIONS, ALLOWED_IMAGE_MIMES, "jpg");
    }

    /**
     * 验证视频文件
     */
    public static ValidationResult validateVideo(MultipartFile file) {
        return validate(file, VIDEO_MAX_SIZE, ALLOWED_VIDEO_EXTENSIONS, ALLOWED_VIDEO_MIMES, "mp4");
    }

    /**
     * 验证文档文件
     */
    public static ValidationResult validateDocument(MultipartFile file) {
        return validate(file, FILE_MAX_SIZE, ALLOWED_DOCUMENT_EXTENSIONS, ALLOWED_DOCUMENT_MIMES, "pdf");
    }

    /**
     * 综合验证文件
     */
    private static ValidationResult validate(
            MultipartFile file,
            long maxSize,
            Set<String> allowedExtensions,
            Set<String> allowedMimes,
            String defaultSignatureCheck
    ) {
        // 1. 检查文件不为空
        if (file == null || file.isEmpty()) {
            return ValidationResult.fail("文件不能为空");
        }

        // 2. 检查文件名
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            return ValidationResult.fail("文件名无效");
        }

        // 3. 防止路径遍历攻击
        if (originalName.contains("..") || originalName.contains("/") || originalName.contains("\\")) {
            log.warn("检测到路径遍历攻击: {}", originalName);
            return ValidationResult.fail("文件名包含非法字符");
        }

        // 4. 检查文件大小
        long fileSize = file.getSize();
        if (fileSize == 0) {
            return ValidationResult.fail("文件大小不能为0");
        }
        if (fileSize > maxSize) {
            return ValidationResult.fail(String.format("文件大小不能超过 %.1f MB", maxSize / (1024.0 * 1024)));
        }

        // 5. 验证扩展名
        String extension = getFileExtension(originalName);
        if (extension == null || extension.isEmpty()) {
            return ValidationResult.fail("无法识别文件扩展名");
        }

        if (!allowedExtensions.contains(extension.toLowerCase())) {
            log.warn("非允许的文件扩展名: {}", extension);
            return ValidationResult.fail(String.format("不支持的文件格式: %s", extension));
        }

        // 6. 验证 MIME 类型
        String contentType = file.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            return ValidationResult.fail("无法识别文件类型");
        }

        if (!allowedMimes.contains(contentType)) {
            log.warn("非允许的 MIME 类型: {} for file: {}", contentType, originalName);
            return ValidationResult.fail(String.format("不支持的 MIME 类型: %s", contentType));
        }

        // 7. 验证文件魔数（签名）- 防止文件伪装
        try {
            byte[] fileBytes = file.getBytes();
            if (!verifyFileSignature(fileBytes, extension)) {
                log.warn("文件魔数验证失败: {}", originalName);
                return ValidationResult.fail("文件格式验证失败，可能是伪装文件");
            }
        } catch (IOException e) {
            log.error("读取文件内容失败", e);
            return ValidationResult.fail("无法读取文件内容");
        }

        return ValidationResult.success();
    }

    /**
     * 验证文件魔数（文件签名）
     *
     * @param fileBytes 文件字节
     * @param extension 文件扩展名
     * @return 文件签名是否匹配
     */
    private static boolean verifyFileSignature(byte[] fileBytes, String extension) {
        if (fileBytes.length == 0) {
            return false;
        }

        byte[][] signatures = FILE_SIGNATURES.get(extension.toLowerCase());
        if (signatures == null) {
            // 如果没有定义签名检查规则，则允许（不安全但避免误报）
            log.debug("未定义文件签名规则: {}", extension);
            return true;
        }

        for (byte[] signature : signatures) {
            if (fileBytes.length >= signature.length) {
                boolean matches = true;
                for (int i = 0; i < signature.length; i++) {
                    if (fileBytes[i] != signature[i]) {
                        matches = false;
                        break;
                    }
                }
                if (matches) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return null;
    }

    /**
     * 生成安全的文件名
     *
     * @param originalFileName 原始文件名
     * @return 安全的文件名（UUID + 原始扩展名）
     */
    public static String generateSafeFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        if (extension == null || extension.isEmpty()) {
            extension = "bin";
        }
        // 使用 UUID 生成唯一文件名
        return java.util.UUID.randomUUID().toString() + "." + extension;
    }

    /**
     * 验证结果包装类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, "验证成功");
        }

        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 私有构造函数
     */
    private FileUploadValidator() {
        throw new IllegalStateException("不能实例化工具类");
    }
}
