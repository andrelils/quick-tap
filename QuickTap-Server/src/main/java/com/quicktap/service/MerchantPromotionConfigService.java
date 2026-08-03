package com.quicktap.service;

import com.quicktap.dto.MerchantPromotionConfigDTO;
import com.quicktap.entity.MerchantPromotionConfig;
import com.quicktap.exception.BusinessException;
import com.quicktap.mapper.MerchantPromotionConfigMapper;
import com.quicktap.security.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商户推广配置Service
 */
@Slf4j
@Service
public class MerchantPromotionConfigService {

    @Autowired
    private MerchantPromotionConfigMapper merchantPromotionConfigMapper;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 获取商户的所有推广配置
     */
    @Cacheable(value = "merchant_promotion_configs", key = "#merchantId")
    public List<MerchantPromotionConfigDTO> getMerchantConfigs(Long merchantId) {
        log.info("获取商户推广配置 | merchantId: {}", merchantId);
        List<MerchantPromotionConfig> configs = merchantPromotionConfigMapper.selectByMerchantId(merchantId.intValue());
        return configs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取商户启用的推广配置
     */
    @Cacheable(value = "merchant_promotion_configs_enabled", key = "#merchantId")
    public List<MerchantPromotionConfigDTO> getMerchantEnabledConfigs(Long merchantId) {
        log.info("获取商户启用的推广配置 | merchantId: {}", merchantId);
        List<MerchantPromotionConfig> configs = merchantPromotionConfigMapper.selectByMerchantIdAndStatus(merchantId.intValue(), 1);
        return configs.stream()
                .map(this::convertToDTO)
                .sorted((a, b) -> Integer.compare(
                        a.getSort() != null ? a.getSort() : 0,
                        b.getSort() != null ? b.getSort() : 0
                ))
                .collect(Collectors.toList());
    }

    /**
     * 获取单个配置
     */
    @Cacheable(value = "merchant_promotion_config", key = "#id")
    public MerchantPromotionConfigDTO getConfigById(Long id) {
        log.info("获取推广配置详情 | id: {}", id);
        MerchantPromotionConfig config = merchantPromotionConfigMapper.selectById(id.intValue());
        if (config == null) {
            throw new BusinessException("推广配置不存在");
        }
        return convertToDTO(config);
    }

    /**
     * 创建推广配置
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"merchant_promotion_configs", "merchant_promotion_configs_enabled"}, key = "#merchantId")
    public MerchantPromotionConfigDTO createConfig(Long merchantId, MerchantPromotionConfigDTO.CreateMerchantConfigRequest request) {
        log.info("创建推广配置 | merchantId: {} | platformId: {}", merchantId, request.getPlatformId());

        // 检查是否已配置该平台（仅对 platform 类型检查，coupon 类型跳过）
        if (request.getPlatformId() != null) {
            MerchantPromotionConfig existing = merchantPromotionConfigMapper.selectByMerchantIdAndPlatformId(merchantId.intValue(), request.getPlatformId().intValue());
            if (existing != null) {
                throw new BusinessException("该推广平台已配置，请勿重复添加");
            }
        }

        MerchantPromotionConfig config = MerchantPromotionConfig.builder()
                .merchantId(merchantId)
                .platformId(request.getPlatformId())
                .type(request.getType())
                .couponId(request.getCouponId())
                .params(request.getParams())
                .customName(request.getCustomName())
                .customIcon(request.getCustomIcon())
                .sort(request.getSort() != null ? request.getSort() : 0)
                .status(1)
                .build();

        merchantPromotionConfigMapper.insert(config);
        log.info("推广配置创建成功 | id: {}", config.getId());
        return convertToDTO(config);
    }

    /**
     * 更新推广配置
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"merchant_promotion_configs", "merchant_promotion_configs_enabled", "merchant_promotion_config"}, allEntries = true)
    public MerchantPromotionConfigDTO updateConfig(Long id, Long merchantId, MerchantPromotionConfigDTO.UpdateMerchantConfigRequest request) {
        log.info("更新推广配置 | id: {}", id);

        MerchantPromotionConfig config = merchantPromotionConfigMapper.selectById(id.intValue());
        if (config == null) {
            throw new BusinessException("推广配置不存在");
        }

        // 验证所有权
        if (!config.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权修改该推广配置");
        }

        if (request.getParams() != null) {
            config.setParams(request.getParams());
        }
        if (request.getCustomName() != null) {
            config.setCustomName(request.getCustomName());
        }
        if (request.getCustomIcon() != null) {
            config.setCustomIcon(request.getCustomIcon());
        }
        if (request.getSort() != null) {
            config.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            config.setStatus(request.getStatus());
        }

        merchantPromotionConfigMapper.update(config);
        log.info("推广配置更新成功 | id: {}", id);
        return convertToDTO(config);
    }

    /**
     * 删除推广配置
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"merchant_promotion_configs", "merchant_promotion_configs_enabled", "merchant_promotion_config"}, allEntries = true)
    public void deleteConfig(Long id, Long merchantId) {
        log.info("删除推广配置 | id: {}", id);

        MerchantPromotionConfig config = merchantPromotionConfigMapper.selectById(id.intValue());
        if (config == null) {
            throw new BusinessException("推广配置不存在");
        }

        // 验证所有权
        if (!config.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权删除该推广配置");
        }

        merchantPromotionConfigMapper.deleteById(id.intValue());
    }

    /**
     * 转换为DTO
     */
    private MerchantPromotionConfigDTO convertToDTO(MerchantPromotionConfig config) {
        return MerchantPromotionConfigDTO.builder()
                .id(config.getId())
                .merchantId(config.getMerchantId())
                .type(config.getType())
                .platformId(config.getPlatformId())
                .couponId(config.getCouponId())
                .params(config.getParams())
                .customName(config.getCustomName())
                .customIcon(config.getCustomIcon())
                .sort(config.getSort())
                .status(config.getStatus())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
