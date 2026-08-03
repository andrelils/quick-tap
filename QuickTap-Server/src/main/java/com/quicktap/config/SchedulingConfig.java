package com.quicktap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置
 * 启用 @Scheduled 注解支持
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // 该配置类用于启用 Spring 的定时任务支持
}
