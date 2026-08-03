package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 商户DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantDTO {
    private Long id;
    private String name;
    private String contactName;
    private String contactPhone;
    private String address;
    private String province;
    private String city;
    private String district;
    private Integer status;
    private String bannerImages;              // 轮播图JSON
    private String bossWechat;                // 老板微信
    private String businessHours;             // 营业时间
    private String shopImages;                // 店铺图片JSON
    private String referrerCode;              // 推荐人编码
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateMerchantRequest {
        @NotBlank(message = "商户名称不能为空")
        private String name;

        @NotBlank(message = "联系人不能为空")
        private String contactName;

        @NotBlank(message = "联系电话不能为空")
        private String contactPhone;

        private String address;
        private String province;
        private String city;
        private String district;
        private String bannerImages;
        private String bossWechat;
        private String businessHours;
        private String shopImages;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateMerchantRequest {
        private String name;
        private String contactName;
        private String contactPhone;
        private String address;
        private String province;
        private String city;
        private String district;
        private Integer status;
        private String bannerImages;
        private String bossWechat;
        private String businessHours;
        private String shopImages;
    }
}
