package com.quicktap.service;

import com.quicktap.dto.PromotionPlatformDTO;
import com.quicktap.entity.PromotionPlatform;
import com.quicktap.exception.BusinessException;
import com.quicktap.mapper.PromotionPlatformMapper;
import com.quicktap.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 推广平台Service
 */
@Slf4j
@Service
public class PromotionPlatformService {

    @Autowired
    private PromotionPlatformMapper promotionPlatformMapper;

    /**
     * 获取所有启用的平台
     */
    @Cacheable(value = "promotion_platforms", key = "'all_enabled'")
    public List<PromotionPlatformDTO> getAllEnabledPlatforms() {
        log.info("获取所有启用的推广平台");
        List<PromotionPlatform> platforms = promotionPlatformMapper.selectByStatus(1);
        return platforms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取平台详情
     */
    @Cacheable(value = "promotion_platform", key = "#id")
    public PromotionPlatformDTO getPlatformById(Long id) {
        log.info("获取推广平台详情 | id: {}", id);
        PromotionPlatform platform = promotionPlatformMapper.selectById(id.intValue());
        if (platform == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "推广平台不存在");
        }
        return convertToDTO(platform);
    }

    /**
     * 获取平台（按编码）
     */
    @Cacheable(value = "promotion_platform", key = "#code")
    public PromotionPlatformDTO getPlatformByCode(String code) {
        log.info("获取推广平台 | code: {}", code);
        PromotionPlatform platform = promotionPlatformMapper.selectByCode(code);
        if (platform == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "推广平台不存在");
        }
        return convertToDTO(platform);
    }

    /**
     * 创建推广平台（超管操作）
     */
    @CacheEvict(value = "promotion_platforms", key = "'all_enabled'")
    public PromotionPlatformDTO createPlatform(PromotionPlatformDTO.CreatePromotionPlatformRequest request) {
        log.info("创建推广平台 | code: {}", request.getCode());

        // 检查编码是否重复
        PromotionPlatform existing = promotionPlatformMapper.selectByCode(request.getCode());
        if (existing != null) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "平台编码已存在");
        }

        PromotionPlatform platform = PromotionPlatform.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIconUrl())
                .color(request.getColor())
                .jumpMode(request.getJumpMode())
                .schemeTemplate(request.getSchemeTemplate())
                .webUrlTemplate(request.getWebUrlTemplate())
                .miniprogramAppid(request.getMiniprogramAppid())
                .miniprogramPathTemplate(request.getMiniprogramPathTemplate())
                .sort(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .status(1)
                .requiredParams(request.getRequiredParams())
                .optionalParams(request.getOptionalParams())
                .build();

        promotionPlatformMapper.insert(platform);
        log.info("推广平台创建成功 | id: {}", platform.getId());
        return convertToDTO(platform);
    }

    /**
     * 更新推广平台（超管操作）
     */
    @CacheEvict(value = {"promotion_platforms", "promotion_platform"}, allEntries = true)
    public PromotionPlatformDTO updatePlatform(Long id, PromotionPlatformDTO.UpdatePromotionPlatformRequest request) {
        log.info("更新推广平台 | id: {}", id);

        PromotionPlatform platform = promotionPlatformMapper.selectById(id.intValue());
        if (platform == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "推广平台不存在");
        }

        if (request.getName() != null) {
            platform.setName(request.getName());
        }
        if (request.getDescription() != null) {
            platform.setDescription(request.getDescription());
        }
        if (request.getIconUrl() != null) {
            platform.setIcon(request.getIconUrl());
        }
        if (request.getSortOrder() != null) {
            platform.setSort(request.getSortOrder());
        }
        if (request.getEnabled() != null) {
            platform.setStatus(request.getEnabled() ? 1 : 0);
        }
        if (request.getRequiredParams() != null) {
            platform.setRequiredParams(request.getRequiredParams());
        }
        if (request.getOptionalParams() != null) {
            platform.setOptionalParams(request.getOptionalParams());
        }
        if (request.getColor() != null) {
            platform.setColor(request.getColor());
        }
        if (request.getJumpMode() != null) {
            platform.setJumpMode(request.getJumpMode());
        }
        if (request.getSchemeTemplate() != null) {
            platform.setSchemeTemplate(request.getSchemeTemplate());
        }
        if (request.getWebUrlTemplate() != null) {
            platform.setWebUrlTemplate(request.getWebUrlTemplate());
        }
        if (request.getMiniprogramAppid() != null) {
            platform.setMiniprogramAppid(request.getMiniprogramAppid());
        }
        if (request.getMiniprogramPathTemplate() != null) {
            platform.setMiniprogramPathTemplate(request.getMiniprogramPathTemplate());
        }

        promotionPlatformMapper.update(platform);
        log.info("推广平台更新成功 | id: {}", id);
        return convertToDTO(platform);
    }

    /**
     * 删除推广平台（超管操作）
     */
    @CacheEvict(value = {"promotion_platforms", "promotion_platform"}, allEntries = true)
    public void deletePlatform(Long id) {
        log.info("删除推广平台 | id: {}", id);
        promotionPlatformMapper.deleteById(id.intValue());
    }

    /**
     * 转换为DTO
     */
    private PromotionPlatformDTO convertToDTO(PromotionPlatform platform) {
        return PromotionPlatformDTO.builder()
                .id(platform.getId() != null ? platform.getId().longValue() : null)
                .code(platform.getCode())
                .name(platform.getName())
                .description(platform.getDescription())
                .iconUrl(platform.getIcon())
                .sortOrder(platform.getSort())
                .enabled(platform.getStatus() != null && platform.getStatus() == 1)
                .requiredParams(platform.getRequiredParams())
                .optionalParams(platform.getOptionalParams())
                .color(platform.getColor())
                .jumpMode(platform.getJumpMode())
                .schemeTemplate(platform.getSchemeTemplate())
                .webUrlTemplate(platform.getWebUrlTemplate())
                .miniprogramAppid(platform.getMiniprogramAppid())
                .miniprogramPathTemplate(platform.getMiniprogramPathTemplate())
                .build();
    }
}
