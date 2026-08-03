package com.quicktap.service;

import com.quicktap.dto.MerchantPromotionConfigDTO;
import java.util.List;

/**
 * 商户推广配置Service
 */
public interface PromotionConfigService {

    /**
     * 创建推广配置
     */
    MerchantPromotionConfigDTO createConfig(Long merchantId, MerchantPromotionConfigDTO.CreateMerchantConfigRequest request);

    /**
     * 更新推广配置
     */
    MerchantPromotionConfigDTO updateConfig(Long configId, MerchantPromotionConfigDTO.UpdateMerchantConfigRequest request);

    /**
     * 删除推广配置
     */
    void deleteConfig(Long configId);

    /**
     * 获取商户的推广配置列表（激活）
     */
    List<MerchantPromotionConfigDTO> getMerchantActiveConfigs(Long merchantId);

    /**
     * 获取商户的平台推广配置列表
     */
    List<MerchantPromotionConfigDTO> getMerchantPlatformConfigs(Long merchantId);

    /**
     * 获取商户的优惠券推广配置列表
     */
    List<MerchantPromotionConfigDTO> getMerchantCouponConfigs(Long merchantId);

    /**
     * 获取配置详情
     */
    MerchantPromotionConfigDTO getConfigDetail(Long configId);

    /**
     * 启用配置
     */
    void enableConfig(Long configId);

    /**
     * 禁用配置
     */
    void disableConfig(Long configId);
}
