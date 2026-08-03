package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 推广平台DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionPlatformDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String iconUrl;
    private Integer sortOrder;
    private Boolean enabled;
    private String requiredParams;
    private String optionalParams;
    private String color;                     // 平台颜色
    private String jumpMode;                  // 跳转方式: scheme, webview, miniprogram, copy
    private String schemeTemplate;            // Scheme模板
    private String webUrlTemplate;            // 网页URL模板
    private String miniprogramAppid;          // 小程序AppID
    private String miniprogramPathTemplate;   // 小程序路径模板

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreatePromotionPlatformRequest {
        @NotBlank(message = "平台代码不能为空")
        private String code;

        @NotBlank(message = "平台名称不能为空")
        private String name;

        private String description;
        private String iconUrl;
        private Integer sortOrder;
        private String requiredParams;
        private String optionalParams;
        private String color;
        private String jumpMode;
        private String schemeTemplate;
        private String webUrlTemplate;
        private String miniprogramAppid;
        private String miniprogramPathTemplate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdatePromotionPlatformRequest {
        private String name;
        private String description;
        private String iconUrl;
        private Integer sortOrder;
        private Boolean enabled;
        private String requiredParams;
        private String optionalParams;
        private String color;
        private String jumpMode;
        private String schemeTemplate;
        private String webUrlTemplate;
        private String miniprogramAppid;
        private String miniprogramPathTemplate;
    }
}
