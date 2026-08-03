package com.quicktap.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull; /**
 * 批量生成二维码请求
 */
@Data
public class BatchGenerateQrCodeRequest {

    @NotNull(message = "设备 ID 列表不能为空")
    private java.util.List<Long> deviceIds;

    private String type = "STANDARD";
}
