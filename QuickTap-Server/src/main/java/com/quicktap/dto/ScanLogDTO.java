package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 扫码日志DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanLogDTO {
    private Long id;
    private Long userId;
    private Long deviceId;
    private Long merchantId;
    private LocalDateTime createdAt;
}
