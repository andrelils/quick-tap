package com.quicktap.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员更新请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminUpdateRequest {

    /**
     * 角色（super_admin, admin, merchant）
     */
    private String role;

    /**
     * 密码
     */
    private String password;

    /**
     * 商户 ID
     */
    private Integer merchantId;

    /**
     * 状态（1=启用, 0=禁用）
     */
    private Integer status;
}
