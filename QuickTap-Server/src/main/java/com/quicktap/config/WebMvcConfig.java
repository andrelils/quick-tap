package com.quicktap.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 配置 - 静态资源映射
 *
 * 将 uploads/ 目录映射到 /uploads/** URL 路径。
 * 由于 server.servlet.context-path=/api，完整请求 URL 为 /api/uploads/**，
 * 但资源处理器注册的是 context-path 剥离后的 servlet path（即 /uploads/**）。
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.upload-dir:uploads/}")
    private String uploadDir;

    private String resolvedLocation;

    @PostConstruct
    public void init() {
        Path path = Paths.get(uploadDir);

        if (!path.isAbsolute()) {
            String workingDir = System.getProperty("user.dir");
            Path absolute = Paths.get(workingDir, uploadDir).toAbsolutePath().normalize();
            log.info("相对路径 '{}' 解析为绝对路径: {} (working dir: {})", uploadDir, absolute, workingDir);
            path = absolute;
        } else {
            path = path.toAbsolutePath().normalize();
            log.info("使用绝对路径: {}", path);
        }

        try {
            Files.createDirectories(path);
        } catch (Exception e) {
            log.warn("创建上传目录失败: {} - {}", path, e.getMessage());
        }

        String locationPath = path.toString();
        if (!locationPath.endsWith(File.separator)) {
            locationPath = locationPath + File.separator;
        }

        // Windows 路径反斜杠必须转为正斜杠，否则 file: URL 无法被 Spring 解析
        locationPath = locationPath.replace('\\', '/');

        this.resolvedLocation = "file:" + locationPath;
        log.info("上传文件目录: {}", this.resolvedLocation);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 项目没有配置 server.servlet.context-path，/api 前缀直接硬编码在 Controller 中。
        // 因此资源处理器 pattern 也需要带 /api 前缀。
        log.info("注册资源处理器: /api/uploads/** -> {}", resolvedLocation);

        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(resolvedLocation);
    }
}
