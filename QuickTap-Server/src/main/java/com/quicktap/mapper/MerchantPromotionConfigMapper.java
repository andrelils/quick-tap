package com.quicktap.mapper;

import com.quicktap.entity.MerchantPromotionConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MerchantPromotionConfigMapper {
    MerchantPromotionConfig selectById(@Param("id") Integer id);
    List<MerchantPromotionConfig> selectByMerchantId(@Param("merchantId") Integer merchantId);
    List<MerchantPromotionConfig> selectByMerchantIdAndStatus(@Param("merchantId") Integer merchantId, @Param("status") Integer status);
    MerchantPromotionConfig selectByMerchantIdAndPlatformId(@Param("merchantId") Integer merchantId, @Param("platformId") Integer platformId);
    List<MerchantPromotionConfig> selectAll();
    int insert(MerchantPromotionConfig config);
    int update(MerchantPromotionConfig config);
    int deleteById(@Param("id") Integer id);
    int deleteByMerchantId(@Param("merchantId") Integer merchantId);
    int countByMerchantId(@Param("merchantId") Integer merchantId);
    int countAll();
}
