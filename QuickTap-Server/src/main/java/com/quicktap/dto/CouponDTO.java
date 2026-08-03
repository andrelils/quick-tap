package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponDTO {
    private Long id;
    private Long merchantId;
    private String title;
    private String type;                  // cash/discount
    private BigDecimal amount;
    private BigDecimal minAmount;
    private Integer totalCount;
    private Integer remainCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateCouponRequest {
        @NotBlank(message = "卡券标题不能为空")
        private String title;

        @NotNull(message = "卡券类型不能为空")
        private String type;

        @NotNull(message = "金额不能为空")
        private BigDecimal amount;

        private BigDecimal minAmount;

        @NotNull(message = "总数不能为空")
        private Integer totalCount;

        @NotNull(message = "有效期开始不能为空")
        private LocalDateTime startTime;

        @NotNull(message = "有效期结束不能为空")
        private LocalDateTime endTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateCouponRequest {
        private String title;
        private BigDecimal amount;
        private BigDecimal minAmount;
        private Integer totalCount;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClaimCouponRequest {
        @NotNull(message = "卡券ID不能为空")
        private Long couponId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UseCouponRequest {
        @NotNull(message = "用户卡券ID不能为空")
        private Long userCouponId;
    }
}
