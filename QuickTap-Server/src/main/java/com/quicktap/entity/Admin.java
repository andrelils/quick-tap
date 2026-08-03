package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String username;           // 登录账号
    private String userCode;           // 用户编码(如AD001)
    private String password;           // 密码（bcrypt加密）
    private String nickname;           // 昵称
    private String email;              // 邮箱
    private String phone;              // 电话
    private String role;               // 角色：super_admin/admin/merchant
    private Integer merchantId;        // 商户ID（merchant角色关联的商户）
    private Integer status;            // 状态：1启用/0禁用
}
