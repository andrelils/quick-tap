package com.quicktap.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 配置
 * <p>
 * 主要职责：将本地上传目录映射为可通过 HTTP 访问的静态资源路径。
 * 例如：上传文件保存到 uploads/images/xxx.png，
 *      通过 /uploads/images/xxx.png 即可直接访问。
 * <p>
 * 修复说明：之前 UploadController 把文件写入 uploads/ 目录并返回 /uploads/xxx URL，
 *          但未配置静态资源映射，导致前端访问 URL 时 404。
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.upload-dir:uploads/}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 uploadDir 解析为绝对路径，兼容相对路径（相对应用启动目录）
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String location = uploadPath.toUri().toString();

        log.info("配置静态资源映射: /uploads/** -> {}", location);

        // /uploads/** 映射到本地上传目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
