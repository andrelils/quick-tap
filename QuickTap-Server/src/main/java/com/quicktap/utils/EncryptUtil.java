package com.quicktap.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 数据加密工具
 * 用于加密敏感数据（如 API Key、密码等）
 *
 * 使用 AES/GCM/NoPadding 模式：
 * - GCM 提供真正的认证加密（AEAD）
 * - 每次加密都使用随机的 IV（初始向量）
 * - 提供密文完整性保护
 */
@Component
public class EncryptUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;  // 128 bits
    private static final int IV_LENGTH = 12;  // 12 bytes for GCM

    @Value("${encryption.key:}")
    private String encryptionKeyBase64;

    private byte[] KEY;

    @PostConstruct
    private void init() {
        // 验证加密密钥是否已设置
        if (encryptionKeyBase64 == null || encryptionKeyBase64.isEmpty()) {
            throw new IllegalStateException(
                "加密密钥未设置！必须通过 ENCRYPTION_KEY 环境变量提供 256 位的 Base64 编码密钥。\n" +
                "生成方法: openssl rand -base64 32"
            );
        }

        try {
            // 从 Base64 解码
            KEY = Base64.getDecoder().decode(encryptionKeyBase64);

            // 验证密钥长度（256 位 = 32 字节）
            if (KEY.length != 32) {
                throw new IllegalStateException(
                    "加密密钥长度必须为 256 位（32 字节），当前为 " + (KEY.length * 8) + " 位。\n" +
                    "生成方法: openssl rand -base64 32"
                );
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("加密密钥必须是有效的 Base64 编码", e);
        }
    }

    /**
     * 加密数据
     *
     * @param data 待加密的数据
     * @return Base64 编码的格式：[IV(12字节) + 密文 + Tag(16字节)]
     */
    public String encrypt(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        try {
            // 生成随机 IV
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);

            // 初始化 Cipher
            SecretKeySpec keySpec = new SecretKeySpec(KEY, 0, KEY.length, ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            // 加密数据
            byte[] ciphertext = cipher.doFinal(data.getBytes());

            // 组合：IV + 密文，然后 Base64 编码
            byte[] encryptedData = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encryptedData, 0, iv.length);
            System.arraycopy(ciphertext, 0, encryptedData, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 解密数据
     *
     * @param encryptedData Base64 编码的格式：[IV(12字节) + 密文 + Tag(16字节)]
     * @return 解密后的原始数据
     */
    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }
        try {
            // Base64 解码
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);

            // 提取 IV
            if (decodedData.length < IV_LENGTH) {
                throw new IllegalArgumentException("加密数据格式无效：长度太短");
            }

            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(decodedData, 0, iv, 0, IV_LENGTH);

            // 提取密文（包括 GCM Tag）
            byte[] ciphertext = new byte[decodedData.length - IV_LENGTH];
            System.arraycopy(decodedData, IV_LENGTH, ciphertext, 0, ciphertext.length);

            // 初始化 Cipher
            SecretKeySpec keySpec = new SecretKeySpec(KEY, 0, KEY.length, ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            // 解密
            byte[] decryptedData = cipher.doFinal(ciphertext);
            return new String(decryptedData);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }
}
