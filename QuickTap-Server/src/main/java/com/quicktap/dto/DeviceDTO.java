package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 设备DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceDTO {
    private Long id;
    private String deviceNo;
    private String name;
    private Integer merchantId;
    private String type;
    private String location;
    private String macAddress;
    private String ipAddress;
    private Integer status;
    private Long bindQrCodeId;
    private String systemCode;                // 系统编码
    private String url;                       // 设备URL

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateDeviceRequest {
        @NotBlank(message = "设备编号不能为空")
        private String deviceNo;

        @NotBlank(message = "设备名称不能为空")
        private String name;

        @NotNull(message = "设备类型不能为空")
        private String type;

        private String location;
        private String macAddress;
        private String ipAddress;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateDeviceRequest {
        private String name;
        private String location;
        private String macAddress;
        private String ipAddress;
        private Integer status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchCreateDeviceRequest {
        @NotNull(message = "设备列表不能为空")
        private java.util.List<CreateDeviceRequest> devices;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BindQrCodeRequest {
        @NotNull(message = "二维码ID不能为空")
        private Long qrCodeId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnbindQrCodeRequest {
        private Long qrCodeId;
    }
}
