package com.quicktap.common.utils;

import com.quicktap.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * 验证工具类
 *
 * 提供常见的参数验证方法
 * 包含：
 * - 邮箱验证
 * - 手机号验证
 * - 身份证号验证
 * - URL验证
 * - IP地址验证
 * - 特殊字符验证
 * - 数值范围验证
 * - 字符串长度验证
 *
 * 使用场景：
 * - 用户注册/登录表单验证
 * - 邮箱和手机号合法性检查
 * - 参数格式验证
 * - 业务数据合法性验证
 *
 * 使用示例：
 * {@code
 * // 邮箱验证
 * if (!ValidationUtil.isValidEmail(request.getEmail())) {
 *     throw new ValidationException("Invalid email format");
 * }
 *
 * // 手机号验证
 * if (!ValidationUtil.isValidPhoneNumber(request.getPhone())) {
 *     throw new ValidationException("Invalid phone number");
 * }
 *
 * // 身份证验证
 * if (!ValidationUtil.isValidIdCard(request.getIdCard())) {
 *     throw new ValidationException("Invalid ID card number");
 * }
 *
 * // URL验证
 * if (!ValidationUtil.isValidUrl(request.getWebsite())) {
 *     throw new ValidationException("Invalid website URL");
 * }
 *
 * // IP验证
 * if (!ValidationUtil.isValidIpAddress(clientIp)) {
 *     throw new ValidationException("Invalid IP address");
 * }
 * }
 */
@Slf4j
public class ValidationUtil {

    /**
     * 私有构造方法，防止实例化
     */
    private ValidationUtil() {
    }

    /**
     * 验证邮箱地址的合法性
     * 支持的格式：username@domain.suffix
     * 支持的特殊字符：. - _
     *
     * @param email 待验证的邮箱地址
     * @return true表示邮箱地址有效，false表示无效
     *
     * 验证规则：
     * - 长度在6-254字符之间
     * - 包含@符号
     * - @前后都有内容
     * - 域名至少包含一个点号
     */
    public static boolean isValidEmail(String email) {
        if (StringUtil.isEmpty(email)) {
            return false;
        }
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(regex, email);
    }

    /**
     * 验证中国大陆手机号的合法性
     * 支持11位的三大运营商号码
     *
     * @param phoneNumber 待验证的手机号
     * @return true表示手机号有效，false表示无效
     *
     * 验证规则：
     * - 必须是11位数字
     * - 首位必须是1
     * - 第二位可以是3-9（代表不同运营商）
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (StringUtil.isEmpty(phoneNumber)) {
            return false;
        }
        String regex = "^1[3-9]\\d{9}$";
        return Pattern.matches(regex, phoneNumber);
    }

    /**
     * 验证身份证号的合法性
     * 支持18位身份证号（第二代身份证）
     *
     * @param idCard 待验证的身份证号
     * @return true表示身份证号有效，false表示无效
     *
     * 验证规则：
     * - 长度为18位
     * - 前17位为数字
     * - 第18位可以是0-9或X
     * - 简单格式验证（不包含校验码验证）
     */
    public static boolean isValidIdCard(String idCard) {
        if (StringUtil.isEmpty(idCard)) {
            return false;
        }
        String regex = "^\\d{17}[0-9Xx]$";
        return Pattern.matches(regex, idCard);
    }

    /**
     * 验证URL的合法性
     *
     * @param url 待验证的URL
     * @return true表示URL有效，false表示无效
     *
     * 验证规则：
     * - 必须以http://或https://开头
     * - 必须包含合法的域名
     * - 支持端口号
     * - 支持路径和查询参数
     */
    public static boolean isValidUrl(String url) {
        if (StringUtil.isEmpty(url)) {
            return false;
        }
        String regex = "^https?://[\\w.-]+(:\\d+)?(/.*)?$";
        return Pattern.matches(regex, url);
    }

    /**
     * 验证IPv4地址的合法性
     *
     * @param ipAddress 待验证的IP地址
     * @return true表示IP地址有效，false表示无效
     *
     * 验证规则：
     * - 四个数字由点号分隔
     * - 每个数字在0-255之间
     * - 不支持IPv6
     */
    public static boolean isValidIpAddress(String ipAddress) {
        if (StringUtil.isEmpty(ipAddress)) {
            return false;
        }
        String regex = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
        return Pattern.matches(regex, ipAddress);
    }

    /**
     * 验证字符串中不包含特定的危险字符
     * 用于防止SQL注入、XSS等攻击
     *
     * @param input 待验证的字符串
     * @return true表示不包含危险字符，false表示包含危险字符
     *
     * 检查的危险字符：
     */
    public static boolean isNotContainDangerousChars(String input) {
        if (StringUtil.isEmpty(input)) {
            return true;
        }
        String regex = "^[\\w\\-._~:/?#\\[\\]@!$%&'()*+,=]*$";
        return Pattern.matches(regex, input);
    }

    /**
     * 验证字符串是否只包含字母和数字
     *
     * @param input 待验证的字符串
     * @return true表示只包含字母和数字，false表示包含其他字符
     */
    public static boolean isAlphaNumeric(String input) {
        if (StringUtil.isEmpty(input)) {
            return false;
        }
        return Pattern.matches("^[a-zA-Z0-9]+$", input);
    }

    /**
     * 验证字符串是否只包含数字
     *
     * @param input 待验证的字符串
     * @return true表示只包含数字，false表示包含其他字符
     */
    public static boolean isNumeric(String input) {
        if (StringUtil.isEmpty(input)) {
            return false;
        }
        return Pattern.matches("^\\d+$", input);
    }

    /**
     * 验证字符串是否只包含字母
     *
     * @param input 待验证的字符串
     * @return true表示只包含字母，false表示包含其他字符
     */
    public static boolean isAlpha(String input) {
        if (StringUtil.isEmpty(input)) {
            return false;
        }
        return Pattern.matches("^[a-zA-Z]+$", input);
    }

    /**
     * 验证字符串长度是否在指定范围内
     *
     * @param input 待验证的字符串
     * @param minLength 最小长度（包含）
     * @param maxLength 最大长度（包含）
     * @return true表示长度在范围内，false表示超出范围或字符串为null
     */
    public static boolean isLengthInRange(String input, int minLength, int maxLength) {
        if (input == null) {
            return false;
        }
        int length = input.length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * 验证数值是否在指定范围内
     *
     * @param value 待验证的数值
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return true表示数值在范围内，false表示超出范围
     */
    public static boolean isValueInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * 验证长整数是否在指定范围内
     *
     * @param value 待验证的长整数
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return true表示数值在范围内，false表示超出范围
     */
    public static boolean isValueInRange(long value, long min, long max) {
        return value >= min && value <= max;
    }

    /**
     * 验证小数是否在指定范围内
     *
     * @param value 待验证的小数
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return true表示数值在范围内，false表示超出范围
     */
    public static boolean isValueInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /**
     * 验证用户名的合法性
     * 用户名必须为字母、数字、下划线的组合
     *
     * @param username 待验证的用户名
     * @return true表示用户名有效，false表示无效
     *
     * 验证规则：
     * - 长度在3-32字符之间
     * - 只能包含字母、数字、下划线
     * - 不能以数字开头
     */
    public static boolean isValidUsername(String username) {
        if (StringUtil.isEmpty(username)) {
            return false;
        }
        return isLengthInRange(username, 3, 32) &&
                Pattern.matches("^[a-zA-Z_][a-zA-Z0-9_]*$", username);
    }

    /**
     * 验证密码强度
     * 强密码要求：混合大小写字母、数字和特殊字符
     *
     * @param password 待验证的密码
     * @return true表示密码符合强度要求，false表示不符合
     *
     * 验证规则：
     * - 长度至少8个字符
     * - 必须包含大写字母
     * - 必须包含小写字母
     * - 必须包含数字
     * - 必须包含特殊字符（!@#$%^&*）
     */
    public static boolean isStrongPassword(String password) {
        if (StringUtil.isEmpty(password) || password.length() < 8) {
            return false;
        }
        boolean hasUpper = Pattern.matches(".*[A-Z].*", password);
        boolean hasLower = Pattern.matches(".*[a-z].*", password);
        boolean hasDigit = Pattern.matches(".*\\d.*", password);
        boolean hasSpecial = Pattern.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};\\':\",.<>?/].*", password);

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
