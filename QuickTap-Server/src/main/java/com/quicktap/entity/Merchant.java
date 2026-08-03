package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商户表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Merchant extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String name;               // 商户名称
    private String logo;               // 商户logo
    private String contactName;        // 联系人名称
    private String contactPhone;       // 联系人电话
    private String contactEmail;       // 联系人Email
    private String address;            // 商户地址
    private String wifiName;           // 门店WiFi名称
    private String wifiPassword;       // WiFi密码
    private Integer auditStatus;       // 审核状态：0待审核/1通过/2拒绝
    private Integer status;            // 状态：1启用/0停用
    private Integer planId;            // 套餐ID
    private Long storageUsed;          // 已使用存储(MB)
    private Long storageLimit;         // 存储限制(MB)
    private String bannerImages;       // 横幅图片(JSON数组)
    private String bossWechat;         // 老板微信
    private String businessHours;      // 营业时间
    private String shopImages;         // 店铺图片(JSON数组)
    private String referrerCode;       // 推荐人代码
    private String description;        // 商户描述
}
