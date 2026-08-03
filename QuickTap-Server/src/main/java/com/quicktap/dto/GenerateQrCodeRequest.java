package com.quicktap.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; /**
 * 生成二维码请求
 */
@Data
public class GenerateQrCodeRequest {

    @NotNull(message = "设备 ID 不能为空")
    private Long deviceId;

    @NotBlank(message = "二维码数据不能为空")
    private String qrData;

    private String type = "STANDARD";
}
