package com.quicktap.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商户更新请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantUpdateRequest {

    /**
     * 商户名称
     */
    private String name;

    /**
     * 商户 Logo
     */
    private String logo;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * WiFi 名称
     */
    private String wifiName;

    /**
     * WiFi 密码
     */
    private String wifiPassword;
}
