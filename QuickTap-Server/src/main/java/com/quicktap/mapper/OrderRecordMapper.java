package com.quicktap.mapper;

import com.quicktap.entity.OrderRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderRecordMapper {
    OrderRecord selectById(@Param("id") Integer id);
    OrderRecord selectByOrderNo(@Param("orderNo") String orderNo);
    List<OrderRecord> selectByMerchantId(@Param("merchantId") Integer merchantId);
    List<OrderRecord> selectAll();
    List<OrderRecord> selectPage(@Param("offset") int offset, @Param("limit") int limit);
    int insert(OrderRecord orderRecord);
    int update(OrderRecord orderRecord);
    int deleteById(@Param("id") Integer id);
    int countAll();
    int deleteExpiredOrders(@Param("beforeTime") LocalDateTime beforeTime);
}
