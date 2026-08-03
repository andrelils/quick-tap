package com.quicktap.constant;

/**
 * Kafka 主题常量类
 * 定义所有 Kafka 消息队列的主题名称
 */
public class KafkaTopics {

    /**
     * ====== Merchant 相关主题 ======
     */
    // 商户创建事件
    public static final String MERCHANT_CREATED = "merchant-created";
    // 商户更新事件
    public static final String MERCHANT_UPDATED = "merchant-updated";
    // 商户审核事件
    public static final String MERCHANT_AUDIT = "merchant-audit";

    /**
     * ====== Device 相关主题 ======
     */
    // 设备创建事件
    public static final String DEVICE_CREATED = "device-created";
    // 设备更新事件
    public static final String DEVICE_UPDATED = "device-updated";
    // 设备删除事件
    public static final String DEVICE_DELETED = "device-deleted";

    /**
     * ====== Order 相关主题 ======
     */
    // 订单创建事件
    public static final String ORDER_CREATED = "order-created";
    // 订单支付成功事件
    public static final String ORDER_PAID = "order-paid";
    // 订单完成事件
    public static final String ORDER_COMPLETED = "order-completed";
    // 订单过期事件
    public static final String ORDER_EXPIRED = "order-expired";

    /**
     * ====== AI 相关主题 ======
     */
    // AI 内容生成请求
    public static final String AI_GENERATE_REQUEST = "ai-generate-request";
    // AI 内容生成完成事件
    public static final String AI_GENERATE_COMPLETED = "ai-generate-completed";
    // AI 内容生成失败事件
    public static final String AI_GENERATE_FAILED = "ai-generate-failed";

    /**
     * ====== Coupon 相关主题 ======
     */
    // 卡券创建事件
    public static final String COUPON_CREATED = "coupon-created";
    // 卡券已使用事件
    public static final String COUPON_USED = "coupon-used";
    // 卡券过期事件
    public static final String COUPON_EXPIRED = "coupon-expired";

    /**
     * ====== User 相关主题 ======
     */
    // 用户注册事件
    public static final String USER_REGISTERED = "user-registered";
    // 用户登录事件
    public static final String USER_LOGIN = "user-login";

    /**
     * ====== 异步任务主题 ======
     */
    // 邮件发送任务
    public static final String EMAIL_SEND = "email-send";
    // 短信发送任务
    public static final String SMS_SEND = "sms-send";
    // 数据同步任务
    public static final String DATA_SYNC = "data-sync";

    /**
     * Kafka 消费者分组前缀
     */
    public static final String CONSUMER_GROUP_PREFIX = "quicktap-";

    /**
     * 私有构造函数
     */
    private KafkaTopics() {
        throw new IllegalStateException("不能实例化常量类");
    }
}
