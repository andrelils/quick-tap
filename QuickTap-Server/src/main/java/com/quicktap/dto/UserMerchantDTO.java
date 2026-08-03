package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户-商户关系DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMerchantDTO {
    private Long id;
    private Long userId;
    private Long merchantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
