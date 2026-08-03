package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * C端用户表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String username;           // 用户名（用于登录）
    private String password;           // 密码（BCrypt加密）
    private String openid;             // 微信openid
    private String unionid;            // 微信unionid
    private String nickname;           // 昵称
    private String avatar;             // 头像URL
    private String phone;              // 绑定手机号
    private Integer status;            // 状态：1启用/0禁用
}
