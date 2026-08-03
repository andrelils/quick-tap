package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * WeChat 小程序登录请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatLoginRequest {

    /**
     * 微信授权码（来自 wx.login() 获取）
     * 用于交换 session_key 和 openid
     */
    @NotBlank(message = "授权码不能为空")
    private String code;

    /**
     * 可选：加密用户数据
     * 如果需要获取用户详细信息可传入
     */
    private String encryptedData;

    /**
     * 可选：加密算法 IV
     */
    private String iv;
}
