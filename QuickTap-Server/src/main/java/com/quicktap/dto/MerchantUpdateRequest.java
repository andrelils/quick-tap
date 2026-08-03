package com.quicktap.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
     * 商户地址
     */
    private String address;

    /**
     * 营业时间
     */
    private String businessHours;

    /**
     * WiFi 名称
     */
    private String wifiName;

    /**
     * WiFi 密码
     */
    private String wifiPassword;

    /**
     * 老板微信
     */
    private String bossWechat;

    /**
     * 横幅图片(JSON数组)
     */
    private List<String> bannerImages;

    /**
     * 店铺图片(JSON数组)
     */
    private List<String> shopImages;

    /**
     * 商户描述
     */
    private String description;
}
