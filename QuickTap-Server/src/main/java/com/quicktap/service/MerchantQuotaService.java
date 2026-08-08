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
    private final com.quicktap.mapper.OrderMapper orderMapper;

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
        Long quota = getEffectiveQuota(merchant, "text");
        if (quota == null || quota <= 0) return true; // 未配置或 0 表示不限
        long usedCount = countGenerationsByType(merchantId, "text");
        log.info("检查文本生成配额: merchantId={}, quota={}, used={}",
            merchantId, quota, usedCount);
        return usedCount < quota;
    }

    public boolean checkImageQuota(Integer merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }
        Long quota = getEffectiveQuota(merchant, "image");
        if (quota == null || quota <= 0) return true; // 未配置或 0 表示不限
        long usedCount = countGenerationsByType(merchantId, "image");
        log.info("检查图片生成配额: merchantId={}, quota={}, used={}",
            merchantId, quota, usedCount);
        return usedCount < quota;
    }

    public boolean checkVideoQuota(Integer merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }
        Long quota = getEffectiveQuota(merchant, "video");
        if (quota == null || quota <= 0) return true; // 未配置或 0 表示不限
        long usedCount = countGenerationsByType(merchantId, "video");
        log.info("检查视频生成配额: merchantId={}, quota={}, used={}",
            merchantId, quota, usedCount);
        return usedCount < quota;
    }

    /**
     * 校验并返回友好提示：额度足够返回 null，不足返回提示文案
     */
    public String validateTextQuota(Integer merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }
        Long quota = getEffectiveQuota(merchant, "text");
        if (quota == null || quota <= 0) return null; // 不限
        long usedCount = countGenerationsByType(merchantId, "text");
        if (usedCount >= quota) {
            return "文字生成额度已用完（已用 " + usedCount + " / " + quota + " 次），请联系管理员调整额度";
        }
        return null;
    }

    public String validateImageQuota(Integer merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }
        Long quota = getEffectiveQuota(merchant, "image");
        if (quota == null || quota <= 0) return null; // 不限
        long usedCount = countGenerationsByType(merchantId, "image");
        if (usedCount >= quota) {
            return "图片生成额度已用完（已用 " + usedCount + " / " + quota + " 次），请联系管理员调整额度";
        }
        return null;
    }

    public String validateVideoQuota(Integer merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }
        Long quota = getEffectiveQuota(merchant, "video");
        if (quota == null || quota <= 0) return null; // 不限
        long usedCount = countGenerationsByType(merchantId, "video");
        if (usedCount >= quota) {
            return "视频生成额度已用完（已用 " + usedCount + " / " + quota + " 次），请联系管理员调整额度";
        }
        return null;
    }

    /**
     * 获取商户某类型 AI 生成的有效额度
     * 优先取商户级覆盖配置（textQuotaLimit 等），否则取套餐默认；均无返回 null
     */
    private Long getEffectiveQuota(Merchant merchant, String type) {
        Long override = switch (type) {
            case "text" -> merchant.getTextQuotaLimit();
            case "image" -> merchant.getImageQuotaLimit();
            case "video" -> merchant.getVideoQuotaLimit();
            default -> null;
        };
        if (override != null) {
            return override;
        }
        Plan plan = getPlanOrNull(merchant);
        if (plan == null) return null;
        return switch (type) {
            case "text" -> (long) plan.getTextQuota();
            case "image" -> (long) plan.getImageQuota();
            case "video" -> (long) plan.getVideoQuota();
            default -> null;
        };
    }

    public boolean checkStorageQuota(Integer merchantId, long requiredSize) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }
        // 存储限制为 0 或空 表示不限制
        long limitMB = merchant.getStorageLimit() == null ? 0 : merchant.getStorageLimit();
        if (limitMB <= 0) {
            log.info("检查存储配额: merchantId={} 不限制存储", merchantId);
            return true;
        }
        long usedMB = merchant.getStorageUsed() == null ? 0 : merchant.getStorageUsed();
        long availableSpace = (limitMB - usedMB) * 1024 * 1024;
        log.info("检查存储配额: merchantId={}, limit={}MB, used={}MB, required={}B",
            merchantId, limitMB, usedMB, requiredSize);
        return availableSpace >= requiredSize;
    }

    public Map<String, Object> getQuotaUsage(Integer merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            log.warn("商户不存在: merchantId={}", merchantId);
            return buildEmptyQuotaUsage();
        }

        Map<String, Object> usage = new HashMap<>();
        long textUsed = countGenerationsByType(merchantId, "text");
        long imageUsed = countGenerationsByType(merchantId, "image");
        long videoUsed = countGenerationsByType(merchantId, "video");

        // 当前套餐信息（供 my-quota 页面展示）
        Plan plan = getPlanOrNull(merchant);
        if (plan != null) {
            Map<String, Object> currentPlan = new HashMap<>();
            currentPlan.put("planId", plan.getId());
            currentPlan.put("planName", plan.getName());
            currentPlan.put("planLevel", plan.getLevel());
            currentPlan.put("price", plan.getPrice());
            currentPlan.put("durationMonths", plan.getDurationMonths());
            currentPlan.put("deviceCount", plan.getDeviceCount());
            currentPlan.put("storageLimit", plan.getStorageLimit());
            currentPlan.put("textQuota", plan.getTextQuota());
            currentPlan.put("imageQuota", plan.getImageQuota());
            currentPlan.put("videoQuota", plan.getVideoQuota());
            // 开通/到期时间：取最近一笔已支付订单
            java.util.Map<String, Object> latestPaid = orderMapper.selectLatestPaidByMerchantId(merchantId);
            if (latestPaid != null) {
                currentPlan.put("paidAt", latestPaid.get("created_at"));
                currentPlan.put("expireAt", latestPaid.get("expire_at"));
            }
            usage.put("currentPlan", currentPlan);
        }

        Map<String, Object> aiQuota = new HashMap<>();
        Long textQuota = getEffectiveQuota(merchant, "text");
        Long imageQuota = getEffectiveQuota(merchant, "image");
        Long videoQuota = getEffectiveQuota(merchant, "video");
        aiQuota.put("text", Map.of("quota", textQuota == null ? 0 : textQuota, "used", textUsed, "remaining", textQuota == null || textQuota <= 0 ? -1 : Math.max(0, textQuota - textUsed), "unlimited", textQuota == null || textQuota <= 0));
        aiQuota.put("image", Map.of("quota", imageQuota == null ? 0 : imageQuota, "used", imageUsed, "remaining", imageQuota == null || imageQuota <= 0 ? -1 : Math.max(0, imageQuota - imageUsed), "unlimited", imageQuota == null || imageQuota <= 0));
        aiQuota.put("video", Map.of("quota", videoQuota == null ? 0 : videoQuota, "used", videoUsed, "remaining", videoQuota == null || videoQuota <= 0 ? -1 : Math.max(0, videoQuota - videoUsed), "unlimited", videoQuota == null || videoQuota <= 0));
        usage.put("aiGeneration", aiQuota);

        long limitMB = merchant.getStorageLimit() == null ? 0 : merchant.getStorageLimit();
        long usedMB = merchant.getStorageUsed() == null ? 0 : merchant.getStorageUsed();
        Map<String, Object> storageQuota = new HashMap<>();
        storageQuota.put("limit", limitMB);
        storageQuota.put("used", usedMB);
        storageQuota.put("remaining", limitMB <= 0 ? -1 : Math.max(0, limitMB - usedMB));
        storageQuota.put("unlimited", limitMB <= 0);
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
        long used = merchant.getStorageUsed() == null ? 0 : merchant.getStorageUsed();
        long limit = merchant.getStorageLimit() == null ? 0 : merchant.getStorageLimit();
        long newUsedStorage = used + sizeInMB;
        if (limit > 0 && newUsedStorage > limit) {
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
        long used = merchant.getStorageUsed() == null ? 0 : merchant.getStorageUsed();
        long newUsedStorage = Math.max(0, used - sizeInMB);
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
        long usedStorage = merchant.getStorageUsed() == null ? 0 : merchant.getStorageUsed();
        if (newPlan.getStorageLimit() < usedStorage) {
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

    /**
     * 管理员端：分页获取所有商户的额度列表（含套餐名、存储、AI生成额度与用量）
     */
    public Map<String, Object> getAdminQuotaList(Integer page, Integer pageSize, String keyword) {
        int pn = (page == null || page <= 0) ? 1 : page;
        int ps = (pageSize == null || pageSize <= 0 || pageSize > 200) ? 10 : pageSize;
        int offset = (pn - 1) * ps;

        List<Merchant> merchants = merchantMapper.selectPage(offset, ps, keyword, null);
        long total = merchantMapper.countAll(keyword, null);

        List<Map<String, Object>> list = new ArrayList<>();
        if (merchants != null) {
            for (Merchant m : merchants) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", m.getId());
                item.put("name", m.getName());
                item.put("logo", m.getLogo());
                item.put("status", m.getStatus());
                item.put("hasCustomQuota",
                    (m.getStorageLimit() != null && m.getStorageLimit() > 0) ||
                    m.getTextQuotaLimit() != null || m.getImageQuotaLimit() != null || m.getVideoQuotaLimit() != null);

                Plan plan = getPlanOrNull(m);
                item.put("planId", plan != null ? plan.getId() : null);
                item.put("planName", plan != null ? plan.getName() : "未绑定套餐");
                item.put("planLevel", plan != null ? plan.getLevel() : null);

                long limitMB = m.getStorageLimit() == null ? 0 : m.getStorageLimit();
                long usedMB = m.getStorageUsed() == null ? 0 : m.getStorageUsed();
                Map<String, Object> storage = new HashMap<>();
                storage.put("limit", limitMB);
                storage.put("used", usedMB);
                storage.put("unlimited", limitMB <= 0);
                storage.put("percent", limitMB > 0 ? Math.min(100, Math.round((usedMB * 100.0) / limitMB)) : 0);
                item.put("storage", storage);

                item.put("textQuota", buildQuotaItem(getEffectiveQuota(m, "text"), countGenerationsByType(m.getId(), "text")));
                item.put("imageQuota", buildQuotaItem(getEffectiveQuota(m, "image"), countGenerationsByType(m.getId(), "image")));
                item.put("videoQuota", buildQuotaItem(getEffectiveQuota(m, "video"), countGenerationsByType(m.getId(), "video")));

                list.add(item);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", pn);
        result.put("pageSize", ps);
        return result;
    }

    private Map<String, Object> buildQuotaItem(Long quota, long used) {
        Map<String, Object> item = new HashMap<>();
        boolean unlimited = quota == null || quota <= 0;
        item.put("total", unlimited ? 0 : quota);
        item.put("used", used);
        item.put("unlimited", unlimited);
        item.put("remaining", unlimited ? -1 : Math.max(0, quota - used));
        return item;
    }

    /**
     * 管理员端：调整商户额度
     * storageLimit / textQuota / imageQuota / videoQuota 为 null 时不修改；0 表示不限
     */
    public void adjustQuota(Integer merchantId, Long storageLimit, Long textQuota, Long imageQuota, Long videoQuota) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }
        if (storageLimit != null) {
            if (storageLimit < 0) throw new BusinessException(ErrorCode.INVALID_REQUEST, "存储上限不能为负数");
            if (storageLimit > 0 && merchant.getStorageUsed() != null && merchant.getStorageUsed() > storageLimit) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "调整失败：当前已使用 " + merchant.getStorageUsed() + "MB，不能低于已使用量");
            }
            merchant.setStorageLimit(storageLimit);
        }
        if (textQuota != null) merchant.setTextQuotaLimit(textQuota < 0 ? null : textQuota);
        if (imageQuota != null) merchant.setImageQuotaLimit(imageQuota < 0 ? null : imageQuota);
        if (videoQuota != null) merchant.setVideoQuotaLimit(videoQuota < 0 ? null : videoQuota);
        merchantMapper.update(merchant);
        log.info("调整商户额度: merchantId={}, storageLimit={}, textQuota={}, imageQuota={}, videoQuota={}",
            merchantId, storageLimit, textQuota, imageQuota, videoQuota);
    }
}
