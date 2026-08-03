package com.quicktap.utils;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * ID 生成工具类
 * 用于生成唯一 ID（UUID、雪花算法等）
 *
 * 安全特性:
 * - 使用 SecureRandom 而非 Math.random() 生成随机数
 * - 所有随机生成操作都使用密码学安全的方法
 */
public class IdUtil {

    // 密码学安全的随机数生成器
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成 UUID（不含 -）
     * @return UUID 字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成 UUID（含 -）
     * @return UUID 字符串
     */
    public static String generateUUIDWithHyphen() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成订单号
     * 格式: ORD + 时间戳 + 随机数
     * @return 订单号
     */
    public static String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + randomNumeric(6);
    }

    /**
     * 生成二维码编码
     * 格式: QR + UUID 后16位
     * @return 二维码编码
     */
    public static String generateQRCode() {
        String uuid = generateUUID();
        return "QR" + uuid.substring(uuid.length() - 16);
    }

    /**
     * 生成设备编号
     * 格式: DEV + 时间戳 + 随机数
     * @return 设备编号
     */
    public static String generateDeviceNo() {
        return "DEV" + System.currentTimeMillis() + randomNumeric(4);
    }

    /**
     * 生成随机数字字符串
     * 使用 SecureRandom 确保密码学安全的随机性
     *
     * @param length 长度
     * @return 随机数字字符串
     */
    public static String randomNumeric(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于 0");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            // 使用 SecureRandom 生成 0-9 的随机数字
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 私有构造函数
     */
    private IdUtil() {
        throw new IllegalStateException("不能实例化工具类");
    }
}
