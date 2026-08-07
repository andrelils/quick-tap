package com.quicktap.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 生产环境配置安全校验器
 *
 * 在 prod profile 启动时强制校验关键密钥/密码：
 * 1. 必须通过环境变量注入，不能为空
 * 2. 不能使用 application-dev.yml 中的默认开发值
 * 3. 长度/格式需满足最低安全要求
 *
 * 任一校验失败直接抛异常，应用启动失败，避免带着弱密钥上线。
 */
@Slf4j
@Component
@Profile("prod")
public class EnvironmentValidator {

    /** dev 环境的默认 JWT 密钥（用于拦截误用） */
    private static final String DEV_JWT_SECRET = "QuickTapDevJwtSecretKeyForDevelopmentOnlyDoNotUseInProduction2026";
    /** dev 环境的默认加密密钥 */
    private static final String DEV_ENCRYPTION_KEY = "cahZqdShHM0SSojE5qRoOU8RQRvfyrNGcXWNq3wGZn0=";

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${encryption.key:}")
    private String encryptionKey;

    @Value("${REDIS_ENABLED:false}")
    private boolean redisEnabled;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @PostConstruct
    public void validate() {
        log.info("[生产环境配置校验] 开始检查关键密钥与密码...");
        boolean hasError = false;

        // 1. 数据库密码
        if (isBlank(dbPassword)) {
            log.error("[配置校验失败] DB_PASSWORD 未配置，生产环境必须通过环境变量设置数据库密码");
            hasError = true;
        }

        // 2. JWT 密钥
        if (isBlank(jwtSecret)) {
            log.error("[配置校验失败] JWT_SECRET 未配置，生产环境必须通过环境变量设置 JWT 密钥");
            hasError = true;
        } else if (DEV_JWT_SECRET.equals(jwtSecret)) {
            log.error("[配置校验失败] JWT_SECRET 使用了开发环境默认值，生产环境禁止使用");
            hasError = true;
        } else if (jwtSecret.length() < 32) {
            log.error("[配置校验失败] JWT_SECRET 长度不足 32 字符（当前 {}），HS512 建议至少 64 字符", jwtSecret.length());
            hasError = true;
        }

        // 3. 加密密钥
        if (isBlank(encryptionKey)) {
            log.error("[配置校验失败] ENCRYPTION_KEY 未配置，生产环境必须通过环境变量设置加密密钥");
            hasError = true;
        } else if (DEV_ENCRYPTION_KEY.equals(encryptionKey)) {
            log.error("[配置校验失败] ENCRYPTION_KEY 使用了开发环境默认值，生产环境禁止使用");
            hasError = true;
        }

        // 4. Redis 密码（生产环境 Redis 默认开启，密码必须配置）
        if (redisEnabled && isBlank(redisPassword)) {
            log.error("[配置校验失败] REDIS_PASSWORD 未配置，生产环境 Redis 必须设置密码");
            hasError = true;
        }

        if (hasError) {
            throw new IllegalStateException(
                    "生产环境配置校验失败，请检查以上错误并配置正确的环境变量。应用拒绝启动以避免安全风险。");
        }

        log.info("[生产环境配置校验] 全部通过");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
