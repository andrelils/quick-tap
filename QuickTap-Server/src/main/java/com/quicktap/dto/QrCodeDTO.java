package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 二维码 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrCodeDTO {

    /**
     * 二维码 ID
     */
    private Long id;

    /**
     * 二维码编码
     */
    private String code;

    /**
     * 设备 ID
     */
    private Long deviceId;

    /**
     * 商户 ID
     */
    private Long merchantId;

    /**
     * 二维码包含的数据
     */
    private String qrData;

    /**
     * 二维码图片 URL
     */
    private String qrImageUrl;

    /**
     * 二维码类型
     */
    private String type;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

