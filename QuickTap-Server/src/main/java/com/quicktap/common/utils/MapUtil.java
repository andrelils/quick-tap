package com.quicktap.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Map工具类
 *
 * 提供对Map的常用操作
 * 包含：
 * - Map判空和非空判断
 * - 安全的value获取（避免null pointer）
 * - Map转换和映射
 * - Map键值互换
 * - Map过滤
 * - 常用Map操作的扩展方法
 *
 * 使用场景：
 * - API响应Map的构建
 * - 配置参数的处理
 * - 缓存数据的操作
 * - JSON解析后的Map处理
 *
 * 使用示例：
 * {@code
 * // 安全获取值
 * Map<String, Object> data = ...;
 * String username = MapUtil.getAsString(data, "username", "anonymous");
 * Integer age = MapUtil.getAsInteger(data, "age", 0);
 *
 * // 构建响应Map
 * Map<String, Object> response = MapUtil.of(
 *     "code", 0,
 *     "message", "success",
 *     "data", result
 * );
 *
 * // 过滤非null值
 * Map<String, Object> filtered = MapUtil.filterNotNull(data);
 *
 * // 键值互换
 * Map<String, String> reversed = MapUtil.reverse(statusMap);
 * }
 */
@Slf4j
public class MapUtil {

    /**
     * 私有构造方法，防止实例化
     */
    private MapUtil() {
    }

    /**
     * 判断Map是否为空
     *
     * @param map 待检查的Map
     * @return true表示Map为空或null，false表示Map非空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断Map是否非空
     *
     * @param map 待检查的Map
     * @return true表示Map非空，false表示Map为空或null
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 获取Map中指定键对应的值，如果不存在返回null
     *
     * @param <V> 值的类型
     * @param map 源Map
     * @param key 键
     * @return 对应的值，如果键不存在返回null
     */
    public static <V> V get(Map<String, V> map, String key) {
        if (isEmpty(map) || key == null) {
            return null;
        }
        return map.get(key);
    }

    /**
     * 获取Map中指定键对应的值，如果不存在返回默认值
     *
     * @param <V> 值的类型
     * @param map 源Map
     * @param key 键
     * @param defaultValue 默认值
     * @return 对应的值，如果键不存在返回默认值
     */
    public static <V> V get(Map<String, V> map, String key, V defaultValue) {
        if (isEmpty(map) || key == null) {
            return defaultValue;
        }
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * 从Map中获取String类型的值
     *
     * @param map 源Map
     * @param key 键
     * @return String值，如果键不存在或值为null返回null
     */
    public static String getAsString(Map<String, Object> map, String key) {
        if (isEmpty(map) || key == null) {
            return null;
        }
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 从Map中获取String类型的值，如果不存在返回默认值
     *
     * @param map 源Map
     * @param key 键
     * @param defaultValue 默认值
     * @return String值，如果键不存在返回默认值
     */
    public static String getAsString(Map<String, Object> map, String key, String defaultValue) {
        String value = getAsString(map, key);
        return value != null ? value : defaultValue;
    }

    /**
     * 从Map中获取Integer类型的值
     *
     * @param map 源Map
     * @param key 键
     * @return Integer值，如果键不存在或类型错误返回null
     */
    public static Integer getAsInteger(Map<String, Object> map, String key) {
        if (isEmpty(map) || key == null) {
            return null;
        }
        Object value = map.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                log.debug("无法将值转换为Integer: key={}, value={}", key, value);
            }
        }
        return null;
    }

    /**
     * 从Map中获取Integer类型的值，如果不存在返回默认值
     *
     * @param map 源Map
     * @param key 键
     * @param defaultValue 默认值
     * @return Integer值，如果键不存在或类型错误返回默认值
     */
    public static Integer getAsInteger(Map<String, Object> map, String key, Integer defaultValue) {
        Integer value = getAsInteger(map, key);
        return value != null ? value : defaultValue;
    }

    /**
     * 从Map中获取Long类型的值
     *
     * @param map 源Map
     * @param key 键
     * @return Long值，如果键不存在或类型错误返回null
     */
    public static Long getAsLong(Map<String, Object> map, String key) {
        if (isEmpty(map) || key == null) {
            return null;
        }
        Object value = map.get(key);
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                log.debug("无法将值转换为Long: key={}, value={}", key, value);
            }
        }
        return null;
    }

    /**
     * 从Map中获取Double类型的值
     *
     * @param map 源Map
     * @param key 键
     * @return Double值，如果键不存在或类型错误返回null
     */
    public static Double getAsDouble(Map<String, Object> map, String key) {
        if (isEmpty(map) || key == null) {
            return null;
        }
        Object value = map.get(key);
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                log.debug("无法将值转换为Double: key={}, value={}", key, value);
            }
        }
        return null;
    }

    /**
     * 从Map中获取Boolean类型的值
     *
     * @param map 源Map
     * @param key 键
     * @return Boolean值，如果键不存在或类型错误返回null
     */
    public static Boolean getAsBoolean(Map<String, Object> map, String key) {
        if (isEmpty(map) || key == null) {
            return null;
        }
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            String str = (String) value;
            return "true".equalsIgnoreCase(str) || "1".equals(str) || "yes".equalsIgnoreCase(str);
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return null;
    }

    /**
     * 使用可变参数快速构建Map
     * 参数必须成对出现，即 key1, value1, key2, value2, ...
     *
     * @param <K> 键的类型
     * @param <V> 值的类型
     * @param keysAndValues 键值对，必须成对出现
     * @return 构建的HashMap
     *
     * 使用示例：
     * {@code
     * Map<String, Object> map = MapUtil.of(
     *     "code", 0,
     *     "message", "success",
     *     "timestamp", System.currentTimeMillis()
     * );
     * }
     */
    public static <K, V> Map<K, V> of(Object... keysAndValues) {
        Map<K, V> map = new HashMap<>();
        if (keysAndValues.length % 2 != 0) {
            log.warn("⚠ 键值对数量不匹配，忽略最后一个键");
        }
        for (int i = 0; i < keysAndValues.length - 1; i += 2) {
            @SuppressWarnings("unchecked")
            K key = (K) keysAndValues[i];
            @SuppressWarnings("unchecked")
            V value = (V) keysAndValues[i + 1];
            map.put(key, value);
        }
        return map;
    }

    /**
     * 过滤Map中的null值，返回新Map
     *
     * @param <K> 键的类型
     * @param <V> 值的类型
     * @param map 源Map
     * @return 不包含null值的新Map
     *
     * 使用场景：
     * - API响应时移除null字段
     * - 参数过滤
     * - 数据清理
     */
    public static <K, V> Map<K, V> filterNotNull(Map<K, V> map) {
        Map<K, V> result = new HashMap<>();
        if (isNotEmpty(map)) {
            map.forEach((key, value) -> {
                if (value != null) {
                    result.put(key, value);
                }
            });
        }
        return result;
    }

    /**
     * Map键值互换（假设所有value都是String且唯一）
     *
     * @param <K> 原Map的键类型
     * @param map 源Map
     * @return 互换后的Map，键变为原Map的值，值变为原Map的键
     *
     * 使用示例：
     * {@code
     * Map<Integer, String> statusMap = Map.of(
     *     1, "enabled",
     *     0, "disabled"
     * );
     * Map<String, Integer> reversed = MapUtil.reverse(statusMap);
     * // reversed: {"enabled" -> 1, "disabled" -> 0}
     * }
     */
    public static <K, V> Map<V, K> reverse(Map<K, V> map) {
        Map<V, K> result = new HashMap<>();
        if (isNotEmpty(map)) {
            map.forEach((key, value) -> {
                if (value != null) {
                    result.put(value, key);
                }
            });
        }
        return result;
    }

    /**
     * 对Map的所有value进行转换
     *
     * @param <K> 键的类型
     * @param <V> 原值的类型
     * @param <R> 新值的类型
     * @param map 源Map
     * @param mapper 转换函数
     * @return 值被转换后的新Map
     *
     * 使用示例：
     * {@code
     * Map<String, Integer> counts = Map.of("a", 1, "b", 2);
     * Map<String, String> strings = MapUtil.mapValues(counts, String::valueOf);
     * // strings: {"a" -> "1", "b" -> "2"}
     * }
     */
    public static <K, V, R> Map<K, R> mapValues(Map<K, V> map, Function<V, R> mapper) {
        Map<K, R> result = new HashMap<>();
        if (isNotEmpty(map)) {
            map.forEach((key, value) -> result.put(key, mapper.apply(value)));
        }
        return result;
    }

    /**
     * 获取Map的大小
     *
     * @param map 源Map
     * @return Map的大小，如果为null返回0
     */
    public static int size(Map<?, ?> map) {
        return isEmpty(map) ? 0 : map.size();
    }

    /**
     * 检查Map是否包含指定的键
     *
     * @param map 源Map
     * @param key 待检查的键
     * @return true表示Map包含该键，false表示不包含或Map为null
     */
    public static boolean containsKey(Map<?, ?> map, Object key) {
        return isNotEmpty(map) && map.containsKey(key);
    }

    /**
     * 检查Map是否包含指定的值
     *
     * @param map 源Map
     * @param value 待检查的值
     * @return true表示Map包含该值，false表示不包含或Map为null
     */
    public static boolean containsValue(Map<?, ?> map, Object value) {
        return isNotEmpty(map) && map.containsValue(value);
    }
}
