package com.quicktap.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类
 * 用于密码的加密和验证
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 生成示例加密密码（用于初始化）
     * @param password 密码
     * @return 加密后的密码
     */
    public static String generateEncodedPassword(String password) {
        return encode(password);
    }

    /**
     * 私有构造函数
     */
    private PasswordUtil() {
        throw new IllegalStateException("不能实例化工具类");
    }
}
