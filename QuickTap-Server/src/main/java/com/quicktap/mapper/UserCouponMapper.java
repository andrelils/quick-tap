package com.quicktap.mapper;

import com.quicktap.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户卡券Mapper
 */
@Mapper
public interface UserCouponMapper {
    UserCoupon selectById(@Param("id") Long id);
    List<UserCoupon> selectByUserId(@Param("userId") Long userId);
    List<UserCoupon> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);
    UserCoupon selectByUserIdAndCouponId(@Param("userId") Long userId, @Param("couponId") Long couponId);
    int insert(UserCoupon userCoupon);
    int update(UserCoupon userCoupon);
    int deleteById(@Param("id") Long id);
    int deleteByUserIdAndCouponId(@Param("userId") Long userId, @Param("couponId") Long couponId);
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);
}
