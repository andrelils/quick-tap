package com.quicktap.mapper;

import com.quicktap.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CouponMapper {
    Coupon selectById(@Param("id") Long id);
    List<Coupon> selectByMerchantId(@Param("merchantId") Long merchantId);
    List<Coupon> selectByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") Integer status);
    List<Coupon> selectByMerchantIdAndPage(@Param("merchantId") Long merchantId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    List<Coupon> selectPage(@Param("offset") int offset, @Param("pageSize") int pageSize);
    /**
     * 获取用户已领取的卡券列表（通过 user_coupons 关联表）
     */
    List<Coupon> selectUserCoupons(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    int insert(Coupon coupon);
    int update(Coupon coupon);
    int deleteById(@Param("id") Long id);
    long countByMerchantId(@Param("merchantId") Long merchantId);
    long countAll();
    /**
     * 统计用户已领取的卡券数量
     */
    long countUserCoupons(@Param("userId") Long userId);
    int updateRemainCountDecrement(@Param("id") Long id);
    int updateRemainCountIncrement(@Param("id") Long id);
    int updateExpiredCoupons(@Param("beforeTime") LocalDateTime beforeTime);
}
