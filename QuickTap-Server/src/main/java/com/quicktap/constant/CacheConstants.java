package com.quicktap.constant;

/**
 * 缓存常量类
 * 定义 Redis 缓存的 key 前缀和过期时间
 */
public class CacheConstants {

    /**
     * Redis key 分隔符
     */
    public static final String REDIS_SEPARATOR = ":";

    /**
     * ====== Admin 缓存 ======
     */
    public static final String ADMIN_CACHE_PREFIX = "admin" + REDIS_SEPARATOR;
    public static final String ADMIN_ID_CACHE_KEY = ADMIN_CACHE_PREFIX + "id" + REDIS_SEPARATOR;
    public static final String ADMIN_USERNAME_CACHE_KEY = ADMIN_CACHE_PREFIX + "username" + REDIS_SEPARATOR;

    /**
     * ====== User 缓存 ======
     */
    public static final String USER_CACHE_PREFIX = "user" + REDIS_SEPARATOR;
    public static final String USER_ID_CACHE_KEY = USER_CACHE_PREFIX + "id" + REDIS_SEPARATOR;
    public static final String USER_OPENID_CACHE_KEY = USER_CACHE_PREFIX + "openid" + REDIS_SEPARATOR;

    /**
     * ====== Merchant 缓存 ======
     */
    public static final String MERCHANT_CACHE_PREFIX = "merchant" + REDIS_SEPARATOR;
    public static final String MERCHANT_ID_CACHE_KEY = MERCHANT_CACHE_PREFIX + "id" + REDIS_SEPARATOR;

    /**
     * ====== Device 缓存 ======
     */
    public static final String DEVICE_CACHE_PREFIX = "device" + REDIS_SEPARATOR;
    public static final String DEVICE_ID_CACHE_KEY = DEVICE_CACHE_PREFIX + "id" + REDIS_SEPARATOR;
    public static final String DEVICE_NO_CACHE_KEY = DEVICE_CACHE_PREFIX + "no" + REDIS_SEPARATOR;

    /**
     * ====== Promotion Platform 缓存 ======
     */
    public static final String PLATFORM_CACHE_PREFIX = "platform" + REDIS_SEPARATOR;
    public static final String PLATFORM_ID_CACHE_KEY = PLATFORM_CACHE_PREFIX + "id" + REDIS_SEPARATOR;
    public static final String PLATFORM_CODE_CACHE_KEY = PLATFORM_CACHE_PREFIX + "code" + REDIS_SEPARATOR;

    /**
     * ====== Plan 缓存 ======
     */
    public static final String PLAN_CACHE_PREFIX = "plan" + REDIS_SEPARATOR;
    public static final String PLAN_ID_CACHE_KEY = PLAN_CACHE_PREFIX + "id" + REDIS_SEPARATOR;
    public static final String PLAN_LIST_CACHE_KEY = PLAN_CACHE_PREFIX + "list";

    /**
     * ====== Coupon 缓存 ======
     */
    public static final String COUPON_CACHE_PREFIX = "coupon" + REDIS_SEPARATOR;
    public static final String COUPON_ID_CACHE_KEY = COUPON_CACHE_PREFIX + "id" + REDIS_SEPARATOR;

    /**
     * ====== QRCode 缓存 ======
     */
    public static final String QRCODE_CACHE_PREFIX = "qrcode" + REDIS_SEPARATOR;
    public static final String QRCODE_ID_CACHE_KEY = QRCODE_CACHE_PREFIX + "id" + REDIS_SEPARATOR;
    public static final String QRCODE_CODE_CACHE_KEY = QRCODE_CACHE_PREFIX + "code" + REDIS_SEPARATOR;

    /**
     * ====== Session 缓存 ======
     */
    public static final String SESSION_CACHE_PREFIX = "session" + REDIS_SEPARATOR;
    public static final String SESSION_TOKEN_KEY = SESSION_CACHE_PREFIX + "token" + REDIS_SEPARATOR;

    /**
     * ====== 缓存过期时间 (单位: 秒) ======
     */
    // 1小时
    public static final Long CACHE_1_HOUR = 3600L;
    // 1天
    public static final Long CACHE_1_DAY = 86400L;
    // 7天
    public static final Long CACHE_7_DAYS = 604800L;
    // 30天
    public static final Long CACHE_30_DAYS = 2592000L;

    /**
     * 默认缓存过期时间（1小时）
     */
    public static final Long CACHE_EXPIRE_SECONDS = CACHE_1_HOUR;

    /**
     * 私有构造函数
     */
    private CacheConstants() {
        throw new IllegalStateException("不能实例化常量类");
    }
}
