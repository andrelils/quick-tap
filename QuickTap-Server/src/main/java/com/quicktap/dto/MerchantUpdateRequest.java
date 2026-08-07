package com.quicktap.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商户更新请求 DTO
 * <p>
 * 支持更新商户基本信息以及 C 端展示配置（轮播图、店铺图、营业时间等）。
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

    // ========== 管理员可修改字段 ==========

    /**
     * 状态：1启用/0禁用（仅管理员可修改）
     */
    private Integer status;

    /**
     * 套餐 ID（仅管理员可修改）
     */
    private Integer planId;

    /**
     * 存储限制 MB（仅管理员可修改）
     */
    private Long storageLimit;
}
