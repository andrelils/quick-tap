package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI生成记录表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerateRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String recordId;           // 记录ID（UUID）
    private Integer merchantId;        // 商户ID（可选）
    private String type;               // 生成类型：text/image/video
    private String mode;               // 生成模式: new(全新创作)/secondary(二次创作)
    private Long corpusId;             // 关联语料库ID
    private String prompt;             // 用户输入的提示词
    private String result;             // 生成结果
    private Integer status;            // 状态：1成功/0失败
    private Integer tokenUsage;        // Token使用数量（仅用于文本生成）
    private Double cost;               // 生成成本（美元）
}
