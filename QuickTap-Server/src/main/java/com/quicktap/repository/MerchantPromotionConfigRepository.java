package com.quicktap.repository;

import com.quicktap.entity.MerchantPromotionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商户推广配置Repository
 */
@Repository
public interface MerchantPromotionConfigRepository extends JpaRepository<MerchantPromotionConfig, Long> {
    
    /**
     * 根据商户ID和状态查询配置，按排序字段排序
     */
    List<MerchantPromotionConfig> findByMerchantIdAndStatusOrderBySort(Long merchantId, Integer status);

    /**
     * 根据商户ID、类型和状态查询配置，按排序字段排序
     */
    List<MerchantPromotionConfig> findByMerchantIdAndTypeAndStatusOrderBySort(Long merchantId, String type, Integer status);

    /**
     * 根据商户ID查询所有配置
     */
    List<MerchantPromotionConfig> findByMerchantId(Long merchantId);

    /**
     * 根据类型查询配置
     */
    List<MerchantPromotionConfig> findByType(String type);
}
