package com.quicktap.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull; /**
 * 绑定二维码到商户请求
 */
@Data
public class BindQrCodeRequest {

    @NotNull(message = "设备 ID 不能为空")
    private Long deviceId;

    @NotNull(message = "商户 ID 不能为空")
    private Long merchantId;
}
