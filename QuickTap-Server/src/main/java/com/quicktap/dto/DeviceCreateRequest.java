package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

/**
 * 创建设备请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCreateRequest {
    @NotBlank(message = "设备编号不能为空")
    private String deviceNo;

    @NotBlank(message = "设备名称不能为空")
    private String name;

    private Integer merchantId;

    @NotBlank(message = "设备类型不能为空")
    private String type;  // nfc/qrcode

    private String qrcode;

    private String systemCode;

    private String url;

    private Integer status;
}
