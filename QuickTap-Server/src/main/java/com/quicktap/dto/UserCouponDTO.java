package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户卡券DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponDTO {
    private Long id;
    private Long userId;
    private Long couponId;
    private Long merchantId;
    private Integer status; // 1=UNUSED, 2=USED, 3=EXPIRED
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
