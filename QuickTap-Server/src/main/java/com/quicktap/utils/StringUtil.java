package com.quicktap.utils;

import java.util.Collection;
import java.util.Map;

/**
 * 字符串工具类
 * 用于常见的字符串操作
 */
public class StringUtil {

    /**
     * 判断字符串是否为空
     * @param str 字符串
     * @return 是否为空（包括 null 和 ""）
     */
    public static Boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 判断字符串是否不为空
     * @param str 字符串
     * @return 是否不为空
     */
    public static Boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否为空白（包括空格）
     * @param str 字符串
     * @return 是否为空白
     */
    public static Boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * 判断字符串是否不为空白
     * @param str 字符串
     * @return 是否不为空白
     */
    public static Boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 判断集合是否为空
     * @param collection 集合
     * @return 是否为空
     */
    public static <T> Boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断集合是否不为空
     * @param collection 集合
     * @return 是否不为空
     */
    public static <T> Boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }

    /**
     * 判断 Map 是否为空
     * @param map Map
     * @return 是否为空
     */
    public static <K, V> Boolean isEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断 Map 是否不为空
     * @param map Map
     * @return 是否不为空
     */
    public static <K, V> Boolean isNotEmpty(Map<K, V> map) {
        return !isEmpty(map);
    }

    /**
     * 获取字符串长度
     * @param str 字符串
     * @return 长度（null 返回 0）
     */
    public static Integer length(String str) {
        return str == null ? 0 : str.length();
    }

    /**
     * 字符串拼接
     * @param strs 字符串数组
     * @return 拼接后的字符串
     */
    public static String concat(String... strs) {
        if (strs == null || strs.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String str : strs) {
            if (str != null) {
                sb.append(str);
            }
        }
        return sb.toString();
    }

    /**
     * 字符串拼接（指定分隔符）
     * @param delimiter 分隔符
     * @param strs 字符串数组
     * @return 拼接后的字符串
     */
    public static String join(String delimiter, String... strs) {
        if (strs == null || strs.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            if (i > 0 && isNotEmpty(delimiter)) {
                sb.append(delimiter);
            }
            if (strs[i] != null) {
                sb.append(strs[i]);
            }
        }
        return sb.toString();
    }

    /**
     * 将驼峰命名转换为下划线命名
     * @param str 驼峰字符串
     * @return 下划线字符串
     */
    public static String camelToUnderscore(String str) {
        if (isEmpty(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                result.append('_').append(Character.toLowerCase(c));
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    /**
     * 将下划线命名转换为驼峰命名
     * @param str 下划线字符串
     * @return 驼峰字符串
     */
    public static String underscoreToCamel(String str) {
        if (isEmpty(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        boolean nextIsUpper = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_') {
                nextIsUpper = true;
            } else {
                if (nextIsUpper) {
                    result.append(Character.toUpperCase(c));
                    nextIsUpper = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }

    /**
     * 首字母大写
     * @param str 字符串
     * @return 首字母大写后的字符串
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 首字母小写
     * @param str 字符串
     * @return 首字母小写后的字符串
     */
    public static String uncapitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * 私有构造函数
     */
    private StringUtil() {
        throw new IllegalStateException("不能实例化工具类");
    }
}
