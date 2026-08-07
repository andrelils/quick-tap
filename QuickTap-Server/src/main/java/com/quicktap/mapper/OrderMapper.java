package com.quicktap.mapper;

import com.quicktap.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper {
    Order selectById(@Param("id") Integer id);
    Order selectByOrderNo(@Param("orderNo") String orderNo);
    List<Order> selectByMerchantId(@Param("merchantId") Integer merchantId);
    List<Order> selectByMerchantIdAndPage(@Param("merchantId") Integer merchantId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 获取商家最近一笔已支付订单（供 my-quota 当前套餐展示开通/到期时间）
     * 返回字段: id/plan_id/created_at/expire_at
     */
    java.util.Map<String, Object> selectLatestPaidByMerchantId(@Param("merchantId") Integer merchantId);

    /**
     * 商家维度订单列表（联查套餐名，供 my-quota 购买记录展示）
     * 返回 snake_case 字段: id/order_no/plan_name/amount/status/pay_type/created_at
     */
    List<java.util.Map<String, Object>> selectMerchantOrdersWithPlan(
        @Param("merchantId") Integer merchantId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    List<Order> selectByStatus(@Param("status") String status);

    /**
     * 全量订单分页（联查商家名/套餐名，camelCase map）
     * 返回字段: id/orderNo/merchantId/merchantName/planId/planName/amount/status/payType/createTime/createdAt
     */
    List<java.util.Map<String, Object>> selectPageWithNames(@Param("offset") int offset, @Param("pageSize") int pageSize);
    List<Order> selectPage(@Param("offset") int offset, @Param("pageSize") int pageSize);
    int insert(Order order);
    int update(Order order);
    int deleteById(@Param("id") Integer id);
    long countByMerchantId(@Param("merchantId") Integer merchantId);
    long countByStatus(@Param("status") String status);
    long countAll();

    /**
     * 按条件导出订单（带商家名/套餐名）
     */
    List<java.util.Map<String, Object>> selectForExport(
        @Param("orderNo") String orderNo,
        @Param("merchantId") Integer merchantId,
        @Param("status") String status,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("limit") int limit);

    /**
     * 统计服务需要的方法 (Statistics Service)
     */

    /**
     * 获取所有订单的总金额
     */
    Double sumTotalAmount();

    /**
     * 按商户ID获取总金额
     */
    Double sumAmountByMerchantId(@Param("merchantId") Integer merchantId);

    /**
     * 按日期分组查询订单数量
     */
    List<java.util.Map<String, Object>> selectOrdersGroupedByDate(
        @Param("startDateTime") java.time.LocalDateTime startDateTime,
        @Param("endDateTime") java.time.LocalDateTime endDateTime);

    /**
     * 按日期分组查询收入
     */
    List<java.util.Map<String, Object>> selectRevenueGroupedByDate(
        @Param("startDateTime") java.time.LocalDateTime startDateTime,
        @Param("endDateTime") java.time.LocalDateTime endDateTime);
}
