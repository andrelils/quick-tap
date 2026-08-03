package com.quicktap.utils;

import lombok.extern.slf4j.Slf4j;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 敏感数据脱敏工具类
 * 用于在日志输出中隐藏密码、Token、API密钥等敏感信息
 *
 * 安全特性:
 * - 支持多种敏感字段检测（密码、Token、密钥等）
 * - 支持JSON格式数据脱敏
 * - 支持查询参数脱敏
 * - 支持请求体脱敏
 * - 防止敏感信息泄露到日志
 */
@Slf4j
public class SensitiveDataSanitizer {

    // 敏感字段名称模式（不区分大小写）
    private static final String[] SENSITIVE_FIELD_PATTERNS = {
        // 密码相关
        "password", "pwd", "passwd", "secret", "pin",
        // Token相关
        "token", "accesstoken", "access_token", "refreshtoken", "refresh_token",
        "authorization", "auth", "jwt", "bearer",
        // API密钥相关
        "apikey", "api_key", "secretkey", "secret_key", "key",
        // 信用卡和支付信息
        "creditcard", "credit_card", "cardnumber", "card_number", "cvv", "cvc",
        "ssn", "socialsecuritynumber", "social_security_number",
        // 个人识别信息
        "idcard", "id_card", "nationalid", "national_id",
        "phonenumber", "phone_number", "telephone", "mobile",
        "email", "emailaddress", "email_address",
        // 其他敏感信息
        "appkey", "app_key", "clientsecret", "client_secret",
        "accesskey", "access_key", "secretaccesskey", "secret_access_key",
        "sessiontoken", "session_token", "authcode", "auth_code",
        "otp", "verificationcode", "verification_code", "captcha"
    };

    // 编译预先编译的正则表达式以提高性能
    private static final Pattern JSON_PATTERN = Pattern.compile(
        "\"(" + String.join("|", SENSITIVE_FIELD_PATTERNS) + ")\"\\s*:\\s*\"([^\"]*)\"|" +
        "'(" + String.join("|", SENSITIVE_FIELD_PATTERNS) + ")'\\s*:\\s*'([^']*)'",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );

    private static final Pattern URL_PARAM_PATTERN = Pattern.compile(
        "(" + String.join("|", SENSITIVE_FIELD_PATTERNS) + ")=([^&]*)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern REQUEST_BODY_PATTERN = Pattern.compile(
        "(" + String.join("|", SENSITIVE_FIELD_PATTERNS) + ")[:=\\s]+([^\\s,;\\}]*)",
        Pattern.CASE_INSENSITIVE
    );

    // 基础模式：匹配 Bearer token 和其他常见格式
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile(
        "Bearer\\s+([\\w.-]+)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern JWT_PATTERN = Pattern.compile(
        "eyJ[A-Za-z0-9_-]+\\.eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 脱敏主要方法 - 用于日志内容
     *
     * @param content 原始内容
     * @return 脱敏后的内容
     */
    public static String sanitize(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String result = content;

        // 脱敏 JSON 格式数据
        result = sanitizeJsonContent(result);

        // 脱敏 URL 查询参数
        result = sanitizeUrlParameters(result);

        // 脱敏 Bearer Token
        result = sanitizeBearerToken(result);

        // 脱敏 JWT Token
        result = sanitizeJwtToken(result);

        return result;
    }

    /**
     * 脱敏 JSON 格式的敏感字段
     * 例如: "password": "mypassword123" → "password": "***"
     *
     * @param content JSON 内容
     * @return 脱敏后的内容
     */
    private static String sanitizeJsonContent(String content) {
        Matcher matcher = JSON_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            // 第1和2组用于双引号
            // 第3和4组用于单引号
            String fieldName = matcher.group(1) != null ? matcher.group(1) : matcher.group(3);
            String fieldValue = matcher.group(2) != null ? matcher.group(2) : matcher.group(4);

            // 不脱敏空值
            if (fieldValue != null && !fieldValue.isEmpty()) {
                String replacement;
                if (fieldValue.length() <= 2) {
                    replacement = "***";
                } else {
                    // 保留前2个和后1个字符，其余用 * 替换
                    String prefix = fieldValue.substring(0, Math.min(2, fieldValue.length()));
                    String suffix = fieldValue.length() > 2 ? fieldValue.substring(fieldValue.length() - 1) : "";
                    replacement = prefix + "*".repeat(Math.max(1, fieldValue.length() - 3)) + suffix;
                }

                if (matcher.group(1) != null) {
                    // 双引号格式
                    matcher.appendReplacement(sb,
                        "\"" + fieldName + "\"\\: \"" + Matcher.quoteReplacement(replacement) + "\"");
                } else {
                    // 单引号格式
                    matcher.appendReplacement(sb,
                        "'" + fieldName + "'\\: '" + Matcher.quoteReplacement(replacement) + "'");
                }
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 脱敏 URL 中的敏感查询参数
     * 例如: ?password=mypassword → ?password=***
     *
     * @param content 包含 URL 的内容
     * @return 脱敏后的内容
     */
    private static String sanitizeUrlParameters(String content) {
        Matcher matcher = URL_PARAM_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String paramName = matcher.group(1);
            String paramValue = matcher.group(2);

            // 不脱敏空值
            if (paramValue != null && !paramValue.isEmpty()) {
                String replacement = createMaskedValue(paramValue);
                matcher.appendReplacement(sb,
                    Matcher.quoteReplacement(paramName + "=" + replacement));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 脱敏 Bearer Token
     * 例如: Authorization: Bearer eyJhbGc... → Authorization: Bearer ****...
     *
     * @param content 原始内容
     * @return 脱敏后的内容
     */
    private static String sanitizeBearerToken(String content) {
        Matcher matcher = BEARER_TOKEN_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String token = matcher.group(1);
            if (token != null && !token.isEmpty()) {
                String masked = createMaskedValue(token);
                matcher.appendReplacement(sb, "Bearer " + Matcher.quoteReplacement(masked));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 脱敏 JWT Token
     * JWT格式: xxx.yyy.zzz → xx*.yy*.zz*
     *
     * @param content 原始内容
     * @return 脱敏后的内容
     */
    private static String sanitizeJwtToken(String content) {
        Matcher matcher = JWT_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String token = matcher.group(0);
            if (token != null && !token.isEmpty()) {
                // JWT格式: header.payload.signature
                String[] parts = token.split("\\.");
                StringBuilder maskedToken = new StringBuilder();

                for (int i = 0; i < parts.length; i++) {
                    if (i > 0) {
                        maskedToken.append(".");
                    }
                    String part = parts[i];
                    maskedToken.append(createMaskedValue(part));
                }

                matcher.appendReplacement(sb, Matcher.quoteReplacement(maskedToken.toString()));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 创建掩码值 - 隐藏大部分字符，保留两端各1-2个
     *
     * @param value 原始值
     * @return 掩码值，如：ab****ef
     */
    private static String createMaskedValue(String value) {
        if (value == null || value.isEmpty()) {
            return "***";
        }

        if (value.length() <= 4) {
            return "****";
        }

        // 保留前2个和后2个字符，中间用 * 替换
        String prefix = value.substring(0, 2);
        String suffix = value.substring(value.length() - 2);
        int maskLength = Math.max(4, value.length() - 4);
        return prefix + "*".repeat(maskLength) + suffix;
    }

    /**
     * 脱敏认证令牌/授权头
     * 用于脱敏 Authorization 头和其他认证相关的头信息
     *
     * @param headerValue 头部值
     * @return 脱敏后的值
     */
    public static String sanitizeAuthHeader(String headerValue) {
        if (headerValue == null || headerValue.isEmpty()) {
            return headerValue;
        }

        // 如果是 Bearer token
        if (headerValue.startsWith("Bearer ")) {
            return "Bearer " + createMaskedValue(headerValue.substring(7));
        }

        // 如果是其他格式的 token
        if (headerValue.length() > 20) {
            return createMaskedValue(headerValue);
        }

        return headerValue;
    }

    /**
     * 脱敏请求体中的敏感信息
     * 支持 key=value 格式和 key: value 格式
     *
     * @param requestBody 请求体
     * @return 脱敏后的请求体
     */
    public static String sanitizeRequestBody(String requestBody) {
        if (requestBody == null || requestBody.isEmpty()) {
            return requestBody;
        }

        // 首先尝试作为 JSON 脱敏
        String result = sanitizeJsonContent(requestBody);

        // 然后尝试作为 URL 编码参数脱敏
        result = sanitizeUrlParameters(result);

        return result;
    }

    /**
     * 检查内容是否包含敏感信息
     * 用于决定是否需要脱敏
     *
     * @param content 内容
     * @return true 如果包含敏感信息，false 否则
     */
    public static boolean containsSensitiveData(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }

        // 检查是否包含敏感字段名称
        String lowerContent = content.toLowerCase();
        for (String pattern : SENSITIVE_FIELD_PATTERNS) {
            if (lowerContent.contains(pattern)) {
                return true;
            }
        }

        // 检查是否包含 Bearer token
        if (BEARER_TOKEN_PATTERN.matcher(content).find()) {
            return true;
        }

        // 检查是否包含 JWT token
        if (JWT_PATTERN.matcher(content).find()) {
            return true;
        }

        return false;
    }

    /**
     * 私有构造函数 - 防止实例化
     */
    private SensitiveDataSanitizer() {
        throw new IllegalStateException("不能实例化工具类");
    }
}
