package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.dto.PageResponse;
import com.quicktap.dto.PromotionPlatformDTO;
import com.quicktap.dto.MerchantPromotionConfigDTO;
import com.quicktap.entity.PromotionPlatform;
import com.quicktap.entity.MerchantPromotionConfig;
import com.quicktap.mapper.PromotionPlatformMapper;
import com.quicktap.mapper.MerchantPromotionConfigMapper;
import com.quicktap.security.SecurityUtil;
import com.quicktap.service.PromotionPlatformService;
import com.quicktap.service.MerchantPromotionConfigService;
import com.quicktap.exception.BusinessException;
import com.quicktap.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 推广平台Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/promotion")
public class PromotionController {

    @Autowired
    private PromotionPlatformService promotionPlatformService;

    @Autowired
    private MerchantPromotionConfigService merchantPromotionConfigService;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private PromotionPlatformMapper promotionPlatformMapper;

    /**
     * 获取推广平台列表（别名路由，兼容管理员端调用）
     * - 支持分页参数 pageNum/pageSize（有分页时返回 PageResponse，无分页时返回全量 List）
     * 对应 Node: GET /api/admin/promotion/platforms
     */
    @GetMapping("/admin/promotion/platforms")
    public ApiResponse<?> getAdminPlatforms(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) {
        log.info("获取推广平台列表(管理员): pageNum={}, pageSize={}", pageNum, pageSize);

        // 如果传了分页参数，走分页查询（PageResponse 结构）
        if (pageNum != null && pageSize != null && pageNum > 0 && pageSize > 0) {
            int offset = (pageNum - 1) * pageSize;
            List<PromotionPlatform> platforms = promotionPlatformMapper.selectPage(offset, pageSize);
            long total = promotionPlatformMapper.countAll();
            List<PromotionPlatformDTO> dtoList = platforms.stream()
                    .map(this::convertPlatformToDTO)
                    .collect(Collectors.toList());
            PageResponse<PromotionPlatformDTO> page = PageResponse.of(dtoList, pageNum, pageSize, total);
            return ApiResponse.success("获取推广平台成功", page);
        }

        // 否则返回全量启用的平台列表
        List<PromotionPlatformDTO> platforms = promotionPlatformService.getAllEnabledPlatforms();
        return ApiResponse.success("获取推广平台成功", platforms);
    }

    /**
     * 获取商户的推广配置列表（别名路由）
     * 对应 Node: GET /api/merchant/promotion/configs
     */
    @GetMapping("/merchant/promotion/configs")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<List<MerchantPromotionConfigDTO>> getMerchantPromotionConfigs(
            @RequestParam(required = false) Integer merchantId,
            @RequestParam(required = false) String type) {
        // 确定查询的 merchantId：参数 > token
        Long targetMerchantId;
        if (merchantId != null && merchantId > 0) {
            targetMerchantId = merchantId.longValue();
        } else {
            targetMerchantId = securityUtil.getCurrentMerchantId();
        }
        log.info("获取商户推广配置(别名) | merchantId: {}, type: {}", targetMerchantId, type);

        List<MerchantPromotionConfigDTO> configs;
        if (targetMerchantId != null && targetMerchantId > 0) {
            // 指定商户时，通过 Service 查询（带缓存）并在内存过滤 type
            List<MerchantPromotionConfigDTO> list = merchantPromotionConfigService.getMerchantConfigs(targetMerchantId);
            configs = list.stream()
                    .filter(c -> type == null || type.isEmpty() || type.equalsIgnoreCase(c.getType()))
                    .collect(Collectors.toList());
        } else {
            // 无有效 merchantId 时返回空列表（避免页面报错）
            configs = new ArrayList<>();
        }
        return ApiResponse.success("获取商户推广配置成功", configs);
    }

    // ==================== 原有推广平台接口（保持不变以兼容性） ====================

    /**
     * 获取推广平台列表
     * - 支持分页参数 pageNum/pageSize（有分页时返回 PageResponse，无分页时返回全量 List）
     * - 用于 getPlatformList（分页）和 getAllPlatforms/getAvailablePlatforms（全量）
     */
    @GetMapping("/platforms")
    public ApiResponse<?> getPlatforms(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) {
        log.info("获取推广平台列表: pageNum={}, pageSize={}", pageNum, pageSize);

        // 如果传了分页参数，走分页查询（PageResponse 结构）
        if (pageNum != null && pageSize != null && pageNum > 0 && pageSize > 0) {
            int offset = (pageNum - 1) * pageSize;
            List<PromotionPlatform> platforms = promotionPlatformMapper.selectPage(offset, pageSize);
            long total = promotionPlatformMapper.countAll();
            List<PromotionPlatformDTO> dtoList = platforms.stream()
                    .map(this::convertPlatformToDTO)
                    .collect(Collectors.toList());
            PageResponse<PromotionPlatformDTO> page = PageResponse.of(dtoList, pageNum, pageSize, total);
            return ApiResponse.success("获取推广平台成功", page);
        }

        // 否则返回全量启用的平台列表
        List<PromotionPlatformDTO> platforms = promotionPlatformService.getAllEnabledPlatforms();
        return ApiResponse.success("获取推广平台成功", platforms);
    }

    /**
     * 获取推广平台详情
     */
    @GetMapping("/platforms/{id}")
    public ApiResponse<PromotionPlatformDTO> getPlatformById(@PathVariable Long id) {
        log.info("获取推广平台详情 | id: {}", id);
        PromotionPlatformDTO platform = promotionPlatformService.getPlatformById(id);
        return ApiResponse.success("获取推广平台详情成功", platform);
    }

    /**
     * 创建推广平台（超管操作）
     */
    @PostMapping("/platforms")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<PromotionPlatformDTO> createPlatform(@Valid @RequestBody PromotionPlatformDTO.CreatePromotionPlatformRequest request) {
        log.info("创建推广平台 | code: {}", request.getCode());
        PromotionPlatformDTO platform = promotionPlatformService.createPlatform(request);
        return ApiResponse.success("推广平台创建成功", platform);
    }

    /**
     * 更新推广平台（超管操作）
     */
    @PutMapping("/platforms/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<PromotionPlatformDTO> updatePlatform(@PathVariable Long id, @Valid @RequestBody PromotionPlatformDTO.UpdatePromotionPlatformRequest request) {
        log.info("更新推广平台 | id: {}", id);
        PromotionPlatformDTO platform = promotionPlatformService.updatePlatform(id, request);
        return ApiResponse.success("推广平台更新成功", platform);
    }

    /**
     * 删除推广平台（超管操作）
     */
    @DeleteMapping("/platforms/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> deletePlatform(@PathVariable Long id) {
        log.info("删除推广平台 | id: {}", id);
        promotionPlatformService.deletePlatform(id);
        return ApiResponse.success("推广平台删除成功", null);
    }

    // ==================== 商户推广配置接口 ====================

    /**
     * 获取商户的推广配置列表
     * - ADMIN/SUPER_ADMIN 视角可通过 merchantId 参数指定查询的商户
     * - MERCHANT 视角从当前登录态取 merchantId
     * - 支持 type 参数按类型过滤（platform/coupon）
     */
    @GetMapping("/merchant-configs")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<List<MerchantPromotionConfigDTO>> getMerchantConfigs(
            @RequestParam(required = false) Integer merchantId,
            @RequestParam(required = false) String type) {
        // 确定查询的 merchantId：参数 > token
        Long targetMerchantId;
        if (merchantId != null && merchantId > 0) {
            targetMerchantId = merchantId.longValue();
        } else {
            targetMerchantId = securityUtil.getCurrentMerchantId();
        }
        log.info("获取商户推广配置 | merchantId: {}, type: {}", targetMerchantId, type);

        List<MerchantPromotionConfigDTO> configs;
        if (targetMerchantId != null && targetMerchantId > 0) {
            // 指定商户时，通过 Service 查询（带缓存）并在内存过滤 type
            List<MerchantPromotionConfigDTO> list = merchantPromotionConfigService.getMerchantConfigs(targetMerchantId);
            configs = list.stream()
                    .filter(c -> type == null || type.isEmpty() || type.equalsIgnoreCase(c.getType()))
                    .collect(Collectors.toList());
        } else {
            // 无有效 merchantId 时返回空列表（避免页面报错）
            configs = new ArrayList<>();
        }
        return ApiResponse.success("获取商户推广配置成功", configs);
    }

    /**
     * 获取商户的启用推广配置
     */
    @GetMapping("/merchant-configs/enabled")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<List<MerchantPromotionConfigDTO>> getMerchantEnabledConfigs() {
        Long merchantId = securityUtil.getCurrentMerchantId();
        log.info("获取商户启用的推广配置 | merchantId: {}", merchantId);
        List<MerchantPromotionConfigDTO> configs = merchantPromotionConfigService.getMerchantEnabledConfigs(merchantId);
        return ApiResponse.success("获取商户启用推广配置成功", configs);
    }

    /**
     * 获取商户的单个推广配置详情
     */
    @GetMapping("/merchant-configs/{id}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<MerchantPromotionConfigDTO> getMerchantConfigById(@PathVariable Long id) {
        log.info("获取商户推广配置详情 | id: {}", id);
        MerchantPromotionConfigDTO config = merchantPromotionConfigService.getConfigById(id);
        return ApiResponse.success("获取推广配置详情成功", config);
    }

    /**
     * 添加推广平台配置
     */
    @PostMapping("/merchant-configs")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<MerchantPromotionConfigDTO> addMerchantConfig(@Valid @RequestBody MerchantPromotionConfigDTO.CreateMerchantConfigRequest request) {
        Long merchantId = resolveMerchantId(request.getMerchantId());
        if (merchantId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "缺少商户ID，请先选择商家");
        }
        log.info("添加商户推广配置 | merchantId: {} | platformId: {}", merchantId, request.getPlatformId());
        MerchantPromotionConfigDTO config = merchantPromotionConfigService.createConfig(merchantId, request);
        return ApiResponse.success("推广配置添加成功", config);
    }

    /**
     * 更新推广平台配置
     */
    @PutMapping("/merchant-configs/{id}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<MerchantPromotionConfigDTO> updateMerchantConfig(@PathVariable Long id, @Valid @RequestBody MerchantPromotionConfigDTO.UpdateMerchantConfigRequest request) {
        Long merchantId = resolveMerchantId(request.getMerchantId());
        log.info("更新商户推广配置 | id: {} | merchantId: {}", id, merchantId);
        MerchantPromotionConfigDTO config = merchantPromotionConfigService.updateConfig(id, merchantId, request);
        return ApiResponse.success("推广配置更新成功", config);
    }

    /**
     * 删除推广平台配置
     */
    @DeleteMapping("/merchant-configs/{id}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Void> deleteMerchantConfig(@PathVariable Long id,
                                                  @RequestParam(required = false) Long merchantId) {
        Long targetMerchantId = resolveMerchantId(merchantId);
        log.info("删除商户推广配置 | id: {} | merchantId: {}", id, targetMerchantId);
        merchantPromotionConfigService.deleteConfig(id, targetMerchantId);
        return ApiResponse.success("推广配置删除成功", null);
    }

    /**
     * 解析目标商户ID：管理员/超管优先使用请求中指定商户，商户角色取登录态
     */
    private Long resolveMerchantId(Long requestedMerchantId) {
        if (securityUtil.isAdmin()) {
            if (requestedMerchantId != null && requestedMerchantId > 0) {
                return requestedMerchantId;
            }
        }
        return securityUtil.getCurrentMerchantId();
    }

    // ==================== 辅助方法：Entity -> DTO 转换 ====================

    private PromotionPlatformDTO convertPlatformToDTO(PromotionPlatform platform) {
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

    private MerchantPromotionConfigDTO convertConfigToDTO(MerchantPromotionConfig config) {
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
