package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * AI生成记录DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiGenerateRecordDTO {
    private Long id;
    private Long userId;
    private String prompt;
    private String result;
    private String mode;                      // new/secondary 生成模式
    private Long corpusId;                    // 语料库ID
    private Integer status;                   // 生成状态
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateGenerateRequest {
        @NotBlank(message = "提示词不能为空")
        private String prompt;

        private String mode;                  // new/secondary

        private Long corpusId;                // 可选，用于二次创作
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenerateRecordListItemDTO {
        private Long id;
        private String prompt;
        private String result;
        private String mode;
        private Long corpusId;
        private Integer status;
        private LocalDateTime createdAt;
    }
}
