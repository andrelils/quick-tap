package com.quicktap.security;

import com.quicktap.entity.Admin;
import com.quicktap.mapper.AdminMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 自定义用户详情服务 - Spring Security 的用户信息提供者
 *
 * 职责：
 * - 实现 Spring Security 的 UserDetailsService 接口
 * - 从数据库中加载用户信息
 * - 构建包含权限和角色的用户主体对象 (UserPrincipal)
 * - 支持按用户名和用户ID两种方式加载用户
 *
 * 核心方法：
 * - loadUserByUsername(String) - 按用户名加载用户信息（Spring Security 标准方法）
 * - loadUserById(Integer) - 按用户ID加载用户信息（自定义扩展方法）
 *
 * 用户加载流程：
 * 1. 接收用户名或用户ID
 * 2. 从 Admin 表中查询用户信息
 * 3. 如果用户不存在，抛出 UsernameNotFoundException
 * 4. 使用 UserPrincipal.create() 将 Admin 对象转换为 UserDetails
 * 5. 返回包含权限和角色信息的 UserPrincipal
 *
 * 使用场景：
 * {@code
 * // 场景1: Spring Security 自动调用（在认证时）
 * // JwtAuthenticationFilter 中会调用此方法
 * UserPrincipal user = customUserDetailsService.loadUserByUsername("admin");
 *
 * // 场景2: 手动调用（需要快速查询用户信息）
 * UserPrincipal user = customUserDetailsService.loadUserById(1);
 * }
 *
 * 数据库查询：
 * - 查询表: admin 表
 * - 查询字段: id, username, password, email, phone, role, status, created_at, updated_at
 * - 索引: 用户名字段应该有唯一索引
 *
 * 错误处理：
 * - 用户不存在: 抛出 UsernameNotFoundException（Spring Security 标准异常）
 * - 数据库异常: 由 Mapper 层处理，不在此捕获
 *
 * 安全说明：
 * - 密码由 UserPrincipal.create() 方法处理，不在此解密
 * - 用户状态检查由 SecurityConfig 中的认证提供者处理
 * - 用户信息在 UserPrincipal 中受到保护
 *
 * @author QuickTap Security Team
 * @version 1.0
 * @since 1.0
 * @see UserPrincipal
 * @see AdminMapper
 * @see org.springframework.security.core.userdetails.UserDetailsService
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AdminMapper adminMapper;

    /**
     * 根据用户名加载用户详情
     * @param username 用户名
     * @return UserPrincipal（实现 UserDetails 接口）
     * @throws UsernameNotFoundException 用户不存在
     */
    @Override
    public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminMapper.selectByUsername(username);

        if (admin == null) {
            log.warn("用户名不存在: {}", username);
            throw new UsernameNotFoundException("用户名不存在: " + username);
        }

        log.debug("用户信息已加载: {}", username);
        return UserPrincipal.create(admin);
    }

    /**
     * 根据用户ID加载用户详情（用于其他场景）
     */
    public UserPrincipal loadUserById(Integer userId) {
        Admin admin = adminMapper.selectById(userId);

        if (admin == null) {
            log.warn("用户不存在: {}", userId);
            throw new UsernameNotFoundException("用户不存在: " + userId);
        }

        return UserPrincipal.create(admin);
    }
}
