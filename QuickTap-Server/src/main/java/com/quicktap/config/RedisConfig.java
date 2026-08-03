package com.quicktap.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/**
 * Redis 配置
 * <p>
 * 通过配置项 spring.redis.enabled 或 REDIS_ENABLED 控制是否启用：
 *   - 生产环境：REDIS_ENABLED=true，启用 Redis 缓存与 Token 黑名单
 *   - 本地开发：REDIS_ENABLED=false（默认），跳过 Redis 自动装配，
 *              缓存由 Caffeine/ConcurrentMap 本地缓存兜底，Token 黑名单降级为日志模式
 * <p>
 * 当 Redis 被启用且连接可用时：
 *   - RedisTemplate<String, Object> 用于缓存业务对象（@Cacheable 等）
 *   - StringRedisTemplate       用于 Token 黑名单等纯字符串操作
 */
@Configuration
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = false)
public class RedisConfig {

    /**
     * 通用 RedisTemplate：Value 使用 Jackson 序列化，适合业务对象缓存
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
            new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType("com.quicktap.")
                .allowIfBaseType("java.util.")
                .allowIfBaseType("java.lang.")
                .build(),
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();

        return template;
    }

    /**
     * StringRedisTemplate：Key/Value 均为字符串序列化
     * 用于 TokenBlacklistService、分布式锁等纯字符串场景
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();
        return template;
    }
}
