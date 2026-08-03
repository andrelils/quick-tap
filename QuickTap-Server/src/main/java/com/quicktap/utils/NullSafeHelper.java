package com.quicktap.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 空指针防护工具类
 * 提供防御性编程辅助方法，减少空指针异常
 *
 * 特性:
 * - 安全的null检查
 * - 默认值支持
 * - 安全的集合操作
 * - 条件执行
 * - 链式操作支持
 */
@Slf4j
public class NullSafeHelper {

    /**
     * 检查对象是否为空，如果为空则返回默认值
     *
     * @param obj 要检查的对象
     * @param defaultValue 默认值
     * @return 对象如果非空则返回对象，否则返回默认值
     */
    public static <T> T orElse(T obj, T defaultValue) {
        return obj != null ? obj : defaultValue;
    }

    /**
     * 检查对象是否为空，如果为空则通过supplier获取默认值
     *
     * @param obj 要检查的对象
     * @param defaultValueSupplier 默认值提供者
     * @return 对象如果非空则返回对象，否则返回supplier提供的值
     */
    public static <T> T orElseGet(T obj, Supplier<T> defaultValueSupplier) {
        if (obj != null) {
            return obj;
        }
        try {
            return defaultValueSupplier.get();
        } catch (Exception e) {
            log.warn("获取默认值异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查对象是否为空，如果为空则抛出异常
     *
     * @param obj 要检查的对象
     * @param fieldName 字段名称（用于异常消息）
     * @return 对象如果非空则返回对象
     * @throws IllegalArgumentException 如果对象为空
     */
    public static <T> T orElseThrow(T obj, String fieldName) {
        if (obj == null) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return obj;
    }

    /**
     * 检查对象是否为空，如果为空则执行操作并返回默认值
     *
     * @param obj 要检查的对象
     * @param action 对象为空时执行的操作
     * @param defaultValue 默认值
     * @return 对象如果非空则返回对象，否则执行操作并返回默认值
     */
    public static <T> T orElseDoAction(T obj, Runnable action, T defaultValue) {
        if (obj == null) {
            try {
                action.run();
            } catch (Exception e) {
                log.warn("执行操作异常: {}", e.getMessage());
            }
            return defaultValue;
        }
        return obj;
    }

    /**
     * 安全地获取集合大小（防止空指针）
     *
     * @param collection 集合
     * @return 集合大小，如果为空则返回0
     */
    public static int safeSize(Collection<?> collection) {
        return collection != null ? collection.size() : 0;
    }

    /**
     * 检查集合是否为空或null
     *
     * @param collection 集合
     * @return true 如果集合为null或为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 检查集合是否非空
     *
     * @param collection 集合
     * @return true 如果集合非null且非空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * 获取集合的安全第一个元素
     *
     * @param collection 集合
     * @return 第一个元素，或Optional.empty()如果集合为空
     */
    public static <T> Optional<T> safeFirst(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(collection.iterator().next());
    }

    /**
     * 安全地对对象执行转换
     *
     * @param obj 原对象
     * @param mapper 转换函数
     * @return 转换后的对象，如果原对象为null则返回null
     */
    public static <T, R> R safeMap(T obj, Function<T, R> mapper) {
        if (obj == null) {
            return null;
        }
        try {
            return mapper.apply(obj);
        } catch (Exception e) {
            log.warn("对象转换异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 安全地对对象执行转换，失败时返回默认值
     *
     * @param obj 原对象
     * @param mapper 转换函数
     * @param defaultValue 默认值
     * @return 转换后的对象，失败则返回默认值
     */
    public static <T, R> R safeMapOrElse(T obj, Function<T, R> mapper, R defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        try {
            R result = mapper.apply(obj);
            return result != null ? result : defaultValue;
        } catch (Exception e) {
            log.warn("对象转换异常: {}", e.getMessage());
            return defaultValue;
        }
    }

    /**
     * 检查字符串是否为null或空
     *
     * @param str 字符串
     * @return true 如果字符串为null或空
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 检查字符串是否非null且非空
     *
     * @param str 字符串
     * @return true 如果字符串非null且非空
     */
    public static boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 安全地获取字符串长度
     *
     * @param str 字符串
     * @return 字符串长度，如果为null则返回0
     */
    public static int safeLength(String str) {
        return str != null ? str.length() : 0;
    }

    /**
     * 创建不可修改的列表（防止意外修改）
     *
     * @param items 列表项
     * @return 不可修改的列表
     */
    @SafeVarargs
    public static <T> List<T> unmodifiableList(T... items) {
        List<T> list = new ArrayList<>();
        if (items != null) {
            for (T item : items) {
                if (item != null) {
                    list.add(item);
                }
            }
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * 创建防御性副本（深拷贝）
     *
     * @param original 原始列表
     * @return 防御性副本
     */
    public static <T> List<T> defensiveCopy(List<T> original) {
        if (original == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(original);
    }

    /**
     * 创建防御性副本（深拷贝）
     *
     * @param original 原始Map
     * @return 防御性副本
     */
    public static <K, V> Map<K, V> defensiveCopy(Map<K, V> original) {
        if (original == null) {
            return new HashMap<>();
        }
        return new HashMap<>(original);
    }

    /**
     * 如果条件为真则执行操作
     *
     * @param condition 条件
     * @param action 要执行的操作
     */
    public static void ifTrue(boolean condition, Runnable action) {
        if (condition && action != null) {
            try {
                action.run();
            } catch (Exception e) {
                log.warn("条件执行异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 如果对象非空则执行操作
     *
     * @param obj 对象
     * @param action 要执行的操作
     */
    public static <T> void ifNotNull(T obj, Runnable action) {
        if (obj != null && action != null) {
            try {
                action.run();
            } catch (Exception e) {
                log.warn("对象处理异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 如果对象为空则执行操作
     *
     * @param obj 对象
     * @param action 要执行的操作
     */
    public static <T> void ifNull(T obj, Runnable action) {
        if (obj == null && action != null) {
            try {
                action.run();
            } catch (Exception e) {
                log.warn("对象处理异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 安全的链式调用
     *
     * @param obj 初始对象
     * @param chainFunctions 链式函数
     * @return 链式调用结果
     */
    @SafeVarargs
    public static <T> T chain(T obj, Function<T, T>... chainFunctions) {
        T result = obj;
        if (chainFunctions != null) {
            for (Function<T, T> function : chainFunctions) {
                if (result == null) {
                    break;
                }
                try {
                    result = function.apply(result);
                } catch (Exception e) {
                    log.warn("链式调用异常: {}", e.getMessage());
                    return null;
                }
            }
        }
        return result;
    }

    /**
     * 获取值，如果异常则返回默认值
     *
     * @param supplier 值提供者
     * @param defaultValue 默认值
     * @return 值或默认值
     */
    public static <T> T getOrDefault(Supplier<T> supplier, T defaultValue) {
        try {
            T result = supplier.get();
            return result != null ? result : defaultValue;
        } catch (Exception e) {
            log.warn("获取值异常: {}", e.getMessage());
            return defaultValue;
        }
    }

    /**
     * 私有构造函数
     */
    private NullSafeHelper() {
        throw new IllegalStateException("不能实例化工具类");
    }
}
