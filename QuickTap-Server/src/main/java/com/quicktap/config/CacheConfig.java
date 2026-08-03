package com.quicktap.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.time.Duration;

/**
 * 缓存配置
 * <p>
 * 两种模式由 REDIS_ENABLED / spring.redis.enabled 控制：
 * <ul>
 *   <li>Redis 模式（生产推荐）：REDIS_ENABLED=true
 *       使用 {@link RedisCacheManager}，集群多实例共享缓存，支持 TTL、持久化
 *   </li>
 *   <li>本地模式（本地开发默认）：REDIS_ENABLED=false 或未设置
 *       使用 {@link ConcurrentMapCacheManager}，JVM 内存缓存，无需外部 Redis
 *   </li>
 * </ul>
 * 无论哪种模式，业务侧的 {@code @Cacheable / @CacheEvict / @CachePut} 用法完全一致，
 * 无需改动任何 Service 代码。
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 启用 Redis 时：优先使用 Redis 作为共享缓存（集群一致性）
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new Jackson2JsonRedisSerializer<>(Object.class)))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
    }

    /**
     * 未启用 Redis 时：回退到 JVM 本地缓存，保证 @Cacheable 等注解仍能工作
     * <p>
     * 本地缓存默认与 Redis 保持一致的"不缓存 null"语义，
     * 单个缓存容量上限通过 softValues() 隐式由 GC 约束，避免本地 OOM。
     */
    @Bean
    @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "false", matchIfMissing = true)
    public CacheManager localCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        // null 值仍走 DB，与 Redis 模式对齐
        cacheManager.setAllowNullValues(false);
        // 缓存名称按需动态创建
        cacheManager.setStoreByValue(false);
        return cacheManager;
    }
}
