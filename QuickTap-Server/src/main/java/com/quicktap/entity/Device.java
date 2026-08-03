package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String deviceNo;               // 设备编号（唯一）
    private String name;                   // 设备名称
    private Integer merchantId;            // 关联商户ID
    private String type;                   // 设备类型: nfc/qrcode
    private String systemCode;             // 系统编码
    private String url;                    // 设备URL
    private String qrcode;                 // 二维码数据
    private String location;               // 设备位置
    private String macAddress;             // MAC地址
    private String ipAddress;              // IP地址
    private Integer status;                // 状态：1启用/0停用/2故障
    private Long bindQrCodeId;             // 绑定的二维码ID
}
