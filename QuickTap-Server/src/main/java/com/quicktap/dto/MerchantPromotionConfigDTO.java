package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 商户推广配置DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantPromotionConfigDTO {
    private Long id;
    private Long merchantId;
    private String type;                   // "platform" or "coupon"
    private Long platformId;
    private Long couponId;
    private String params;
    private String customName;
    private String customIcon;
    private Integer sort;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateMerchantConfigRequest {
        @NotNull(message = "类型不能为空")
        private String type;                // "platform" or "coupon"

        private Long platformId;
        private Long couponId;
        private String params;
        private String customName;
        private String customIcon;
        private Integer sort;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateMerchantConfigRequest {
        private String params;
        private String customName;
        private String customIcon;
        private Integer sort;
        private Integer status;
    }
}
