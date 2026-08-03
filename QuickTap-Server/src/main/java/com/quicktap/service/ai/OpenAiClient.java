package com.quicktap.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * OpenAI API 客户端接口和实现
 * 支持真实 API 调用和 Mock 实现
 */
public interface OpenAiClient {

    /**
     * 生成文本内容
     * @param prompt 提示词
     * @return 生成结果
     */
    TextGenerationResponse generateText(String prompt);

    /**
     * 生成图片 URL（通过 DALL-E）
     * @param prompt 提示词
     * @return 图片生成结果
     */
    ImageGenerationResponse generateImage(String prompt);

    /**
     * 文本生成响应
     */
    @Data
    @AllArgsConstructor
    class TextGenerationResponse {
        private String id;
        private String content;
        private int tokenUsage;
        private double cost;
        private LocalDateTime createdAt;
    }

    /**
     * 图片生成响应
     */
    @Data
    @AllArgsConstructor
    class ImageGenerationResponse {
        private String id;
        private String imageUrl;
        private LocalDateTime createdAt;
    }

    /**
     * 真实 OpenAI 客户端实现
     */
    @Slf4j
    class RealOpenAiClient implements OpenAiClient {

        private final RestTemplate restTemplate;
        private final String apiKey;
        private final String apiUrl;
        private final String model;
        private final int timeout;
        private final int maxTokens;
        private final double temperature;
        private final ObjectMapper objectMapper = new ObjectMapper();

        public RealOpenAiClient(RestTemplate restTemplate, String apiKey, String apiUrl,
                               String model, int timeout, int maxTokens, double temperature) {
            this.restTemplate = restTemplate;
            this.apiKey = apiKey;
            this.apiUrl = apiUrl;
            this.model = model;
            this.timeout = timeout;
            this.maxTokens = maxTokens;
            this.temperature = temperature;
        }

        @Override
        public TextGenerationResponse generateText(String prompt) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + apiKey);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", model);
                requestBody.put("messages", new Object[]{
                    Map.of("role", "user", "content", prompt)
                });
                requestBody.put("max_tokens", maxTokens);
                requestBody.put("temperature", temperature);

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                long startTime = System.currentTimeMillis();
                ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl + "/chat/completions",
                    request,
                    String.class
                );
                long duration = System.currentTimeMillis() - startTime;

                if (response.getStatusCode().is2xxSuccessful()) {
                    JsonNode jsonNode = objectMapper.readTree(response.getBody());
                    String content = jsonNode.get("choices").get(0).get("message").get("content").asText();
                    int tokens = jsonNode.get("usage").get("total_tokens").asInt();
                    double cost = tokens * 0.000002; // 估算成本

                    log.info("✓ OpenAI 文本生成成功 | 耗时: {}ms | Tokens: {} | Cost: ${}", duration, tokens, String.format("%.6f", cost));

                    return new TextGenerationResponse(
                        UUID.randomUUID().toString(),
                        content,
                        tokens,
                        cost,
                        LocalDateTime.now()
                    );
                } else {
                    log.error("❌ OpenAI API 请求失败 | Status: {}", response.getStatusCode());
                    throw new RuntimeException("OpenAI API 返回错误状态码: " + response.getStatusCode());
                }

            } catch (Exception e) {
                log.error("❌ OpenAI 文本生成失败: {}", e.getMessage(), e);
                throw new RuntimeException("OpenAI 文本生成失败: " + e.getMessage(), e);
            }
        }

        @Override
        public ImageGenerationResponse generateImage(String prompt) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + apiKey);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("prompt", prompt);
                requestBody.put("n", 1);
                requestBody.put("size", "1024x1024");

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                long startTime = System.currentTimeMillis();
                ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl + "/images/generations",
                    request,
                    String.class
                );
                long duration = System.currentTimeMillis() - startTime;

                if (response.getStatusCode().is2xxSuccessful()) {
                    JsonNode jsonNode = objectMapper.readTree(response.getBody());
                    String imageUrl = jsonNode.get("data").get(0).get("url").asText();

                    log.info("✓ DALL-E 图片生成成功 | 耗时: {}ms | URL: {}", duration, imageUrl);

                    return new ImageGenerationResponse(
                        UUID.randomUUID().toString(),
                        imageUrl,
                        LocalDateTime.now()
                    );
                } else {
                    log.error("❌ DALL-E API 请求失败 | Status: {}", response.getStatusCode());
                    throw new RuntimeException("DALL-E API 返回错误状态码: " + response.getStatusCode());
                }

            } catch (Exception e) {
                log.error("❌ DALL-E 图片生成失败: {}", e.getMessage(), e);
                throw new RuntimeException("DALL-E 图片生成失败: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Mock OpenAI 客户端实现
     * 当 API Key 未配置时使用
     */
    @Slf4j
    class MockOpenAiClient implements OpenAiClient {

        @Override
        public TextGenerationResponse generateText(String prompt) {
            log.debug("🔄 使用 Mock OpenAI 客户端生成文本 | Prompt: {}", prompt);
            return new TextGenerationResponse(
                UUID.randomUUID().toString(),
                "Mock 生成结果: 这是基于提示词 \"" + prompt + "\" 的模拟生成文本。",
                150,
                0.0003,
                LocalDateTime.now()
            );
        }

        @Override
        public ImageGenerationResponse generateImage(String prompt) {
            log.debug("🔄 使用 Mock DALL-E 客户端生成图片 | Prompt: {}", prompt);
            return new ImageGenerationResponse(
                UUID.randomUUID().toString(),
                "https://via.placeholder.com/1024x1024?text=Generated+Image",
                LocalDateTime.now()
            );
        }
    }
}
