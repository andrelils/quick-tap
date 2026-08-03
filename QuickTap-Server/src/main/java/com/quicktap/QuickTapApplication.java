package com.quicktap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * QuickTap Server 启动类
 *
 * @author QuickTap Team
 * @date 2024-01-01
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"com.quicktap"})
@EnableKafka
@EnableAsync
public class QuickTapApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(QuickTapApplication.class, args);

        // 从 Spring Environment 获取配置（这是正确的方式）
        Environment env = context.getEnvironment();
        String[] activeProfiles = env.getActiveProfiles();
        String activeProfile = (activeProfiles.length > 0) ? activeProfiles[0] : "default";
        String port = env.getProperty("server.port", "8080");

        // 使用 logger 输出启动信息
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║         QuickTap Server Started Successfully             ║");
        log.info("║         Version: 1.0.0                                   ║");
        log.info("║         Environment: {}", String.format("%-45s║", activeProfile));
        log.info("║         Port: {}", String.format("%-51s║", port));
        log.info("║         API URL: http://localhost:{}/api", port);
        log.info("║         Swagger UI: http://localhost:{}/api/swagger-ui.html", port);
        log.info("╚══════════════════════════════════════════════════════════╝");
    }
}
