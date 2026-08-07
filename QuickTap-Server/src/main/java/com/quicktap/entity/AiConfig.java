package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 配置表
 * 支持全局配置和商户级配置
 * 商户级配置优先于全局配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiConfig extends BaseEntity {

    /**
     * 商户 ID（NULL 表示全局配置）
     */
    private Long merchantId;

    /**
     * 文本生成模型（如 gpt-3.5-turbo）
     */
    private String textModel;

    /**
     * 图片生成模型（如 dall-e-3）
     */
    private String imageModel;

    /**
     * 视频生成模型
     */
    private String videoModel;

    /**
     * API Key（加密存储）
     */
    private String apiKey;

    /**
     * API Secret（加密存储）
     */
    private String apiSecret;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 文本生成提示词
     */
    private String textPrompt;

    /**
     * 图片生成提示词
     */
    private String imagePrompt;

    /**
     * 视频生成提示词
     */
    private String videoPrompt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
