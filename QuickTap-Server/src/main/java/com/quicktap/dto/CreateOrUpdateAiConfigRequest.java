package com.quicktap.dto;

import lombok.Data;

/**
 * 创建或更新 AI 配置请求
 */
@Data
public class CreateOrUpdateAiConfigRequest {

    private String textModel;

    private String imageModel;

    private String videoModel;

    private String apiKey;

    private String apiSecret;

    private Boolean enabled = true;

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
}
