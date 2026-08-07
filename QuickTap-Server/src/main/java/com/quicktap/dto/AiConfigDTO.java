package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * AI 配置 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiConfigDTO {

    /**
     * 配置 ID
     */
    private Long id;

    /**
     * 商户 ID
     */
    private Long merchantId;

    /**
     * 文本生成模型
     */
    private String textModel;

    /**
     * 图片生成模型
     */
    private String imageModel;

    /**
     * 视频生成模型
     */
    private String videoModel;

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

