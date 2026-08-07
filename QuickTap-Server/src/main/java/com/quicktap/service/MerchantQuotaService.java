package com.quicktap.service;

import com.quicktap.entity.AiGenerateRecord;
import com.quicktap.entity.Merchant;
import com.quicktap.entity.Plan;
import com.quicktap.exception.BusinessException;
import com.quicktap.mapper.AiGenerateRecordMapper;
import com.quicktap.mapper.MerchantMapper;
import com.quicktap.mapper.PlanMapper;
import com.quicktap.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantQuotaService {

    private final MerchantMapper merchantMapper;
    private final PlanMapper planMapper;
    private final AiGenerateRecordMapper aiGenerateRecordMapper;

    private Plan getPlanOrNull(Merchant merchant) {
        if (merchant.getPlanId() != null) {
            return planMapper.selectById(merchant.getPlanId());
        }
        return null;
    }

    public boolean checkTextQuota(Integer merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }
        Plan plan = getPlanOrNull(merchant);
        if (plan == null) return false;
        long usedCount = countGenerationsByType(merchantId, "text");
        log.info("检查文本生成配额: merchantId={}, quota={}, used={}",
            merchantId, plan.getTextQuota(), usedCount);
        return usedCount < plan.getTextQuota();
    }

    public boolean checkImageQuota(Integer merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }
        Plan plan = getPlanOrNull(merchant);
        if (plan == null) return false;
        long usedCount = countGenerationsByType(merchantId, "image");
        log.info("检查图片生成配额: merchantId={}, quota={}, used={}",
            merchantId, plan.getImageQuota(), usedCount);
        return usedCount < plan.getImageQuota();
    }

    public boolean checkVideoQuota(Integer merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }
        Plan plan = getPlanOrNull(merchant);
        if (plan == null) return false;
        long usedCount = countGenerationsByType(merchantId, "video");
        log.info("检查视频生成配额: merchantId={}, quota={}, used={}",
            merchantId, plan.getVideoQuota(), usedCount);
        return usedCount < plan.getVideoQuota();
    }

    public boolean checkStorageQuota(Integer merchantId, long requiredSize) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }
        long availableSpace = (merchant.getStorageLimit() - merchant.getStorageUsed()) * 1024 * 1024;
        log.info("检查存储配额: merchantId={}, limit={}MB, used={}MB, required={}B",
            merchantId, merchant.getStorageLimit(), merchant.getStorageUsed(), requiredSize);
        return availableSpace >= requiredSize;
    }

    public Map<String, Object> getQuotaUsage(Integer merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            log.warn("商户不存在: merchantId={}", merchantId);
            return buildEmptyQuotaUsage();
        }
        Plan plan = getPlanOrNull(merchant);
        if (plan == null) {
            log.warn("商户 {} 未绑定套餐，返回默认配额", merchantId);
            return buildEmptyQuotaUsage();
        }

        Map<String, Object> usage = new HashMap<>();
        long textUsed = countGenerationsByType(merchantId, "text");
        long imageUsed = countGenerationsByType(merchantId, "image");
        long videoUsed = countGenerationsByType(merchantId, "video");

        Map<String, Object> aiQuota = new HashMap<>();
        aiQuota.put("text", Map.of("quota", plan.getTextQuota(), "used", textUsed, "remaining", Math.max(0, plan.getTextQuota() - textUsed)));
        aiQuota.put("image", Map.of("quota", plan.getImageQuota(), "used", imageUsed, "remaining", Math.max(0, plan.getImageQuota() - imageUsed)));
        aiQuota.put("video", Map.of("quota", plan.getVideoQuota(), "used", videoUsed, "remaining", Math.max(0, plan.getVideoQuota() - videoUsed)));
        usage.put("aiGeneration", aiQuota);

        Map<String, Object> storageQuota = new HashMap<>();
        storageQuota.put("limit", merchant.getStorageLimit());
        storageQuota.put("used", merchant.getStorageUsed());
        storageQuota.put("remaining", Math.max(0, merchant.getStorageLimit() - merchant.getStorageUsed()));
        usage.put("storage", storageQuota);
        return usage;
    }

    private Map<String, Object> buildEmptyQuotaUsage() {
        Map<String, Object> usage = new HashMap<>();
        Map<String, Object> aiQuota = new HashMap<>();
        aiQuota.put("text", Map.of("quota", 0, "used", 0, "remaining", 0));
        aiQuota.put("image", Map.of("quota", 0, "used", 0, "remaining", 0));
        aiQuota.put("video", Map.of("quota", 0, "used", 0, "remaining", 0));
        usage.put("aiGeneration", aiQuota);
        usage.put("storage", Map.of("limit", 0, "used", 0, "remaining", 0));
        return usage;
    }

    public void updateStorageUsage(Integer merchantId, long sizeInMB) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }
        long newUsedStorage = merchant.getStorageUsed() + sizeInMB;
        if (newUsedStorage > merchant.getStorageLimit()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "存储空间不足");
        }
        merchant.setStorageUsed(newUsedStorage);
        merchantMapper.update(merchant);
        log.info("更新存储使用量: merchantId={}, newUsed={}MB", merchantId, newUsedStorage);
    }

    public void reduceStorageUsage(Integer merchantId, long sizeInMB) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }
        long newUsedStorage = Math.max(0, merchant.getStorageUsed() - sizeInMB);
        merchant.setStorageUsed(newUsedStorage);
        merchantMapper.update(merchant);
        log.info("减少存储使用量: merchantId={}, newUsed={}MB", merchantId, newUsedStorage);
    }

    public void resetMonthlyQuota(Integer merchantId) {
        log.info("月度配额重置: merchantId={}", merchantId);
    }

    public void changePlan(Integer merchantId, Integer newPlanId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }
        Plan newPlan = planMapper.selectById(newPlanId);
        if (newPlan == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "套餐不存在");
        }
        Plan oldPlan = getPlanOrNull(merchant);
        if (newPlan.getStorageLimit() < merchant.getStorageUsed()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "新套餐的存储空间不足以容纳当前已使用的空间");
        }
        merchant.setPlanId(newPlanId);
        merchantMapper.update(merchant);
        log.info("套餐变更: merchantId={}, oldPlanId={}, newPlanId={}",
            merchantId, oldPlan != null ? oldPlan.getId() : null, newPlanId);
    }

    public Map<String, Object> getQuotaDetails(Integer merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }
        Plan plan = getPlanOrNull(merchant);
        Map<String, Object> details = new HashMap<>();
        details.put("merchantId", merchantId);
        if (plan != null) {
            details.put("planId", plan.getId());
            details.put("planName", plan.getName());
            Map<String, Object> aiQuotas = new HashMap<>();
            aiQuotas.put("textQuota", plan.getTextQuota());
            aiQuotas.put("imageQuota", plan.getImageQuota());
            aiQuotas.put("videoQuota", plan.getVideoQuota());
            details.put("aiQuotas", aiQuotas);
        } else {
            details.put("planId", null);
            details.put("planName", "未绑定");
            details.put("aiQuotas", Map.of("textQuota", 0, "imageQuota", 0, "videoQuota", 0));
        }
        Map<String, Object> usage = getQuotaUsage(merchantId);
        details.put("usage", usage);
        return details;
    }

    private long countGenerationsByType(Integer merchantId, String type) {
        try {
            long count = aiGenerateRecordMapper.countByMerchantAndType(merchantId, type);
            log.debug("查询生成次数: merchantId={}, type={}, count={}", merchantId, type, count);
            return count;
        } catch (Exception e) {
            log.error("查询生成次数失败: merchantId={}, type={}, {}", merchantId, type, e.getMessage(), e);
            return 0;
        }
    }

    public Map<String, Object> getAllMerchantQuotaStatistics() {
        List<Merchant> merchants = merchantMapper.selectAll();
        Map<String, Object> result = new HashMap<>();
        long totalMerchants = merchants != null ? merchants.size() : 0;
        long totalAiGenerations = 0;
        if (merchants != null) {
            for (Merchant merchant : merchants) {
                totalAiGenerations += countGenerationsByType(merchant.getId(), null);
            }
        }
        result.put("totalMerchants", totalMerchants);
        result.put("totalStorageUsed", 0);
        result.put("totalAIGenerations", totalAiGenerations);
        return result;
    }
}
