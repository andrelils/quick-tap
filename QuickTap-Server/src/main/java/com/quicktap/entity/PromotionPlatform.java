package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推广平台表（超管维护 - 全局配置）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionPlatform extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String code;               // 平台代码：douyin/xiaohongshu等
    private String name;               // 平台名称
    private String icon;               // 平台图标URL
    private String color;              // 平台颜色(十六进制)
    private String description;        // 平台描述
    private String jumpMode;           // 跳转方式：scheme/webview/miniprogram/copy
    private String schemeTemplate;     // URL scheme模板
    private String webUrlTemplate;     // H5链接模板
    private String miniprogramAppid;   // 小程序AppID
    private String miniprogramPathTemplate; // 小程序路径模板
    private String requiredParams;     // 必填参数（JSON格式）
    private String optionalParams;     // 可选参数（JSON格式）
    private Integer sort;              // 排序号
    private Integer status;            // 状态：1启用/0停用
}
