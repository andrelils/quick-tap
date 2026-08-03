package com.quicktap.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

/**
 * JWT Token 提供者 - 核心安全组件
 *
 * 职责：
 * - 生成 JWT Access Token (用于无状态身份验证)
 * - 验证 JWT Token 的有效性和完整性
 * - 从 Token 中安全地提取用户信息（用户名、用户ID、角色）
 * - 检测 Token 过期并拒绝过期请求
 *
 * 安全特性：
 * - 使用 HMAC SHA-512 (HS512) 算法进行数字签名
 * - 强制使用至少 64 字符长的加密密钥
 * - 验证 Token 签名完整性，防止篡改
 * - 检测多种 JWT 攻击（格式错误、签名无效等）
 * - 自动检测并拒绝过期 Token
 *
 * 配置说明：
 * - jwt.secret: JWT 加密密钥（通过环境变量 JWT_SECRET 设置，必需）
 * - jwt.expiration: Token 过期时间，单位毫秒（默认 604800000ms = 7天）
 *
 * 密钥生成方法：
 * {@code openssl rand -base64 64}
 *
 * 使用示例：
 * {@code
 * String token = jwtTokenProvider.generateToken("user123", 1, "ADMIN");
 * if (jwtTokenProvider.validateToken(token)) {
 *     String username = jwtTokenProvider.getUsernameFromToken(token);
 *     Integer userId = jwtTokenProvider.getUserIdFromToken(token);
 *     String role = jwtTokenProvider.getRoleFromToken(token);
 * }
 * }
 *
 * @author QuickTap Security Team
 * @version 2.0
 * @since 1.0
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${jwt.expiration:604800000}")
    private long jwtExpirationMs;

    private Key signingKey;
    /**
     * 复用的 JwtParser，避免高频请求中重复构建，提升性能
     */
    private JwtParser jwtParser;

    @PostConstruct
    private void init() {
        // 验证 JWT Secret 是否已设置
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException(
                "JWT Secret 未设置！必须通过 JWT_SECRET 环境变量提供。\n" +
                "要求: 长度至少 64 字符（512 位），用于 HS512 算法。\n" +
                "生成方法: openssl rand -base64 64"
            );
        }

        // 验证 Secret 长度（最小 64 字符）
        if (jwtSecret.length() < 64) {
            throw new IllegalStateException(
                "JWT Secret 长度过短！当前长度: " + jwtSecret.length() + " 字符，" +
                "最小要求: 64 字符（512 位）。\n" +
                "生成方法: openssl rand -base64 64"
            );
        }

        // 预计算签名密钥以提高性能
        try {
            signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            // 预构建 JwtParser，线程安全可复用
            jwtParser = Jwts.parserBuilder().setSigningKey(signingKey).build();
            log.info("JWT Token Provider 初始化成功");
        } catch (Exception e) {
            throw new IllegalStateException("无法初始化 JWT 签名密钥", e);
        }
    }

    /**
     * 生成 JWT Token
     * @param username 用户名
     * @param userId 用户ID
     * @param role 角色
     * @return JWT Token
     */
    public String generateToken(String username, Integer userId, String role) {
        if (username == null || userId == null || role == null) {
            throw new IllegalArgumentException("用户名、ID和角色不能为空");
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
            .setSubject(username)
            .claim("userId", userId)
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact();
    }

    /**
     * 生成 JWT Token（C端用户专用重载）
     * <p>C端 user 表无角色字段，统一使用固定角色 "USER"，内部调用三参数原方法。</p>
     * @param username 用户名（或 openid）
     * @param userId 用户ID
     * @return JWT Token
     */
    public String generateToken(String username, Integer userId) {
        return generateToken(username, userId, "USER");
    }

    /**
     * 从 token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token 不能为空");
        }
        return jwtParser.parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * 从 token 中获取用户ID
     */
    public Integer getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token 不能为空");
        }
        Object userIdObj = jwtParser.parseClaimsJws(token).getBody().get("userId");

        if (userIdObj == null) {
            throw new IllegalArgumentException("Token 中缺少 userId");
        }
        return ((Number) userIdObj).intValue();
    }

    /**
     * 从 token 中获取角色
     */
    public String getRoleFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token 不能为空");
        }
        Object roleObj = jwtParser.parseClaimsJws(token).getBody().get("role");

        if (roleObj == null) {
            throw new IllegalArgumentException("Token 中缺少 role");
        }
        return (String) roleObj;
    }

    /**
     * 验证 JWT Token 是否有效
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            log.warn("Token 为空");
            return false;
        }

        try {
            jwtParser.parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            log.error("无效的 JWT 签名: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("无效的 JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token 已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("不支持的 JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims 字符串为空: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 从 token 中获取过期时间
     * 用于黑名单 TTL 对齐 Token 自身过期时间，避免黑名单无限膨胀
     *
     * @param token JWT Token
     * @return 过期时间 Date
     */
    public Date getExpiryDate(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token 不能为空");
        }
        return jwtParser.parseClaimsJws(token).getBody().getExpiration();
    }

    /**
     * 检查 token 是否过期
     */
    public boolean isTokenExpired(String token) {
        if (token == null || token.isEmpty()) {
            return true;
        }

        try {
            Date expiration = jwtParser.parseClaimsJws(token).getBody().getExpiration();
            return expiration == null || expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.error("检查 token 过期时出错: {}", e.getMessage());
            return true;
        }
    }
}
