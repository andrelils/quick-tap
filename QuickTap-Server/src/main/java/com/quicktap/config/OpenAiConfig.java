package com.quicktap.config;

import com.quicktap.service.ai.OpenAiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * OpenAI API 配置类
 * 用于初始化 OpenAI API 客户端和相关配置
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "ai.openai.enabled", havingValue = "true")
public class OpenAiConfig {

    @Value("${ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${ai.openai.api-url:https://api.openai.com/v1}")
    private String openAiApiUrl;

    @Value("${ai.openai.model:gpt-3.5-turbo}")
    private String openAiModel;

    @Value("${ai.openai.timeout:30000}")
    private int timeout;

    @Value("${ai.openai.max-tokens:2000}")
    private int maxTokens;

    @Value("${ai.openai.temperature:0.7}")
    private double temperature;

    /**
     * 创建 RestTemplate Bean
     * 用于调用 OpenAI API
     */
    @Bean
    public RestTemplate openAiRestTemplate() {
        return new RestTemplate();
    }

    /**
     * 创建 OpenAI 客户端
     */
    @Bean
    public OpenAiClient openAiClient(RestTemplate openAiRestTemplate) {
        if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
            log.warn("⚠️ OpenAI API Key 未配置，将使用 Mock 实现");
            return new OpenAiClient.MockOpenAiClient();
        }

        log.info("✓ OpenAI 客户端已初始化 | Model: {} | URL: {}", openAiModel, openAiApiUrl);

        return new OpenAiClient.RealOpenAiClient(
            openAiRestTemplate,
            openAiApiKey,
            openAiApiUrl,
            openAiModel,
            timeout,
            maxTokens,
            temperature
        );
    }
}
