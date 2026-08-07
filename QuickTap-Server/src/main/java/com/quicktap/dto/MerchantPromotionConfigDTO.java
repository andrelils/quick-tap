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

    // 推广平台详情字段（用于详情页显示和参数配置）
    private String platformName;
    private String platformCode;
    private String platformDescription;
    private String platformColor;
    private String jumpMode;
    private String schemeTemplate;
    private String webUrlTemplate;
    private String miniprogramAppid;
    private String miniprogramPathTemplate;
    private String requiredParams;          // JSON string
    private String optionalParams;          // JSON string

    // 优惠券详情字段
    private String couponName;
    private String couponType;
    private String couponValue;
    private String couponThreshold;
    private Integer couponStatus;
    private Integer couponTotalCount;
    private Integer couponRemainCount;
    private String couponValidStart;
    private String couponValidEnd;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateMerchantConfigRequest {
        @NotNull(message = "类型不能为空")
        private String type;                // "platform" or "coupon"

        private Long merchantId;            // 管理员/超管代指定商户操作时传
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
        private Long merchantId;            // 管理员/超管代指定商户操作时传
        private String params;
        private String customName;
        private String customIcon;
        private Integer sort;
        private Integer status;
    }
}
