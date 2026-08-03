package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 推广点击日志DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionClickLogDTO {
    private Long id;
    private Long userId;
    private Long platformId;
    private Long merchantId;
    private Long deviceId;
    private LocalDateTime createdAt;
}
