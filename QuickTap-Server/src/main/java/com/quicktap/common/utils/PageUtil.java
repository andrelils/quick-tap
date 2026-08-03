package com.quicktap.common.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 分页工具类
 *
 * 提供分页相关的操作和验证
 * 包含：
 * - 分页参数验证
 * - 分页参数规范化（确保在合法范围内）
 * - 计算分页偏移量（SQL的OFFSET）
 * - 计算总页数
 * - 分页参数的边界检查
 *
 * 设计理念：
 * - 默认分页大小为10
 * - 最大分页大小为100（防止一次加载过多数据）
 * - 最小分页大小为1
 * - 分页号从1开始（用户友好）
 *
 * 使用场景：
 * - 控制器参数验证
 * - 业务层分页计算
 * - 数据库查询的LIMIT和OFFSET计算
 * - API响应分页信息的生成
 *
 * 使用示例：
 * {@code
 * // 验证和规范化分页参数
 * Integer pageNum = PageUtil.validatePageNum(request.getPageNum());
 * Integer pageSize = PageUtil.validatePageSize(request.getPageSize());
 *
 * // 计算数据库查询的OFFSET
 * Integer offset = PageUtil.calculateOffset(pageNum, pageSize);
 *
 * // 计算总页数
 * Integer totalPages = PageUtil.calculateTotalPages(total, pageSize);
 *
 * // 检查页码是否有效
 * if (!PageUtil.isValidPageNum(pageNum, totalPages)) {
 *     throw new ValidationException("Invalid page number");
 * }
 * }
 */
@Slf4j
public class PageUtil {

    /**
     * 默认分页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小（防止一次加载过多数据导致内存溢出）
     */
    public static final Integer MAX_PAGE_SIZE = 100;

    /**
     * 最小分页大小
     */
    public static final Integer MIN_PAGE_SIZE = 1;

    /**
     * 默认分页号
     */
    public static final Integer DEFAULT_PAGE_NUM = 1;

    /**
     * 最小分页号
     */
    public static final Integer MIN_PAGE_NUM = 1;

    /**
     * 私有构造方法，防止实例化
     */
    private PageUtil() {
    }

    /**
     * 验证和规范化分页号
     * 如果分页号不合法，自动调整到默认值
     *
     * @param pageNum 原始分页号
     * @return 验证后的分页号（至少为1）
     *
     * 规则：
     * - null或<=0 → 返回DEFAULT_PAGE_NUM(1)
     * - >0 → 返回原值
     */
    public static Integer validatePageNum(Integer pageNum) {
        if (pageNum == null || pageNum < MIN_PAGE_NUM) {
            log.warn("⚠ 分页号不合法: {}, 使用默认值: {}", pageNum, DEFAULT_PAGE_NUM);
            return DEFAULT_PAGE_NUM;
        }
        return pageNum;
    }

    /**
     * 验证和规范化分页大小
     * 如果分页大小不合法，自动调整到合法范围
     *
     * @param pageSize 原始分页大小
     * @return 验证后的分页大小（在MIN_PAGE_SIZE到MAX_PAGE_SIZE之间）
     *
     * 规则：
     * - null或<=0 → 返回DEFAULT_PAGE_SIZE(10)
     * - >MAX_PAGE_SIZE → 返回MAX_PAGE_SIZE(100)
     * - 1-100 → 返回原值
     */
    public static Integer validatePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < MIN_PAGE_SIZE) {
            log.warn("⚠ 分页大小不合法: {}, 使用默认值: {}", pageSize, DEFAULT_PAGE_SIZE);
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            log.warn("⚠ 分页大小超过最大值: {}, 限制为: {}", pageSize, MAX_PAGE_SIZE);
            return MAX_PAGE_SIZE;
        }
        return pageSize;
    }

    /**
     * 计算SQL查询的OFFSET值
     * 用于LIMIT offset, size语句中的offset参数
     *
     * @param pageNum 分页号（从1开始）
     * @param pageSize 分页大小
     * @return OFFSET值（从0开始计算）
     *
     * 计算公式：offset = (pageNum - 1) * pageSize
     *
     * 使用示例：
     * {@code
     * // pageNum=1, pageSize=10 → offset=0（查询第1-10条记录）
     * // pageNum=2, pageSize=10 → offset=10（查询第11-20条记录）
     * // pageNum=3, pageSize=10 → offset=20（查询第21-30条记录）
     * int offset = PageUtil.calculateOffset(2, 10);  // 返回10
     * }
     */
    public static Integer calculateOffset(Integer pageNum, Integer pageSize) {
        pageNum = validatePageNum(pageNum);
        pageSize = validatePageSize(pageSize);
        return (pageNum - 1) * pageSize;
    }

    /**
     * 计算总页数
     *
     * @param total 数据总数
     * @param pageSize 分页大小
     * @return 总页数（至少为1）
     *
     * 计算公式：totalPages = (total + pageSize - 1) / pageSize
     * 等价于：totalPages = Math.ceil(total / pageSize)
     *
     * 使用示例：
     * {@code
     * // total=25, pageSize=10 → totalPages=3
     * // total=0, pageSize=10 → totalPages=1
     * // total=100, pageSize=25 → totalPages=4
     * int totalPages = PageUtil.calculateTotalPages(25, 10);  // 返回3
     * }
     */
    public static Integer calculateTotalPages(long total, Integer pageSize) {
        pageSize = validatePageSize(pageSize);
        if (total <= 0) {
            return 1;  // 即使没有数据，也至少返回1页
        }
        return (int) ((total + pageSize - 1) / pageSize);
    }

    /**
     * 检查分页号是否有效
     * 用于验证用户请求的分页号是否超出范围
     *
     * @param pageNum 分页号
     * @param totalPages 总页数
     * @return true表示分页号有效（1 <= pageNum <= totalPages）
     *
     * 使用场景：
     * - 检查用户是否请求了不存在的页面
     * - 防止用户访问超出范围的页码
     *
     * 使用示例：
     * {@code
     * // 假设总共3页
     * PageUtil.isValidPageNum(1, 3)  // true - 第1页有效
     * PageUtil.isValidPageNum(3, 3)  // true - 第3页有效
     * PageUtil.isValidPageNum(4, 3)  // false - 第4页不存在
     * PageUtil.isValidPageNum(0, 3)  // false - 分页号<1
     * }
     */
    public static boolean isValidPageNum(Integer pageNum, Integer totalPages) {
        if (pageNum == null || pageNum < MIN_PAGE_NUM) {
            return false;
        }
        if (totalPages == null || totalPages < 1) {
            totalPages = 1;
        }
        return pageNum <= totalPages;
    }

    /**
     * 判断是否需要分页
     * 当数据量小于分页大小时，不需要分页
     *
     * @param total 数据总数
     * @param pageSize 分页大小
     * @return false表示数据只有一页，true表示需要分页
     */
    public static boolean needsPaging(long total, Integer pageSize) {
        pageSize = validatePageSize(pageSize);
        return total > pageSize;
    }

    /**
     * 判断是否有下一页
     *
     * @param pageNum 当前分页号
     * @param totalPages 总页数
     * @return true表示有下一页
     */
    public static boolean hasNextPage(Integer pageNum, Integer totalPages) {
        pageNum = validatePageNum(pageNum);
        if (totalPages == null || totalPages < 1) {
            totalPages = 1;
        }
        return pageNum < totalPages;
    }

    /**
     * 判断是否有上一页
     *
     * @param pageNum 当前分页号
     * @return true表示有上一页
     */
    public static boolean hasPreviousPage(Integer pageNum) {
        pageNum = validatePageNum(pageNum);
        return pageNum > MIN_PAGE_NUM;
    }

    /**
     * 获取下一页的分页号
     *
     * @param pageNum 当前分页号
     * @param totalPages 总页数
     * @return 下一页的分页号，如果已是最后一页返回当前分页号
     */
    public static Integer getNextPageNum(Integer pageNum, Integer totalPages) {
        pageNum = validatePageNum(pageNum);
        if (totalPages == null || totalPages < 1) {
            totalPages = 1;
        }
        if (pageNum >= totalPages) {
            return pageNum;  // 已是最后一页，返回当前页
        }
        return pageNum + 1;
    }

    /**
     * 获取上一页的分页号
     *
     * @param pageNum 当前分页号
     * @return 上一页的分页号，如果已是第一页返回1
     */
    public static Integer getPreviousPageNum(Integer pageNum) {
        pageNum = validatePageNum(pageNum);
        if (pageNum <= MIN_PAGE_NUM) {
            return MIN_PAGE_NUM;  // 已是第一页，返回1
        }
        return pageNum - 1;
    }
}
