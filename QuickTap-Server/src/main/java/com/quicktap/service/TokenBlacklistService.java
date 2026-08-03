package com.quicktap.service;

import com.quicktap.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务
 * 用于管理已登出或已过期的JWT Token
 * 支持Redis存储和本地缓存两种方式
 *
 * 提供两类失效机制：
 * 1. 单 Token 黑名单（登出/刷新/过期）：addToBlacklist / isBlacklisted
 * 2. 用户级 Token 撤销（权限变更/禁用）：revokeUser / isUserRevoked
 *    用户级撤销会让该用户所有已签发的 Token 立即失效，强制重新登录
 */
@Slf4j
@Service
public class TokenBlacklistService {

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String USER_REVOKE_PREFIX = "jwt:user_revoke:";
    private static final long BLACKLIST_EXPIRY_TIME = 7 * 24 * 60 * 60; // 7 days in seconds

    /**
     * 将Token加入黑名单
     * @param token JWT token
     */
    public void addToBlacklist(String token) {
        try {
            // 从token中获取过期时间
            long expiresAt = jwtTokenProvider.getExpiryDate(token).getTime();
            long ttl = (expiresAt - System.currentTimeMillis()) / 1000;

            // 如果token已过期，设置一个默认TTL
            if (ttl <= 0) {
                ttl = BLACKLIST_EXPIRY_TIME;
            }

            String blacklistKey = BLACKLIST_PREFIX + token;

            if (redisTemplate != null) {
                // 使用Redis存储
                redisTemplate.opsForValue().set(blacklistKey, "blacklisted", ttl, TimeUnit.SECONDS);
                log.info("[TokenBlacklist] Token已加入Redis黑名单, TTL: {}s", ttl);
            } else {
                // Redis不可用时，只记录日志
                log.warn("[TokenBlacklist] Redis不可用, 仅记录日志: token={}", maskToken(token));
            }
        } catch (Exception e) {
            log.error("[TokenBlacklist] 添加token到黑名单失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查Token是否在黑名单中
     * @param token JWT token
     * @return true if blacklisted, false otherwise
     */
    public boolean isBlacklisted(String token) {
        try {
            String blacklistKey = BLACKLIST_PREFIX + token;

            if (redisTemplate != null) {
                Boolean exists = redisTemplate.hasKey(blacklistKey);
                return exists != null && exists;
            }
            // Redis不可用时，返回false允许请求通过
            return false;
        } catch (Exception e) {
            log.error("[TokenBlacklist] 检查token黑名单状态失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 撤销某用户的所有 Token（用于权限变更/账号禁用场景）
     * 设置一个用户级撤销标记，TTL 与 Token 最大有效期对齐
     *
     * @param userId 用户ID
     */
    public void revokeUser(Integer userId) {
        if (userId == null) {
            return;
        }
        try {
            if (redisTemplate != null) {
                String key = USER_REVOKE_PREFIX + userId;
                redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()),
                        BLACKLIST_EXPIRY_TIME, TimeUnit.SECONDS);
                log.info("[TokenBlacklist] 用户 {} 的所有Token已撤销", userId);
            } else {
                log.warn("[TokenBlacklist] Redis不可用, 无法撤销用户 {} 的Token", userId);
            }
        } catch (Exception e) {
            log.error("[TokenBlacklist] 撤销用户Token失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 检查某用户是否已被撤销（权限变更后强制重新登录）
     * @param userId 用户ID
     * @return true 表示该用户的所有 Token 都应被视为无效
     */
    public boolean isUserRevoked(Integer userId) {
        if (userId == null) {
            return false;
        }
        try {
            if (redisTemplate != null) {
                Boolean exists = redisTemplate.hasKey(USER_REVOKE_PREFIX + userId);
                return exists != null && exists;
            }
            return false;
        } catch (Exception e) {
            log.error("[TokenBlacklist] 检查用户撤销状态失败: userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 从黑名单中移除Token（不常用）
     * @param token JWT token
     */
    public void removeFromBlacklist(String token) {
        try {
            String blacklistKey = BLACKLIST_PREFIX + token;
            if (redisTemplate != null) {
                redisTemplate.delete(blacklistKey);
                log.info("[TokenBlacklist] Token已从黑名单移除");
            }
        } catch (Exception e) {
            log.error("[TokenBlacklist] 从黑名单移除token失败: {}", e.getMessage());
        }
    }

    /**
     * 清空所有黑名单（用于系统维护）
     * 使用 SCAN 命令替代 KEYS，避免在大数据量下阻塞 Redis
     */
    public void clearBlacklist() {
        try {
            if (redisTemplate != null) {
                ScanOptions options = ScanOptions.scanOptions()
                        .match(BLACKLIST_PREFIX + "*")
                        .count(100)
                        .build();
                int deleted = 0;
                try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                        .getConnection().scan(options)) {
                    while (cursor.hasNext()) {
                        redisTemplate.delete(new String(cursor.next()));
                        deleted++;
                    }
                }
                log.info("[TokenBlacklist] 黑名单已清空, 共删除 {} 条", deleted);
            }
        } catch (Exception e) {
            log.error("[TokenBlacklist] 清空黑名单失败: {}", e.getMessage());
        }
    }

    /**
     * 掩盖token的敏感信息用于日志记录
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 20) {
            return "***";
        }
        return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
    }
}
