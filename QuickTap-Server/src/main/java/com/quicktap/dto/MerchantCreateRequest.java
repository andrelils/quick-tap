package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商户创建请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantCreateRequest {

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

    // ========== C 端展示配置字段 ==========

    /**
     * 商户地址（C 端展示）
     */
    private String address;

    /**
     * 横幅图片（JSON 数组字符串，C 端首页轮播图）
     */
    private String bannerImages;

    /**
     * 店铺图片（JSON 数组字符串，C 端店铺展示）
     */
    private String shopImages;

    /**
     * 老板微信（C 端联系方式展示）
     */
    private String bossWechat;

    /**
     * 营业时间（C 端展示）
     */
    private String businessHours;

    /**
     * 推荐人代码
     */
    private String referrerCode;

    /**
     * 商家简介（C 端展示）
     */
    private String description;

    /**
     * 状态：1启用/0禁用（默认 1）
     */
    private Integer status;
}
