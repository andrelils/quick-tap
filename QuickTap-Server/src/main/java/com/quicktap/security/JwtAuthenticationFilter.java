package com.quicktap.security;

import com.quicktap.service.TokenBlacklistService;
import com.quicktap.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 身份验证过滤器 - Spring Security 请求过滤链中的关键组件
 *
 * 职责：
 * - 拦截所有 HTTP 请求，从请求头中提取 JWT Token
 * - 验证 Token 的完整性和有效性
 * - 加载相关的用户信息并构建认证对象
 * - 将认证信息存储到 Spring Security 上下文中
 *
 * 工作流程 (9个步骤)：
 * 1. 从 HTTP Authorization 请求头中提取 Bearer Token
 * 2. 如果没有 Token，继续过滤链（允许匿名访问）
 * 3. 验证 Token 的签名和完整性（使用 JwtTokenProvider）
 * 4. 从 Token 中安全地提取用户名
 * 5. 加载用户的详细信息和权限列表
 * 6. 验证用户对象和权限不为空
 * 7. 创建 Spring Security 的认证令牌
 * 8. 从请求中提取并设置认证详情（如远程IP、会话ID等）
 * 9. 将认证信息存储到 Spring Security 的线程本地上下文
 *
 * Token 格式：
 * {@code Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...}
 *
 * 安全特性：
 * - 一次性过滤器 (OncePerRequestFilter) - 确保每个请求只处理一次
 * - 全面的异常处理 - 任何错误都不会中断过滤链
 * - 详细的日志记录 - 便于安全审计和调试
 * - 防御性编程 - Null 检查和边界检查
 * - 线程安全 - 使用 Spring Security 的 ThreadLocal 机制
 *
 * Token 验证失败的情况（都会继续过滤链）：
 * - Authorization 头不存在
 * - Authorization 头格式不正确（不以 "Bearer " 开头）
 * - Token 为空或格式错误
 * - Token 签名无效或被篡改
 * - Token 已过期
 * - Token 中缺少用户信息
 * - 用户不存在于数据库
 * - 用户没有任何权限
 *
 * 最后的权限检查：
 * - 认证通过后，细粒度的权限检查由 @PreAuthorize 注解处理
 * - 未认证的请求会被标记为匿名用户 (ROLE_ANONYMOUS)
 *
 * 使用示例：
 * {@code
 * // HTTP 请求示例
 * GET /api/user/profile HTTP/1.1
 * Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMTIzIn0...
 * }
 *
 * @author QuickTap Security Team
 * @version 1.0
 * @since 1.0
 * @see JwtTokenProvider
 * @see CustomUserDetailsService
 * @see UserPrincipal
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private IpUtil ipUtil;

    // 授权请求头常量
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    /**
     * 从请求头中获取 JWT token
     * Authorization: Bearer <token>
     *
     * @param request HTTP 请求
     * @return JWT token，若不存在则返回 null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        if (request == null) {
            log.warn("HttpServletRequest 为 null");
            return null;
        }

        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(bearerToken)) {
            // 这是正常情况（匿名用户），不记录为警告
            return null;
        }

        if (!bearerToken.startsWith(BEARER_PREFIX)) {
            log.debug("Authorization 头格式不正确，未以 'Bearer ' 开头");
            return null;
        }

        String token = bearerToken.substring(BEARER_PREFIX_LENGTH).trim();
        if (token.isEmpty()) {
            log.warn("Authorization 头中的 token 为空");
            return null;
        }

        return token;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (request == null || response == null) {
            log.error("请求或响应对象为 null");
            filterChain.doFilter(request, response);
            return;
        }

        // 认证逻辑：成功则设置 SecurityContext，失败则保持匿名
        // doFilter 只在最末尾调用一次，避免重复执行下游过滤器
        String clientIp = IpUtil.getClientIp(request);

        try {
            String jwt = getJwtFromRequest(request);

            if (!StringUtils.hasText(jwt)) {
                // 无 token，匿名请求，直接放行
                return;
            }

            // 验证 token 签名与有效期
            if (!tokenProvider.validateToken(jwt)) {
                log.warn("JWT token 验证失败，来源IP: {}", clientIp);
                return;
            }

            // 检查 token 是否在黑名单中（登出/刷新后的旧 token）
            if (tokenBlacklistService.isBlacklisted(jwt)) {
                log.warn("JWT token 已在黑名单中，来源IP: {}", clientIp);
                return;
            }

            // 提取用户名
            String username;
            try {
                username = tokenProvider.getUsernameFromToken(jwt);
            } catch (IllegalArgumentException e) {
                log.warn("从 JWT token 中提取用户名失败: {}，来源IP: {}", e.getMessage(), clientIp);
                return;
            }

            if (!StringUtils.hasText(username)) {
                log.warn("JWT token 中的用户名为空，来源IP: {}", clientIp);
                return;
            }

            // 提取 userId 并检查用户是否被撤销（权限变更/禁用场景）
            Integer userId = null;
            try {
                userId = tokenProvider.getUserIdFromToken(jwt);
            } catch (Exception e) {
                log.debug("从 JWT token 中提取 userId 失败: {}，来源IP: {}", e.getMessage(), clientIp);
            }
            if (userId != null && tokenBlacklistService.isUserRevoked(userId)) {
                log.warn("用户 {} 的 Token 已被撤销（权限变更），来源IP: {}", userId, clientIp);
                return;
            }

            // 加载用户详情
            UserPrincipal userPrincipal;
            try {
                userPrincipal = customUserDetailsService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                log.warn("用户不存在: {}，来源IP: {}", username, clientIp);
                return;
            } catch (Exception e) {
                log.error("加载用户信息失败: {} - {}，来源IP: {}", username, e.getMessage(), clientIp);
                return;
            }

            if (userPrincipal == null) {
                log.error("用户主体对象为 null: {}，来源IP: {}", username, clientIp);
                return;
            }

            if (userPrincipal.getAuthorities() == null || userPrincipal.getAuthorities().isEmpty()) {
                log.warn("用户没有任何权限: {}，来源IP: {}", username, clientIp);
                // 继续处理，权限检查由 @PreAuthorize 处理
            }

            // 构建认证对象并写入 SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userPrincipal,
                            null,
                            userPrincipal.getAuthorities()
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("用户 '{}' JWT 认证成功，来源IP: {}", username, clientIp);

        } catch (Exception ex) {
            // 捕获所有未预期的异常，防止过滤器链中断
            log.error("JWT 认证过程中发生未知错误，来源IP: {}", clientIp, ex);
            SecurityContextHolder.clearContext();
        } finally {
            // 无论认证成功还是失败，都继续过滤链；doFilter 只调用一次
            filterChain.doFilter(request, response);
        }
    }
}
