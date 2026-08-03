package com.quicktap.service;

import com.quicktap.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析服务 - 系统数据分析和业务报表生成
 *
 * 职责：
 * - 收集和聚合系统级别的关键指标（KPI）
 * - 生成商户级别的业务统计报表
 * - 提供趋势分析和时间序列数据
 * - 计算排名和性能指标
 * - 统计 AI 生成使用情况
 * - 支持管理员仪表板和数据可视化
 *
 * 核心指标维度：
 * - 商户数：系统中活跃商户总数
 * - 用户数：全系统注册用户总数
 * - 订单数：全系统交易订单总数
 * - 收入：全系统销售收入总和（元）
 * - AI生成：文本、图片、视频生成的累计次数
 * - 设备数：商户关联的设备总数
 *
 * 核心方法：
 * - getSystemOverview() - 获取系统全局概览（四大关键指标）
 * - getTrendData(LocalDate, LocalDate) - 获取时间范围内的趋势数据
 * - getMerchantStatistics(Integer) - 获取单个商户的详细统计
 * - getTopMerchants(Integer) - 获取排名前N的商户（按收入、订单数）
 * - getTotalAIGenerations() - 获取 AI 生成的总次数
 * - getAIGenerationsByType() - 按类型统计 AI 生成（含占比）
 * - getMerchantAIUsage() - 获取商户级别的 AI 使用情况
 *
 * 系统概览数据结构：
 * {@code
 * {
 *   "totalMerchants": 245,         // 商户总数
 *   "totalUsers": 15320,           // 用户总数
 *   "totalOrders": 98456,          // 订单总数
 *   "totalRevenue": 2450000.50     // 收入总和（元）
 * }</n
 *
 * 趋势数据结构：
 * {@code
 * {
 *   "startDate": "2024-07-01",
 *   "endDate": "2024-07-31",
 *   "dates": ["2024-07-01", "2024-07-02", ...],
 *   "orders": [45, 52, 48, ...],      // 每日订单数
 *   "revenue": [12000.5, 13200.0, ...], // 每日收入
 *   "newUsers": [125, 98, 142, ...]   // 每日新增用户
 * }</n
 *
 * 商户统计数据结构：
 * {@code
 * {
 *   "merchantId": 123,
 *   "orders": 5432,                // 该商户订单数
 *   "revenue": 545000.75,          // 该商户收入
 *   "devices": 25,                 // 该商户关联设备数
 *   "users": 2340,                 // 该商户用户数
 *   "status": 1                    // 商户状态（1=正常，0=禁用）
 * }</n
 *
 * 排名商户数据结构：
 * {@code
 * {
 *   "limit": 10,
 *   "topByRevenue": [              // 按收入排序的商户
 *     {
 *       "merchantId": 1,
 *       "name": "商户A",
 *       "revenue": 1000000.0,
 *       "orders": 5000
 *     },
 *     ...
 *   ],
 *   "topByOrders": [               // 按订单数排序的商户
 *     {
 *       "merchantId": 2,
 *       "name": "商户B",
 *       "orders": 8000,
 *       "revenue": 800000.0
 *     },
 *     ...
 *   ]
 * }</n
 *
 * AI生成统计数据结构：
 * {@code
 * {
 *   "total": 125000,               // AI生成总次数
 *   "text": {
 *     "count": 60000,              // 文本生成次数
 *     "percentage": 48.0            // 占比 48%
 *   },
 *   "image": {
 *     "count": 40000,              // 图片生成次数
 *     "percentage": 32.0            // 占比 32%
 *   },
 *   "video": {
 *     "count": 25000,              // 视频生成次数
 *     "percentage": 20.0            // 占比 20%
 *   }
 * }</n
 *
 * 数据收集策略：
 * - 所有统计数据通过数据库聚合查询获取
 * - 不在应用层进行内存聚合，确保性能
 * - 设置安全限制（limit最多1000）防止内存溢出
 * - 异常情况返回空值或0，不中断业务
 *
 * 时间处理：
 * - 支持自定义时间范围统计
 * - 默认时间范围：过去30天
 * - 时间粒度：按日期统计
 * - 日期格式：ISO 8601 (YYYY-MM-DD)
 *
 * 使用场景：
 * {@code
 * // 场景1: 获取管理员仪表板数据
 * Map<String, Object> overview = statisticsService.getSystemOverview();
 * // 显示系统的关键指标
 *
 * // 场景2: 生成业务报表（过去30天）
 * LocalDate startDate = LocalDate.now().minusDays(30);
 * LocalDate endDate = LocalDate.now();
 * Map<String, Object> trend = statisticsService.getTrendData(startDate, endDate);
 * // 返回时间序列数据用于图表展示
 *
 * // 场景3: 商户业绩查询
 * Map<String, Object> stats = statisticsService.getMerchantStatistics(merchantId);
 * // 显示该商户的订单、收入、设备、用户等数据
 *
 * // 场景4: 排行榜展示（前10名商户）
 * Map<String, Object> topMerchants = statisticsService.getTopMerchants(10);
 * // 显示收入最高和订单最多的商户
 *
 * // 场景5: AI生成统计
 * Map<String, Object> aiStats = statisticsService.getAIGenerationsByType();
 * // 显示AI生成的类型分布（文本/图片/视频）
 * }</n
 *
 * 错误处理：
 * - 数据库查询异常不抛出异常，返回默认值（0或空列表）
 * - 记录详细的错误日志便于问题排查
 * - 确保统计服务故障不影响主业务流程
 *
 * 性能优化：
 * - 使用数据库聚合函数（COUNT、SUM、GROUP BY）
 * - 避免加载全量数据到应用层
 * - 对时间范围的查询应有数据库索引
 * - 考虑缓存热点数据（如系统概览）
 *
 * 数据库交互：
 * - MerchantMapper.countAll() - 商户总数
 * - UserMapper.countAll() - 用户总数
 * - OrderMapper.countAll() - 订单总数
 * - OrderMapper.sumTotalAmount() - 收入总和
 * - OrderMapper.selectOrdersGroupedByDate() - 按日期分组的订单
 * - OrderMapper.selectRevenueGroupedByDate() - 按日期分组的收入
 * - UserMapper.selectNewUsersGroupedByDate() - 按日期分组的新用户
 * - MerchantMapper.selectTopMerchantsByRevenue() - 按收入排序的商户
 * - MerchantMapper.selectTopMerchantsByOrders() - 按订单排序的商户
 * - AiGenerateRecordMapper.countByStatus() - 按状态统计生成记录
 * - AiGenerateRecordMapper.countByTypeAndStatus() - 按类型和状态统计
 *
 * 日志级别：
 * - INFO: 正常统计操作（系统概览、商户排名等）
 * - ERROR: 统计查询失败
 *
 * 集成点：
 * - DashboardController: 管理员仪表板API
 * - ReportController: 业务报表API
 * - MerchantService: 商户统计查询
 * - AnalyticsService: 数据分析和趋势预测
 *
 * @author QuickTap Analytics Team
 * @version 1.0
 * @since 1.0
 * @see MerchantMapper
 * @see UserMapper
 * @see OrderMapper
 * @see DeviceMapper
 * @see AiGenerateRecordMapper
 */
@Slf4j
@Service
public class StatisticsService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private AiGenerateRecordMapper aiGenerateRecordMapper;

    /**
     * 获取系统概览 (TODO 5)
     */
    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();

        try {
            long totalMerchants = merchantMapper.countAll();
            long totalUsers = userMapper.countAll();
            long totalOrders = orderMapper.countAll();
            Double totalRevenue = orderMapper.sumTotalAmount();
            if (totalRevenue == null) {
                totalRevenue = 0.0;
            }

            overview.put("totalMerchants", totalMerchants);
            overview.put("totalUsers", totalUsers);
            overview.put("totalOrders", totalOrders);
            overview.put("totalRevenue", totalRevenue);

            log.info("📊 系统概览: 商户={}, 用户={}, 订单={}, 收入={}",
                totalMerchants, totalUsers, totalOrders, totalRevenue);
        } catch (Exception e) {
            log.error("❌ 获取系统概览失败: {}", e.getMessage(), e);
            // 返回空数据而不是抛异常
            overview.put("totalMerchants", 0);
            overview.put("totalUsers", 0);
            overview.put("totalOrders", 0);
            overview.put("totalRevenue", 0.0);
        }

        return overview;
    }

    /**
     * 获取趋势数据 (TODO 6)
     */
    public Map<String, Object> getTrendData(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> trend = new HashMap<>();

        try {
            if (startDate == null || endDate == null) {
                startDate = LocalDate.now().minusDays(30);
                endDate = LocalDate.now();
            }

            // 查询按日期分组的订单数
            List<Map<String, Object>> ordersByDate = orderMapper
                .selectOrdersGroupedByDate(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

            // 查询按日期分组的收入
            List<Map<String, Object>> revenueByDate = orderMapper
                .selectRevenueGroupedByDate(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

            // 查询按日期分组的新用户
            List<Map<String, Object>> newUsersByDate = userMapper
                .selectNewUsersGroupedByDate(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

            // 提取和格式化数据
            List<String> dates = new ArrayList<>();
            List<Integer> orders = new ArrayList<>();
            List<Double> revenue = new ArrayList<>();
            List<Integer> newUsers = new ArrayList<>();

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                dates.add(date.toString());

                // 查找该日期的订单数据
                LocalDate finalDate = date;
                Map<String, Object> orderData = ordersByDate.stream()
                    .filter(m -> m.get("date").equals(java.sql.Date.valueOf(finalDate)))
                    .findFirst()
                    .orElse(Map.of("count", 0));
                orders.add(((Number) orderData.get("count")).intValue());

                // 查找该日期的收入数据
                Map<String, Object> revenueData = revenueByDate.stream()
                    .filter(m -> m.get("date").equals(java.sql.Date.valueOf(finalDate)))
                    .findFirst()
                    .orElse(Map.of("total", 0.0));
                revenue.add(((Number) revenueData.get("total")).doubleValue());

                // 查找该日期的新用户数据
                Map<String, Object> userData = newUsersByDate.stream()
                    .filter(m -> m.get("date").equals(java.sql.Date.valueOf(finalDate)))
                    .findFirst()
                    .orElse(Map.of("count", 0));
                newUsers.add(((Number) userData.get("count")).intValue());
            }

            trend.put("startDate", startDate.toString());
            trend.put("endDate", endDate.toString());
            trend.put("dates", dates);
            trend.put("orders", orders);
            trend.put("revenue", revenue);
            trend.put("newUsers", newUsers);

            int totalOrders = orders.stream().mapToInt(Integer::intValue).sum();
            double totalRevenue = revenue.stream().mapToDouble(Double::doubleValue).sum();
            log.info("📈 趋势数据: 时间范围={}到{}, 订单数={}, 总收入={}",
                startDate, endDate, totalOrders, totalRevenue);
        } catch (Exception e) {
            log.error("❌ 获取趋势数据失败: {}", e.getMessage(), e);
            trend.put("dates", new ArrayList<>());
            trend.put("orders", new ArrayList<>());
            trend.put("revenue", new ArrayList<>());
            trend.put("newUsers", new ArrayList<>());
        }

        return trend;
    }

    /**
     * 获取商户统计 (TODO 7)
     */
    public Map<String, Object> getMerchantStatistics(Integer merchantId) {
        Map<String, Object> stats = new HashMap<>();

        try {
            long merchantOrders = orderMapper.countByMerchantId(merchantId);
            Double merchantRevenue = orderMapper.sumAmountByMerchantId(merchantId);
            if (merchantRevenue == null) {
                merchantRevenue = 0.0;
            }
            long merchantDevices = deviceMapper.countByMerchantId(merchantId);
            long merchantUsers = userMapper.countByMerchantId(merchantId);
            Integer merchantStatus = merchantMapper.getStatusByMerchantId(merchantId);

            stats.put("merchantId", merchantId);
            stats.put("orders", merchantOrders);
            stats.put("revenue", merchantRevenue);
            stats.put("devices", merchantDevices);
            stats.put("users", merchantUsers);
            stats.put("status", merchantStatus);

            log.info("🏪 商户统计: merchantId={}, 订单={}, 收入={}, 设备={}, 用户={}",
                merchantId, merchantOrders, merchantRevenue, merchantDevices, merchantUsers);
        } catch (Exception e) {
            log.error("❌ 获取商户统计失败 (merchantId={}): {}", merchantId, e.getMessage(), e);
            stats.put("merchantId", merchantId);
            stats.put("orders", 0);
            stats.put("revenue", 0.0);
            stats.put("devices", 0);
            stats.put("users", 0);
            stats.put("status", null);
        }

        return stats;
    }

    /**
     * 获取排名前N的商户 (TODO 8)
     */
    public Map<String, Object> getTopMerchants(Integer limit) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (limit == null || limit <= 0) {
                limit = 10;
            }
            if (limit > 1000) {
                limit = 1000; // 安全限制
            }

            // 按收入排序的商户
            List<Map<String, Object>> topByRevenue = merchantMapper
                .selectTopMerchantsByRevenue(limit);

            // 按订单数排序的商户
            List<Map<String, Object>> topByOrders = merchantMapper
                .selectTopMerchantsByOrders(limit);

            result.put("limit", limit);
            result.put("topByRevenue", topByRevenue);
            result.put("topByOrders", topByOrders);

            log.info("🏆 商户排名: 按收入排名 TOP {}, 按订单排名 TOP {}", limit, limit);
        } catch (Exception e) {
            log.error("❌ 获取商户排名失败: {}", e.getMessage(), e);
            result.put("limit", limit != null ? limit : 10);
            result.put("topByRevenue", new ArrayList<>());
            result.put("topByOrders", new ArrayList<>());
        }

        return result;
    }

    /**
     * 获取总 AI 生成次数 (TODO 9)
     */
    public Integer getTotalAIGenerations() {
        try {
            // 查询状态为1（成功）的生成记录
            long totalGenerations = aiGenerateRecordMapper.countByStatus(1);
            log.info("🤖 AI生成总数: {}", totalGenerations);
            return (int) totalGenerations;
        } catch (Exception e) {
            log.error("❌ 获取AI生成总数失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 按类型统计 AI 生成 (TODO 10)
     */
    public Map<String, Object> getAIGenerationsByType() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 按类型和状态统计
            long textCount = aiGenerateRecordMapper.countByTypeAndStatus("text", 1);
            long imageCount = aiGenerateRecordMapper.countByTypeAndStatus("image", 1);
            long videoCount = aiGenerateRecordMapper.countByTypeAndStatus("video", 1);

            long totalCount = textCount + imageCount + videoCount;

            // 文本统计
            Map<String, Object> textStats = new HashMap<>();
            textStats.put("count", textCount);
            textStats.put("percentage", totalCount > 0 ? (textCount * 100.0 / totalCount) : 0);

            // 图片统计
            Map<String, Object> imageStats = new HashMap<>();
            imageStats.put("count", imageCount);
            imageStats.put("percentage", totalCount > 0 ? (imageCount * 100.0 / totalCount) : 0);

            // 视频统计
            Map<String, Object> videoStats = new HashMap<>();
            videoStats.put("count", videoCount);
            videoStats.put("percentage", totalCount > 0 ? (videoCount * 100.0 / totalCount) : 0);

            result.put("total", totalCount);
            result.put("text", textStats);
            result.put("image", imageStats);
            result.put("video", videoStats);

            log.info("🎨 AI生成统计: 文本={}, 图片={}, 视频={}, 总计={}",
                textCount, imageCount, videoCount, totalCount);
        } catch (Exception e) {
            log.error("❌ 获取AI生成类型统计失败: {}", e.getMessage(), e);
            result.put("total", 0);
            result.put("text", Map.of("count", 0, "percentage", 0));
            result.put("image", Map.of("count", 0, "percentage", 0));
            result.put("video", Map.of("count", 0, "percentage", 0));
        }

        return result;
    }

    /**
     * 获取商户 AI 使用情况
     */
    public List<Map<String, Object>> getMerchantAIUsage() {
        try {
            // TODO: 返回每个商户的 AI 使用次数和配额
            log.info("📊 获取商户AI使用情况");
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("❌ 获取商户AI使用情况失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
