package com.quicktap.common.constants;

/**
 * 系统通用常量配置
 *
 * 用于存放项目中的所有常用常量
 * 便于统一管理和维护
 *
 * 包含的常量分类：
 * - HTTP/响应相关常量
 * - 分页相关常量
 * - 时间相关常量
 * - 安全相关常量
 * - 系统配置常量
 *
 * 使用示例：
 * {@code
 * // 在控制器中使用
 * if (CollectionUtil.size(list) > SystemConstants.MAX_BATCH_SIZE) {
 *     throw new ValidationException("Batch size exceeds limit");
 * }
 *
 * // 在缓存Key中使用
 * String cacheKey = SystemConstants.CACHE_KEY_PREFIX + userId;
 *
 * // 在时间计算中使用
 * long expiryTime = System.currentTimeMillis() + SystemConstants.CACHE_EXPIRY_ONE_HOUR;
 * }
 *
 * @see com.quicktap.constant.Constants
 * @see com.quicktap.constant.CacheConstants
 * @see com.quicktap.constant.KafkaTopics
 */
public class SystemConstants {

    /**
     * 私有构造方法，防止实例化
     */
    private SystemConstants() {
    }

    // ==================== 分页常量 ====================

    /**
     * 默认分页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小（防止一次加载过多数据）
     */
    public static final Integer MAX_PAGE_SIZE = 100;

    /**
     * 最小分页大小
     */
    public static final Integer MIN_PAGE_SIZE = 1;

    /**
     * 默认分页号
     */
    public static final Integer DEFAULT_PAGE_NUM = 1;

    // ==================== 时间常量（秒） ====================

    /**
     * 1分钟的秒数
     */
    public static final Integer ONE_MINUTE = 60;

    /**
     * 5分钟的秒数
     */
    public static final Integer FIVE_MINUTES = 5 * 60;

    /**
     * 15分钟的秒数
     */
    public static final Integer FIFTEEN_MINUTES = 15 * 60;

    /**
     * 30分钟的秒数
     */
    public static final Integer THIRTY_MINUTES = 30 * 60;

    /**
     * 1小时的秒数
     */
    public static final Integer ONE_HOUR = 3600;

    /**
     * 1天的秒数
     */
    public static final Integer ONE_DAY = 86400;

    /**
     * 7天的秒数
     */
    public static final Integer ONE_WEEK = 7 * 86400;

    /**
     * 30天的秒数
     */
    public static final Integer ONE_MONTH = 30 * 86400;

    /**
     * 90天的秒数（用于审计日志保留）
     */
    public static final Integer NINETY_DAYS = 90 * 86400;

    /**
     * 1年的秒数
     */
    public static final Integer ONE_YEAR = 365 * 86400;

    // ==================== 批量操作常量 ====================

    /**
     * 默认批量操作大小限制
     */
    public static final Integer DEFAULT_BATCH_SIZE = 100;

    /**
     * 最大批量操作大小限制
     */
    public static final Integer MAX_BATCH_SIZE = 1000;

    /**
     * 最小批量操作大小限制
     */
    public static final Integer MIN_BATCH_SIZE = 1;

    // ==================== 文件相关常量 ====================

    /**
     * 单个文件最大大小：100MB
     */
    public static final Long MAX_FILE_SIZE = 100 * 1024 * 1024L;

    /**
     * 单个文件最小大小：1KB
     */
    public static final Long MIN_FILE_SIZE = 1024L;

    /**
     * 图片文件最大大小：10MB
     */
    public static final Long MAX_IMAGE_SIZE = 10 * 1024 * 1024L;

    /**
     * 视频文件最大大小：500MB
     */
    public static final Long MAX_VIDEO_SIZE = 500 * 1024 * 1024L;

    /**
     * 允许的图片格式
     */
    public static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png", "gif", "bmp"};

    /**
     * 允许的视频格式
     */
    public static final String[] ALLOWED_VIDEO_TYPES = {"mp4", "avi", "mov", "mkv", "webm"};

    // ==================== 缓存常量 ====================

    /**
     * 缓存Key前缀
     */
    public static final String CACHE_KEY_PREFIX = "quicktap:";

    /**
     * 缓存过期时间：1小时（秒）
     */
    public static final Long CACHE_EXPIRY_ONE_HOUR = 3600L;

    /**
     * 缓存过期时间：1天（秒）
     */
    public static final Long CACHE_EXPIRY_ONE_DAY = 86400L;

    /**
     * 缓存过期时间：7天（秒）
     */
    public static final Long CACHE_EXPIRY_ONE_WEEK = 604800L;

    /**
     * 缓存过期时间：30天（秒）
     */
    public static final Long CACHE_EXPIRY_ONE_MONTH = 2592000L;

    // ==================== 安全相关常量 ====================

    /**
     * JWT Token过期时间：7天（毫秒）
     */
    public static final Long JWT_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 刷新Token过期时间：30天（毫秒）
     */
    public static final Long JWT_REFRESH_TOKEN_EXPIRE_TIME = 30 * 24 * 60 * 60 * 1000L;

    /**
     * Token请求头名称
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 密码最小长度
     */
    public static final Integer PASSWORD_MIN_LENGTH = 8;

    /**
     * 密码最大长度
     */
    public static final Integer PASSWORD_MAX_LENGTH = 32;

    /**
     * 用户名最小长度
     */
    public static final Integer USERNAME_MIN_LENGTH = 3;

    /**
     * 用户名最大长度
     */
    public static final Integer USERNAME_MAX_LENGTH = 32;

    // ==================== 请求相关常量 ====================

    /**
     * 默认请求超时时间（毫秒）：30秒
     */
    public static final Integer DEFAULT_REQUEST_TIMEOUT = 30000;

    /**
     * HTTP请求头：User-Agent
     */
    public static final String HEADER_USER_AGENT = "User-Agent";

    /**
     * HTTP请求头：Content-Type
     */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * HTTP请求头：X-Forwarded-For（代理转发的真实IP）
     */
    public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * HTTP请求头：Authorization
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * Content-Type：application/json
     */
    public static final String CONTENT_TYPE_JSON = "application/json";

    /**
     * Content-Type：multipart/form-data
     */
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";

    // ==================== 日志相关常量 ====================

    /**
     * 最大日志记录大小（字节）：10KB
     * 防止日志过大导致性能问题
     */
    public static final Integer MAX_LOG_SIZE = 10240;

    /**
     * 敏感字段脱敏符号
     */
    public static final String SENSITIVE_MASK = "***";

    // ==================== 审计日志常量 ====================

    /**
     * 审计日志保留天数
     */
    public static final Integer AUDIT_LOG_RETENTION_DAYS = 90;

    /**
     * 敏感操作事件类型
     */
    public static final String[] SENSITIVE_EVENT_TYPES = {
            "PASSWORD_CHANGE",
            "PASSWORD_RESET",
            "ROLE_GRANT",
            "ROLE_REVOKE",
            "PERMISSION_GRANT",
            "PERMISSION_REVOKE",
            "CONFIG_CHANGE",
            "SECURITY_SETTING_CHANGE",
            "USER_DELETE",
            "MERCHANT_AUDIT",
            "ADMIN_CREATE",
            "ADMIN_DELETE"
    };

    // ==================== 业务逻辑常量 ====================

    /**
     * 订单支付超时时间：15分钟（秒）
     */
    public static final Integer ORDER_PAY_TIMEOUT = 15 * 60;

    /**
     * 优惠券过期提醒天数
     */
    public static final Integer COUPON_EXPIRY_REMINDER_DAYS = 3;

    /**
     * 登录失败最多重试次数
     */
    public static final Integer MAX_LOGIN_ATTEMPT = 5;

    /**
     * 登录失败锁定时间：30分钟（秒）
     */
    public static final Integer LOGIN_LOCK_DURATION = 30 * 60;

    /**
     * 默认AI生成超时时间：60秒
     */
    public static final Integer DEFAULT_AI_GENERATE_TIMEOUT = 60;

    /**
     * 最大并发AI生成任务数
     */
    public static final Integer MAX_CONCURRENT_AI_TASKS = 10;

    // ==================== 状态码常量 ====================

    /**
     * 启用状态
     */
    public static final Integer STATUS_ENABLED = 1;

    /**
     * 禁用状态
     */
    public static final Integer STATUS_DISABLED = 0;

    /**
     * 待审核状态
     */
    public static final Integer STATUS_PENDING = 0;

    /**
     * 已批准状态
     */
    public static final Integer STATUS_APPROVED = 1;

    /**
     * 已拒绝状态
     */
    public static final Integer STATUS_REJECTED = 2;
}
