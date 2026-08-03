package com.quicktap.service.impl;

import com.quicktap.entity.MerchantPromotionConfig;
import com.quicktap.dto.MerchantPromotionConfigDTO;
import com.quicktap.repository.MerchantPromotionConfigRepository;
import com.quicktap.service.PromotionConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商户推广配置Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PromotionConfigServiceImpl implements PromotionConfigService {

    private final MerchantPromotionConfigRepository promotionConfigRepository;

    @Override
    public MerchantPromotionConfigDTO createConfig(Long merchantId, MerchantPromotionConfigDTO.CreateMerchantConfigRequest request) {
        MerchantPromotionConfig config = MerchantPromotionConfig.builder()
                .merchantId(merchantId)
                .type(request.getType())
                .platformId(request.getPlatformId())
                .couponId(request.getCouponId())
                .params(request.getParams())
                .customName(request.getCustomName())
                .customIcon(request.getCustomIcon())
                .sort(request.getSort() != null ? request.getSort() : 0)
                .status(1)  // 1=启用
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        MerchantPromotionConfig saved = promotionConfigRepository.save(config);
        log.info("Created promotion config for merchant: {}, type: {}", merchantId, request.getType());
        return convertToDTO(saved);
    }

    @Override
    public MerchantPromotionConfigDTO updateConfig(Long configId, MerchantPromotionConfigDTO.UpdateMerchantConfigRequest request) {
        var config = promotionConfigRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("配置不存在"));

        if (request.getParams() != null) config.setParams(request.getParams());
        if (request.getCustomName() != null) config.setCustomName(request.getCustomName());
        if (request.getCustomIcon() != null) config.setCustomIcon(request.getCustomIcon());
        if (request.getSort() != null) config.setSort(request.getSort());
        if (request.getStatus() != null) config.setStatus(request.getStatus());
        config.setUpdatedAt(LocalDateTime.now());

        MerchantPromotionConfig saved = promotionConfigRepository.save(config);
        log.info("Updated promotion config: {}", configId);
        return convertToDTO(saved);
    }

    @Override
    public void deleteConfig(Long configId) {
        promotionConfigRepository.deleteById(configId);
        log.info("Deleted promotion config: {}", configId);
    }

    @Override
    public List<MerchantPromotionConfigDTO> getMerchantActiveConfigs(Long merchantId) {
        return promotionConfigRepository.findByMerchantIdAndStatusOrderBySort(merchantId, 1)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MerchantPromotionConfigDTO> getMerchantPlatformConfigs(Long merchantId) {
        return promotionConfigRepository.findByMerchantIdAndTypeAndStatusOrderBySort(merchantId, "platform", 1)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MerchantPromotionConfigDTO> getMerchantCouponConfigs(Long merchantId) {
        return promotionConfigRepository.findByMerchantIdAndTypeAndStatusOrderBySort(merchantId, "coupon", 1)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MerchantPromotionConfigDTO getConfigDetail(Long configId) {
        return promotionConfigRepository.findById(configId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public void enableConfig(Long configId) {
        var config = promotionConfigRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("配置不存在"));
        config.setStatus(1);
        config.setUpdatedAt(LocalDateTime.now());
        promotionConfigRepository.save(config);
        log.info("Enabled promotion config: {}", configId);
    }

    @Override
    public void disableConfig(Long configId) {
        var config = promotionConfigRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("配置不存在"));
        config.setStatus(0);
        config.setUpdatedAt(LocalDateTime.now());
        promotionConfigRepository.save(config);
        log.info("Disabled promotion config: {}", configId);
    }

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
