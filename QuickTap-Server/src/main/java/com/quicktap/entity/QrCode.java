package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 二维码表
 * 管理所有生成的二维码
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrCode extends BaseEntity {

    /**
     * 二维码编码（唯一）
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
     * 二维码类型（NFC 或 STANDARD）
     */
    private String type;

    /**
     * 状态（ACTIVE、INACTIVE、EXPIRED）
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
