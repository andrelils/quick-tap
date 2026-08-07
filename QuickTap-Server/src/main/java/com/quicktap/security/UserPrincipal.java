package com.quicktap.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.quicktap.entity.Admin;
import com.quicktap.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * 用户主体（UserPrincipal）
 * 实现 Spring Security 的 UserDetails 接口
 * 用于在认证过程中携带用户信息
 */
@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails, Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String username;
    private String password;
    private String role;
    private Integer merchantId;
    private Integer status;

    /**
     * 从 Admin 实体转换为 UserPrincipal
     */
    public static UserPrincipal create(Admin admin) {
        return new UserPrincipal(
            admin.getId(),
            admin.getUsername(),
            admin.getPassword(),
            admin.getRole(),
            admin.getMerchantId(),
            admin.getStatus()
        );
    }

    /**
     * 从 C端 User 实体转换为 UserPrincipal
     * role 固定为 USER
     */
    public static UserPrincipal create(User user) {
        return new UserPrincipal(
            user.getId().intValue(),
            user.getUsername(),
            user.getPassword(),
            "USER",
            null,
            user.getStatus()
        );
    }

    /**
     * 返回用户的权限集合
     * 根据角色返回不同的权限；自定义角色按管理员(ADMIN)级权限处理，保证后台接口可用
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String r = role == null ? "" : role.trim().toUpperCase();
        switch (r) {
            case "SUPER_ADMIN":
            case "ADMIN":
            case "MERCHANT":
            case "USER":
                return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + r));
            default:
                // 自定义角色：按 ADMIN 级权限处理
                return Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
    }

    /**
     * 账号是否未过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账号是否未被锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 凭证是否未过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账号是否启用（status=1启用）
     */
    @Override
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
