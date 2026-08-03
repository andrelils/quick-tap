package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户-设备关系DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceDTO {
    private Long id;
    private Long userId;
    private Long deviceId;
    private LocalDateTime createdAt;
}
