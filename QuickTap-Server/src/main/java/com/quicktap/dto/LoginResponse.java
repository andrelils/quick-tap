package com.quicktap.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    /**
     * JWT Token
     */
    private String token;

    /**
     * Token 过期时间（毫秒）
     */
    private Long expiresIn;

    /**
     * 用户 ID
     */
    private Integer userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户角色
     */
    private String role;

    /**
     * 商户 ID（如果有）
     */
    private Integer merchantId;
}
