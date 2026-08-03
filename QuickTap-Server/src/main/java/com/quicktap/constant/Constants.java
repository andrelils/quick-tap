package com.quicktap.constant;

/**
 * 通用常量类
 * 定义应用中的各种常量
 */
public class Constants {

    /**
     * ====== HTTP 状态码 ======
     */
    public static final Integer HTTP_OK = 200;
    public static final Integer HTTP_BAD_REQUEST = 400;
    public static final Integer HTTP_UNAUTHORIZED = 401;
    public static final Integer HTTP_FORBIDDEN = 403;
    public static final Integer HTTP_NOT_FOUND = 404;
    public static final Integer HTTP_INTERNAL_ERROR = 500;

    /**
     * ====== API 响应码 ======
     */
    public static final Integer CODE_SUCCESS = 0;
    public static final Integer CODE_ERROR = 1;
    public static final Integer CODE_BAD_REQUEST = 400;
    public static final Integer CODE_UNAUTHORIZED = 401;
    public static final Integer CODE_FORBIDDEN = 403;
    public static final Integer CODE_NOT_FOUND = 404;
    public static final Integer CODE_INTERNAL_ERROR = 500;

    /**
     * ====== 分页参数默认值 ======
     */
    public static final Integer DEFAULT_PAGE_NUM = 1;
    public static final Integer DEFAULT_PAGE_SIZE = 10;
    public static final Integer MAX_PAGE_SIZE = 100;

    /**
     * ====== 布尔值 ======
     */
    public static final Integer YES = 1;
    public static final Integer NO = 0;

    /**
     * ====== Admin 角色 ======
     */
    public static final String ROLE_SUPER_ADMIN = "super_admin";
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_MERCHANT = "merchant";

    /**
     * ====== Admin 状态 ======
     */
    public static final Integer ADMIN_STATUS_ENABLED = 1;
    public static final Integer ADMIN_STATUS_DISABLED = 0;

    /**
     * ====== User 状态 ======
     */
    public static final Integer USER_STATUS_NORMAL = 1;
    public static final Integer USER_STATUS_BANNED = 0;

    /**
     * ====== Merchant 审核状态 ======
     */
    public static final Integer MERCHANT_AUDIT_PENDING = 0;    // 待审核
    public static final Integer MERCHANT_AUDIT_APPROVED = 1;   // 已批准
    public static final Integer MERCHANT_AUDIT_REJECTED = 2;   // 已拒绝

    /**
     * ====== Merchant 状态 ======
     */
    public static final Integer MERCHANT_STATUS_NORMAL = 1;
    public static final Integer MERCHANT_STATUS_SUSPENDED = 0;

    /**
     * ====== Device 类型 ======
     */
    public static final String DEVICE_TYPE_NFC = "nfc";
    public static final String DEVICE_TYPE_QRCODE = "qrcode";

    /**
     * ====== Device 状态 ======
     */
    public static final Integer DEVICE_STATUS_NORMAL = 1;
    public static final Integer DEVICE_STATUS_OFFLINE = 0;

    /**
     * ====== Promotion Platform Jump Mode ======
     */
    public static final String JUMP_MODE_SCHEME = "scheme";
    public static final String JUMP_MODE_WEBVIEW = "webview";
    public static final String JUMP_MODE_MINIPROGRAM = "miniprogram";
    public static final String JUMP_MODE_COPY = "copy";

    /**
     * ====== Coupon 类型 ======
     */
    public static final String COUPON_TYPE_CASH = "cash";       // 现金券
    public static final String COUPON_TYPE_DISCOUNT = "discount"; // 折扣券

    /**
     * ====== Coupon 状态 ======
     */
    public static final Integer COUPON_STATUS_NORMAL = 1;       // 正常
    public static final Integer COUPON_STATUS_EXPIRED = 0;      // 已过期

    /**
     * ====== Order 状态 ======
     */
    public static final Integer ORDER_STATUS_PENDING = 0;       // 待支付
    public static final Integer ORDER_STATUS_PAID = 1;          // 已支付
    public static final Integer ORDER_STATUS_EXPIRED = 2;       // 已过期
    public static final Integer ORDER_STATUS_COMPLETED = 3;     // 已完成

    /**
     * ====== AI Generate 类型 ======
     */
    public static final String AI_TYPE_TEXT = "text";           // 文本生成
    public static final String AI_TYPE_IMAGE = "image";         // 图片生成
    public static final String AI_TYPE_VIDEO = "video";         // 视频生成

    /**
     * ====== AI Generate 状态 ======
     */
    public static final Integer AI_STATUS_PENDING = 0;          // 待处理
    public static final Integer AI_STATUS_PROCESSING = 1;       // 处理中
    public static final Integer AI_STATUS_COMPLETED = 2;        // 已完成
    public static final Integer AI_STATUS_FAILED = 3;           // 已失败

    /**
     * ====== 文件上传 ======
     */
    public static final String UPLOAD_DIR = "uploads";
    public static final Long MAX_FILE_SIZE = 100 * 1024 * 1024L; // 100MB
    public static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png", "gif"};
    public static final String[] ALLOWED_VIDEO_TYPES = {"mp4", "avi", "mov", "flv"};

    /**
     * ====== JWT ======
     */
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final Long TOKEN_EXPIRE_TIME = 604800000L;    // 7 days in milliseconds

    /**
     * ====== 请求头 ======
     */
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * ====== 时间常量 ======
     */
    public static final Long ONE_MINUTE = 60L;
    public static final Long ONE_HOUR = 3600L;
    public static final Long ONE_DAY = 86400L;
    public static final Long ONE_WEEK = 604800L;

    /**
     * 私有构造函数
     */
    private Constants() {
        throw new IllegalStateException("不能实例化常量类");
    }
}
