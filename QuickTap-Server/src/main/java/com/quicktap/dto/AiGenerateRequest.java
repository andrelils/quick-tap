package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * AI 内容生成请求
 * 用于文本、图片、视频生成
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerateRequest {

    /**
     * 商户ID (必填)
     */
    @NotNull(message = "商户ID不能为空")
    private Integer merchantId;

    /**
     * 提示词 (必填)
     */
    @NotBlank(message = "提示词不能为空")
    private String prompt;

    /**
     * 可选: 生成风格 (如: modern, vintage, etc.)
     */
    private String style;

    /**
     * 可选: 生成数量 (仅对图片/视频)
     */
    private Integer count;

    /**
     * 可选: 生成分辨率 (仅对图片)
     */
    private String resolution;
}
