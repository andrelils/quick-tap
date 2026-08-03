package com.quicktap.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank; /**
 * 创建或更新 AI 配置请求
 */
@Data
public class CreateOrUpdateAiConfigRequest {

    @NotBlank(message = "文本生成模型不能为空")
    private String textModel;

    @NotBlank(message = "图片生成模型不能为空")
    private String imageModel;

    @NotBlank(message = "视频生成模型不能为空")
    private String videoModel;

    private String apiKey;

    private String apiSecret;

    private Boolean enabled = true;
}
