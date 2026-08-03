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
    List<Order> selectByStatus(@Param("status") String status);
    List<Order> selectPage(@Param("offset") int offset, @Param("pageSize") int pageSize);
    int insert(Order order);
    int update(Order order);
    int deleteById(@Param("id") Integer id);
    long countByMerchantId(@Param("merchantId") Integer merchantId);
    long countByStatus(@Param("status") String status);
    long countAll();

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
