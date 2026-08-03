package com.quicktap.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * JSON工具类
 *
 * 提供JSON序列化和反序列化的便利方法
 * 基于Jackson库，支持：
 * - 对象与JSON字符串相互转换
 * - 集合和Map的JSON处理
 * - LocalDateTime等Java 8时间类型支持
 * - 异常安全处理（不抛出异常，返回默认值）
 *
 * 使用示例：
 * {@code
 * // 对象转JSON字符串
 * User user = new User(1L, "john@example.com");
 * String json = JsonUtil.toJson(user);
 *
 * // JSON字符串转对象
 * User parsedUser = JsonUtil.fromJson(json, User.class);
 *
 * // JSON字符串转List
 * List<User> users = JsonUtil.fromJsonList(jsonArray, User.class);
 *
 * // JSON字符串转Map
 * Map<String, Object> map = JsonUtil.fromJsonMap(jsonString);
 *
 * // 处理泛型集合
 * List<Map<String, Object>> data = JsonUtil.fromJson(json,
 *     new TypeReference<List<Map<String, Object>>>() {});
 * }
 *
 * 性能优化：
 * - 使用全局单例ObjectMapper实例（Jackson推荐做法）
 * - ObjectMapper已配置LocalDateTime等Java 8时间类型支持
 * - 异常处理不影响性能（日志打印时延）
 *
 * @see com.fasterxml.jackson.databind.ObjectMapper
 */
@Slf4j
public class JsonUtil {

    /**
     * 全局ObjectMapper实例
     * Jackson建议使用单例，避免频繁创建
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 配置ObjectMapper
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * 私有构造方法，防止实例化
     */
    private JsonUtil() {
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param obj 待转换的对象
     * @return JSON字符串，如果转换失败返回null并记录错误日志
     */
    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("❌ JSON序列化失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将对象转换为格式化的JSON字符串（带缩进）
     *
     * @param obj 待转换的对象
     * @return 格式化后的JSON字符串，如果转换失败返回null并记录错误日志
     */
    public static String toJsonPretty(Object obj) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("❌ JSON格式化序列化失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将JSON字符串转换为指定类型的对象
     *
     * @param <T> 目标对象类型
     * @param json JSON字符串
     * @param clazz 目标类型的Class对象
     * @return 转换后的对象，如果转换失败返回null并记录错误日志
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            log.error("❌ JSON反序列化失败，目标类型: {}, 错误: {}", clazz.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将JSON字符串转换为List集合
     *
     * @param <T> List中元素的类型
     * @param json JSON字符串（应为JSON数组格式）
     * @param elementType 集合中元素的Class对象
     * @return 转换后的List，如果转换失败返回空List（不返回null）
     */
    public static <T> List<T> fromJsonList(String json, Class<T> elementType) {
        try {
            return OBJECT_MAPPER.readValue(json,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, elementType));
        } catch (Exception e) {
            log.error("❌ JSON反序列化为List失败，元素类型: {}, 错误: {}", elementType.getName(), e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 将JSON字符串转换为Map
     *
     * @param json JSON字符串（应为JSON对象格式）
     * @return 转换后的Map<String, Object>，如果转换失败返回空Map（不返回null）
     */
    public static Map<String, Object> fromJsonMap(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.error("❌ JSON反序列化为Map失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 将JSON字符串转换为带泛型的对象
     * 用于处理复杂的泛型类型（如List<Map<String, Object>>等）
     *
     * @param <T> 目标对象类型
     * @param json JSON字符串
     * @param typeReference TypeReference实例，包含完整的泛型信息
     * @return 转换后的对象，如果转换失败返回null并记录错误日志
     *
     * 使用示例：
     * {@code
     * List<Map<String, Object>> data = JsonUtil.fromJson(json,
     *     new TypeReference<List<Map<String, Object>>>() {});
     * }
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("❌ JSON反序列化失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 检查字符串是否为有效的JSON格式
     *
     * @param json 待检查的字符串
     * @return true表示是有效JSON，false表示无效JSON
     */
    public static boolean isValidJson(String json) {
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (Exception e) {
            log.debug("字符串不是有效的JSON格式: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从JSON字符串中提取指定路径的值
     * 支持简单路径，如 "user.name"、"data[0].id"
     *
     * @param json JSON字符串
     * @param path 路径表达式
     * @return 提取的值，如果路径不存在或解析失败返回null
     */
    public static Object getValueByPath(String json, String path) {
        try {
            return OBJECT_MAPPER.readTree(json).at("/" + path.replace(".", "/")).textValue();
        } catch (Exception e) {
            log.debug("❌ JSON路径提取失败，路径: {}, 错误: {}", path, e.getMessage());
            return null;
        }
    }

    /**
     * 获取ObjectMapper实例
     * 如需自定义操作，可使用此方法获取ObjectMapper
     *
     * @return ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}
