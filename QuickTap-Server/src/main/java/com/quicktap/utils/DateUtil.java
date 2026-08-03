package com.quicktap.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 * 用于日期的格式化、解析和转换
 */
public class DateUtil {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATETIME_MILLI_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 获取当前时间
     * @return 当前时间戳（毫秒）
     */
    public static Long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前日期时间字符串
     * @return 格式: yyyy-MM-dd HH:mm:ss
     */
    public static String getCurrentDateTime() {
        return format(new Date(), DATETIME_FORMAT);
    }

    /**
     * 获取当前日期字符串
     * @return 格式: yyyy-MM-dd
     */
    public static String getCurrentDate() {
        return format(new Date(), DATE_FORMAT);
    }

    /**
     * 格式化日期
     * @param date 日期
     * @param format 格式
     * @return 格式化后的日期字符串
     */
    public static String format(Date date, String format) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 默认格式化日期为 yyyy-MM-dd HH:mm:ss
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    public static String format(Date date) {
        return format(date, DATETIME_FORMAT);
    }

    /**
     * 解析日期字符串
     * @param dateStr 日期字符串
     * @param format 格式
     * @return 日期对象
     */
    public static Date parse(String dateStr, String format) {
        try {
            return new SimpleDateFormat(format).parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 按默认格式 yyyy-MM-dd HH:mm:ss 解析日期字符串
     * @param dateStr 日期字符串
     * @return 日期对象
     */
    public static Date parse(String dateStr) {
        return parse(dateStr, DATETIME_FORMAT);
    }

    /**
     * 计算两个日期的差值（天数）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 天数
     */
    public static Long daysDifference(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return null;
        }
        return (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24);
    }

    /**
     * 计算两个日期的差值（小时）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 小时数
     */
    public static Long hoursDifference(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return null;
        }
        return (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60);
    }

    /**
     * 计算两个日期的差值（分钟）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分钟数
     */
    public static Long minutesDifference(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return null;
        }
        return (endDate.getTime() - startDate.getTime()) / (1000 * 60);
    }

    /**
     * 判断日期是否已过期
     * @param date 日期
     * @return 是否已过期
     */
    public static Boolean isExpired(Date date) {
        if (date == null) {
            return false;
        }
        return date.before(new Date());
    }

    /**
     * 私有构造函数
     */
    private DateUtil() {
        throw new IllegalStateException("不能实例化工具类");
    }
}
