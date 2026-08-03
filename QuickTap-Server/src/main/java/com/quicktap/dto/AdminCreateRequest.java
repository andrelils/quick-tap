package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员创建请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateRequest {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 角色（super_admin, admin, merchant）
     */
    private String role;

    /**
     * 商户 ID
     */
    private Integer merchantId;

    /**
     * 状态（1=启用, 0=禁用）
     */
    private Integer status;
}
