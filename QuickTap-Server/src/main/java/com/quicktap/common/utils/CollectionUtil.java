package com.quicktap.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 集合工具类
 *
 * 提供对集合（List、Set、Map）的常用操作
 * 包含：
 * - 集合判空和非空判断
 * - 集合元素转换和映射
 * - 集合合并和去重
 * - 集合元素按条件分组
 * - 集合排序
 * - 安全的集合访问（避免IndexOutOfBoundsException）
 *
 * 设计原则：
 * - 所有方法都进行null安全检查
 * - 避免抛出异常，返回安全的默认值
 * - 优先使用Stream API实现，可读性强
 *
 * 使用示例：
 * {@code
 * // 判空
 * List<User> users = ...;
 * if (CollectionUtil.isEmpty(users)) {
 *     return;
 * }
 *
 * // 集合转换
 * List<Integer> ids = CollectionUtil.map(users, User::getId);
 *
 * // 集合过滤
 * List<User> activeUsers = CollectionUtil.filter(users, u -> u.getStatus() == 1);
 *
 * // 集合分组
 * Map<Integer, List<User>> usersByStatus = CollectionUtil.groupBy(users, User::getStatus);
 *
 * // 集合去重
 * List<User> uniqueUsers = CollectionUtil.distinct(users, User::getId);
 *
 * // 安全获取第一个元素
 * User first = CollectionUtil.getFirst(users);
 * }
 */
@Slf4j
public class CollectionUtil {

    /**
     * 私有构造方法，防止实例化
     */
    private CollectionUtil() {
    }

    /**
     * 判断集合是否为空
     * null或size为0都返回true
     *
     * @param collection 待检查的集合
     * @return true表示集合为空或null，false表示集合非空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断集合是否非空
     * null或size为0都返回false
     *
     * @param collection 待检查的集合
     * @return true表示集合非空，false表示集合为空或null
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
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
     * 获取集合的大小
     *
     * @param collection 待测量的集合
     * @return 集合的大小，如果为null返回0
     */
    public static int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * 获取Map的大小
     *
     * @param map 待测量的Map
     * @return Map的大小，如果为null返回0
     */
    public static int size(Map<?, ?> map) {
        return map == null ? 0 : map.size();
    }

    /**
     * 将集合中的每个元素转换为新类型
     * 实现集合的元素映射（Map操作）
     *
     * @param <T> 源集合元素类型
     * @param <R> 目标集合元素类型
     * @param list 源集合
     * @param mapper 转换函数
     * @return 转换后的List，如果源集合为null返回空List
     *
     * 使用示例：
     * {@code
     * List<User> users = ...;
     * List<Integer> userIds = CollectionUtil.map(users, User::getId);
     * List<String> userNames = CollectionUtil.map(users, User::getUsername);
     * }
     */
    public static <T, R> List<R> map(Collection<T> list, Function<? super T, ? extends R> mapper) {
        if (isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * 按条件过滤集合中的元素
     *
     * @param <T> 集合元素类型
     * @param list 源集合
     * @param predicate 过滤条件
     * @return 符合条件的元素组成的List，如果源集合为null返回空List
     *
     * 使用示例：
     * {@code
     * List<User> users = ...;
     * List<User> activeUsers = CollectionUtil.filter(users, u -> u.getStatus() == 1);
     * List<User> admins = CollectionUtil.filter(users, u -> "ADMIN".equals(u.getRole()));
     * }
     */
    public static <T> List<T> filter(Collection<T> list, java.util.function.Predicate<? super T> predicate) {
        if (isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * 按指定属性对集合元素进行分组
     *
     * @param <T> 集合元素类型
     * @param <K> 分组键的类型
     * @param list 源集合
     * @param classifier 分组函数，返回元素所属的分组键
     * @return Map<K, List<T>>格式的分组结果，如果源集合为null返回空Map
     *
     * 使用示例：
     * {@code
     * List<Order> orders = ...;
     * Map<Integer, List<Order>> ordersByStatus = CollectionUtil.groupBy(orders, Order::getStatus);
     * Map<Long, List<Order>> ordersByMerchant = CollectionUtil.groupBy(orders, Order::getMerchantId);
     * }
     */
    public static <T, K> Map<K, List<T>> groupBy(Collection<T> list, Function<? super T, ? extends K> classifier) {
        if (isEmpty(list)) {
            return new HashMap<>();
        }
        return list.stream()
                .collect(Collectors.groupingBy(classifier));
    }

    /**
     * 去除集合中的重复元素（按指定属性）
     * 保留第一个出现的元素
     *
     * @param <T> 集合元素类型
     * @param <K> 用于去重的属性类型
     * @param list 源集合
     * @param keyExtractor 用于提取去重属性的函数
     * @return 去重后的List，如果源集合为null返回空List
     *
     * 使用示例：
     * {@code
     * List<User> users = ...;
     * List<User> uniqueUsers = CollectionUtil.distinct(users, User::getId);
     * List<User> uniqueByEmail = CollectionUtil.distinct(users, User::getEmail);
     * }
     */
    public static <T, K> List<T> distinct(Collection<T> list, Function<? super T, ? extends K> keyExtractor) {
        if (isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream()
                .collect(Collectors.toMap(
                        keyExtractor,
                        t -> t,
                        (first, second) -> first
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
    }

    /**
     * 获取集合的第一个元素
     *
     * @param <T> 集合元素类型
     * @param list 源集合
     * @return 第一个元素，如果集合为空返回null
     *
     * 使用示例：
     * {@code
     * List<User> users = ...;
     * User firstUser = CollectionUtil.getFirst(users);
     * if (firstUser != null) {
     *     // 处理第一个用户
     * }
     * }
     */
    public static <T> T getFirst(Collection<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        return list.stream().findFirst().orElse(null);
    }

    /**
     * 获取集合的最后一个元素
     *
     * @param <T> 集合元素类型
     * @param list 源集合
     * @return 最后一个元素，如果集合为空返回null
     */
    public static <T> T getLast(Collection<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        T last = null;
        for (T item : list) {
            last = item;
        }
        return last;
    }

    /**
     * 从List中安全地获取指定索引的元素
     *
     * @param <T> 列表元素类型
     * @param list 源列表
     * @param index 元素索引（0开始）
     * @return 指定索引的元素，如果索引越界返回null
     *
     * 使用示例：
     * {@code
     * List<User> users = ...;
     * User firstUser = CollectionUtil.get(users, 0);
     * User secondUser = CollectionUtil.get(users, 1);
     * User lastUser = CollectionUtil.get(users, users.size() - 1);
     * }
     */
    public static <T> T get(List<T> list, int index) {
        if (isEmpty(list) || index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    /**
     * 合并多个集合为一个集合
     *
     * @param <T> 集合元素类型
     * @param lists 待合并的多个集合
     * @return 合并后的List，不包含null元素
     *
     * 使用示例：
     * {@code
     * List<User> admins = ...;
     * List<User> merchants = ...;
     * List<User> all = CollectionUtil.merge(admins, merchants);
     * }
     */
    @SafeVarargs
    public static <T> List<T> merge(Collection<T>... lists) {
        List<T> result = new ArrayList<>();
        for (Collection<T> list : lists) {
            if (isNotEmpty(list)) {
                result.addAll(list);
            }
        }
        return result;
    }

    /**
     * 检查集合是否包含指定元素
     *
     * @param <T> 集合元素类型
     * @param list 源集合
     * @param element 待检查的元素
     * @return true表示集合包含该元素，false表示不包含或集合为null
     */
    public static <T> boolean contains(Collection<T> list, T element) {
        return isNotEmpty(list) && list.contains(element);
    }

    /**
     * 检查集合中是否存在满足条件的元素
     *
     * @param <T> 集合元素类型
     * @param list 源集合
     * @param predicate 检查条件
     * @return true表示集合中存在满足条件的元素，false表示不存在或集合为null
     *
     * 使用示例：
     * {@code
     * List<User> users = ...;
     * boolean hasAdmin = CollectionUtil.anyMatch(users, u -> "ADMIN".equals(u.getRole()));
     * boolean allActive = CollectionUtil.anyMatch(users, u -> u.getStatus() == 1);
     * }
     */
    public static <T> boolean anyMatch(Collection<T> list, java.util.function.Predicate<? super T> predicate) {
        return isNotEmpty(list) && list.stream().anyMatch(predicate);
    }

    /**
     * 检查集合中的所有元素是否都满足条件
     *
     * @param <T> 集合元素类型
     * @param list 源集合
     * @param predicate 检查条件
     * @return true表示所有元素都满足条件（空集合返回true），false表示存在不满足的元素或集合为null
     */
    public static <T> boolean allMatch(Collection<T> list, java.util.function.Predicate<? super T> predicate) {
        if (isEmpty(list)) {
            return true;
        }
        return list.stream().allMatch(predicate);
    }
}
