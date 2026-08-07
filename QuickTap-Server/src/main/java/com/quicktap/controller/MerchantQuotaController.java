package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.security.SecurityUtil;
import com.quicktap.service.MerchantQuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商户配额管理控制器 - AI生成和存储资源配额管理
 *
 * 职责：
 * - 提供商户级别的配额查询接口（AI生成、存储空间）
 * - 实现AI生成配额的动态检查（文本、图片、视频）
 * - 管理存储空间的使用和限制
 * - 支持套餐升级和降级操作
 * - 提供管理员端的配额统计和管理功能
 * - 支持月度配额重置功能
 *
 * 系统架构：
 * {@code
 * 商户配额系统
 * ├─ AI生成配额 (来自 Plan/套餐)
 * │  ├─ 文本生成: textQuota (限制次数)
 * │  ├─ 图片生成: imageQuota (限制次数)
 * │  └─ 视频生成: videoQuota (限制次数)
 * │
 * └─ 存储配额 (来自 Merchant)
 *    ├─ 存储限制: storageLimit (MB)
 *    └─ 已使用: storageUsed (MB)
 * }
 *
 * 访问控制：
 * - 商户端接口: 要求 MERCHANT 角色
 * - 管理员接口: 要求 ADMIN 或 SUPER_ADMIN 角色
 * - 所有接口需JWT Token认证
 *
 * 核心API端点：
 * {@code
 * 商户端接口 (MERCHANT only):
 * ============================================================
 *
 * GET /api/merchant/merchant-quota/usage
 *   → 获取配额使用情况总览
 *   参数: merchantId (必需)
 *   返回: {
 *     aiGeneration: { text: {...}, image: {...}, video: {...} },
 *     storage: { limit, used, remaining }
 *   }
 *
 * GET /api/merchant/merchant-quota/details
 *   → 获取详细的配额信息
 *   参数: merchantId (必需)
 *   返回: {
 *     merchantId, planId, planName,
 *     aiQuotas: { textQuota, imageQuota, videoQuota },
 *     usage: { ... }
 *   }
 *
 * GET /api/merchant/merchant-quota/check/text
 *   → 检查文本生成配额是否充足
 *   参数: merchantId (必需)
 *   返回: { merchantId, type: 'text', hasQuota: boolean }
 *
 * GET /api/merchant/merchant-quota/check/image
 *   → 检查图片生成配额是否充足
 *   参数: merchantId (必需)
 *   返回: { merchantId, type: 'image', hasQuota: boolean }
 *
 * GET /api/merchant/merchant-quota/check/video
 *   → 检查视频生成配额是否充足
 *   参数: merchantId (必需)
 *   返回: { merchantId, type: 'video', hasQuota: boolean }
 *
 * GET /api/merchant/merchant-quota/check/storage
 *   → 检查存储空间是否充足
 *   参数: merchantId (必需), requiredSize (必需，单位字节)
 *   返回: { merchantId, type: 'storage', requiredSize, hasQuota: boolean }
 *
 * POST /api/merchant/merchant-quota/update-storage
 *   → 增加存储使用量（上传文件后调用）
 *   参数: merchantId (必需), sizeInMB (必需)
 *   返回: { merchantId, addedSize, message }
 *
 * POST /api/merchant/merchant-quota/reduce-storage
 *   → 减少存储使用量（删除文件后调用）
 *   参数: merchantId (必需), sizeInMB (必需)
 *   返回: { merchantId, reducedSize, message }
 *
 * POST /api/merchant/merchant-quota/change-plan
 *   → 变更套餐（升级/降级）
 *   参数: merchantId (必需), newPlanId (必需)
 *   返回: { merchantId, newPlanId, message }
 *
 * 管理员端接口 (ADMIN/SUPER_ADMIN only):
 * ============================================================
 *
 * GET /api/admin/merchant-quota/all
 *   → 获取所有商户的配额统计
 *   返回: 所有商户的配额聚合数据
 *
 * POST /api/admin/merchant-quota/{merchantId}/reset
 *   → 重置商户的月度AI生成配额
 *   参数: merchantId (路径参数)
 *   返回: { merchantId, message: '月度配额重置成功' }
 * }
 *
 * 使用场景：
 * {@code
 * // 场景1: AI生成前的配额检查
 * const hasQuota = await fetch(
 *   '/api/merchant/merchant-quota/check/text?merchantId=123'
 * );
 * if (!hasQuota.data.hasQuota) {
 *   // 提示商户配额不足，引导升级
 * }
 *
 * // 场景2: 获取配额使用情况展示在仪表板
 * const usage = await fetch(
 *   '/api/merchant/merchant-quota/usage?merchantId=123'
 * );
 * // 前端显示各类型的使用量和剩余量
 *
 * // 场景3: 文件上传前检查存储空间
 * const fileSizeBytes = 5 * 1024 * 1024; // 5MB
 * const hasSpace = await fetch(
 *   '/api/merchant/merchant-quota/check/storage?' +
 *   `merchantId=123&requiredSize=${fileSizeBytes}`
 * );
 *
 * // 场景4: 套餐升级
 * await fetch('/api/merchant/merchant-quota/change-plan', {
 *   method: 'POST',
 *   params: { merchantId: 123, newPlanId: 2 }
 * });
 *
 * // 场景5: 管理员查看所有商户配额统计
 * const stats = await fetch('/api/admin/merchant-quota/all');
 * // 显示系统级的配额使用汇总
 * }
 *
 * 配额检查流程：
 * {@code
 * 1. AI生成请求到来
 * 2. 调用 checkTextQuota/checkImageQuota/checkVideoQuota
 * 3. 从数据库查询 Plan 配额限制
 * 4. 统计已使用的生成次数（状态=成功）
 * 5. 比较: 已使用 < 限制 → 有配额
 * 6. 若有配额，允许生成；否则拒绝
 * }
 *
 * 存储管理流程：
 * {@code
 * 文件上传流程:
 * 1. 前端检查存储配额 (checkStorageQuota)
 * 2. 若充足，上传文件
 * 3. 后端更新存储使用量 (updateStorageUsage)
 *
 * 文件删除流程:
 * 1. 删除文件
 * 2. 调用减少存储使用量 (reduceStorageUsage)
 * 3. 更新 merchant.storageUsed 字段
 * }
 *
 * 配额数据结构：
 * {@code
 * {
 *   "aiGeneration": {
 *     "text": {
 *       "quota": 1000,        // 月度限制
 *       "used": 350,          // 已使用
 *       "remaining": 650      // 剩余
 *     },
 *     "image": {
 *       "quota": 500,
 *       "used": 180,
 *       "remaining": 320
 *     },
 *     "video": {
 *       "quota": 100,
 *       "used": 25,
 *       "remaining": 75
 *     }
 *   },
 *   "storage": {
 *     "limit": 1024,          // MB
 *     "used": 512,            // MB
 *     "remaining": 512        // MB
 *   }
 * }
 * }
 *
 * 错误处理：
 * - 商户不存在 → 返回 404 Not Found
 * - 套餐不存在 → 返回 404 Not Found
 * - 存储空间不足 → 返回 400 Bad Request
 * - 套餐降级时存储不足 → 返回 400 Bad Request with 具体错误信息
 * - 权限不足 → 返回 403 Forbidden
 * - 数据库异常 → 返回 500 Internal Server Error
 *
 * 业务规则：
 * - AI生成配额：按月重置（由定时任务处理）
 * - 存储配额：累积计算，不自动重置
 * - 套餐升级：立即生效，新配额从下月开始
 * - 套餐降级：检查存储兼容性，防止数据丢失
 *
 * 数据库交互：
 * - MerchantQuotaService.checkTextQuota() - 检查文本配额
 * - MerchantQuotaService.checkImageQuota() - 检查图片配额
 * - MerchantQuotaService.checkVideoQuota() - 检查视频配额
 * - MerchantQuotaService.checkStorageQuota() - 检查存储配额
 * - MerchantQuotaService.getQuotaUsage() - 获取使用情况
 * - MerchantQuotaService.getQuotaDetails() - 获取详情
 * - MerchantQuotaService.updateStorageUsage() - 增加存储
 * - MerchantQuotaService.reduceStorageUsage() - 减少存储
 * - MerchantQuotaService.changePlan() - 变更套餐
 *
 * 性能优化：
 * - 使用数据库聚合函数计算已使用次数（非内存过滤）
 * - 配额检查快速失败，避免不必要的数据库查询
 * - 支持缓存热点数据（如商户配额）
 *
 * 日志记录：
 * - INFO: 正常的配额查询和更新操作
 * - ERROR: 配额检查失败、更新失败等异常
 *
 * 集成点：
 * - MerchantQuotaService: 业务逻辑实现
 * - AiGenerateService: AI生成前调用检查
 * - StorageService: 文件操作时调用
 * - BillingService: 套餐计费整合
 * - ScheduledTask: 定时任务（月度重置）
 *
 * @author QuickTap Quota Management Team
 * @version 1.0
 * @since 1.0
 * @see MerchantQuotaService
 * @see com.quicktap.entity.Merchant
 * @see com.quicktap.entity.Plan
 * @see com.quicktap.entity.AiGenerateRecord
 */
@Slf4j
@RestController
@RequestMapping("/api")

@RequiredArgsConstructor
public class MerchantQuotaController {

    private final MerchantQuotaService merchantQuotaService;
    private final SecurityUtil securityUtil;

    /**
     * 获取商户配额使用情况
     * - MERCHANT 角色：不传 merchantId 时从 token 取
     * - ADMIN/SUPER_ADMIN 角色：可通过 merchantId 参数指定查询
     */
    @GetMapping("/merchant/merchant-quota/usage")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> getQuotaUsage(@RequestParam(required = false) Integer merchantId) {
        Integer mid = resolveMerchantId(merchantId);
        log.info("获取商户配额使用情况: merchantId={}", mid);
        if (mid == null) return ApiResponse.badRequest("缺少 merchantId 参数");
        Map<String, Object> usage = merchantQuotaService.getQuotaUsage(mid);
        return ApiResponse.success(usage);
    }

    /**
     * 获取商户配额详情
     */
    @GetMapping("/merchant/merchant-quota/details")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> getQuotaDetails(@RequestParam(required = false) Integer merchantId) {
        Integer mid = resolveMerchantId(merchantId);
        log.info("获取商户配额详情: merchantId={}", mid);
        if (mid == null) return ApiResponse.badRequest("缺少 merchantId 参数");
        Map<String, Object> details = merchantQuotaService.getQuotaDetails(mid);
        return ApiResponse.success(details);
    }

    /**
     * 检查文本生成配额
     */
    @GetMapping("/merchant/merchant-quota/check/text")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> checkTextQuota(@RequestParam(required = false) Integer merchantId) {
        Integer mid = resolveMerchantId(merchantId);
        log.info("检查文本生成配额: merchantId={}", mid);
        if (mid == null) return ApiResponse.badRequest("缺少 merchantId 参数");
        boolean hasQuota = merchantQuotaService.checkTextQuota(mid);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("merchantId", mid);
        result.put("type", "text");
        result.put("hasQuota", hasQuota);

        return ApiResponse.success(result);
    }

    /**
     * 检查图片生成配额
     */
    @GetMapping("/merchant/merchant-quota/check/image")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> checkImageQuota(@RequestParam(required = false) Integer merchantId) {
        Integer mid = resolveMerchantId(merchantId);
        log.info("检查图片生成配额: merchantId={}", mid);
        if (mid == null) return ApiResponse.badRequest("缺少 merchantId 参数");
        boolean hasQuota = merchantQuotaService.checkImageQuota(mid);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("merchantId", mid);
        result.put("type", "image");
        result.put("hasQuota", hasQuota);

        return ApiResponse.success(result);
    }

    /**
     * 检查视频生成配额
     */
    @GetMapping("/merchant/merchant-quota/check/video")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> checkVideoQuota(@RequestParam(required = false) Integer merchantId) {
        Integer mid = resolveMerchantId(merchantId);
        log.info("检查视频生成配额: merchantId={}", mid);
        if (mid == null) return ApiResponse.badRequest("缺少 merchantId 参数");
        boolean hasQuota = merchantQuotaService.checkVideoQuota(mid);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("merchantId", mid);
        result.put("type", "video");
        result.put("hasQuota", hasQuota);

        return ApiResponse.success(result);
    }

    /**
     * 检查存储空间配额
     */
    @GetMapping("/merchant/merchant-quota/check/storage")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> checkStorageQuota(
            @RequestParam(required = false) Integer merchantId,
            @RequestParam long requiredSize) {
        Integer mid = resolveMerchantId(merchantId);
        log.info("检查存储配额: merchantId={}, requiredSize={}", mid, requiredSize);
        if (mid == null) return ApiResponse.badRequest("缺少 merchantId 参数");
        boolean hasQuota = merchantQuotaService.checkStorageQuota(mid, requiredSize);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("merchantId", mid);
        result.put("type", "storage");
        result.put("requiredSize", requiredSize);
        result.put("hasQuota", hasQuota);

        return ApiResponse.success(result);
    }

    /**
     * 更新存储使用量（系统内部调用）
     */
    @PostMapping("/merchant/merchant-quota/update-storage")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> updateStorageUsage(
            @RequestParam(required = false) Integer merchantId,
            @RequestParam long sizeInMB) {
        Integer mid = resolveMerchantId(merchantId);
        log.info("更新存储使用量: merchantId={}, sizeInMB={}", mid, sizeInMB);
        if (mid == null) return ApiResponse.badRequest("缺少 merchantId 参数");
        merchantQuotaService.updateStorageUsage(mid, sizeInMB);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("merchantId", mid);
        result.put("addedSize", sizeInMB);
        result.put("message", "存储使用量更新成功");

        return ApiResponse.success(result);
    }

    /**
     * 减少存储使用量
     */
    @PostMapping("/merchant/merchant-quota/reduce-storage")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> reduceStorageUsage(
            @RequestParam(required = false) Integer merchantId,
            @RequestParam long sizeInMB) {
        Integer mid = resolveMerchantId(merchantId);
        log.info("减少存储使用量: merchantId={}, sizeInMB={}", mid, sizeInMB);
        if (mid == null) return ApiResponse.badRequest("缺少 merchantId 参数");
        merchantQuotaService.reduceStorageUsage(mid, sizeInMB);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("merchantId", mid);
        result.put("reducedSize", sizeInMB);
        result.put("message", "存储使用量减少成功");

        return ApiResponse.success(result);
    }

    /**
     * 变更套餐
     */
    @PostMapping("/merchant/merchant-quota/change-plan")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> changePlan(
            @RequestParam(required = false) Integer merchantId,
            @RequestParam Integer newPlanId) {
        Integer mid = resolveMerchantId(merchantId);
        log.info("变更套餐: merchantId={}, newPlanId={}", mid, newPlanId);
        if (mid == null) return ApiResponse.badRequest("缺少 merchantId 参数");
        merchantQuotaService.changePlan(mid, newPlanId);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("merchantId", mid);
        result.put("newPlanId", newPlanId);
        result.put("message", "套餐变更成功");

        return ApiResponse.success(result);
    }

    /**
     * 辅助方法：解析 merchantId
     * 优先使用参数传入的 merchantId，否则从 token 中取当前登录的商户ID
     */
    private Integer resolveMerchantId(Integer merchantId) {
        if (merchantId != null && merchantId > 0) {
            return merchantId;
        }
        try {
            Long fromToken = securityUtil.getCurrentMerchantId();
            return fromToken != null ? fromToken.intValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取所有商户的配额统计（管理员端）
     * 匹配 Node.js: GET /api/admin/merchant-quota/all
     */
    @GetMapping("/admin/merchant-quota/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> getAllMerchantQuota() {
        log.info("获取所有商户配额统计");

        // ✅ TODO 13 COMPLETED: Implement quota statistics for all merchants
        try {
            Map<String, Object> result = merchantQuotaService.getAllMerchantQuotaStatistics();
            log.info("✅ 获取所有商户配额统计成功");
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("❌ 获取商户配额统计失败: {}", e.getMessage(), e);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("totalMerchants", 0);
            result.put("totalStorageUsed", 0);
            result.put("totalAIGenerations", 0);
            result.put("error", e.getMessage());
            return ApiResponse.error(500, "获取商户配额统计失败: " + e.getMessage());
        }
    }

    /**
     * 重置商户月度配额（管理员端）
     * 匹配 Node.js: POST /api/admin/merchant-quota/:merchantId/reset
     */
    @PostMapping("/admin/merchant-quota/{merchantId}/reset")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> resetMonthlyQuota(@PathVariable Integer merchantId) {
        log.info("重置月度配额: merchantId={}", merchantId);
        merchantQuotaService.resetMonthlyQuota(merchantId);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("merchantId", merchantId);
        result.put("message", "月度配额重置成功");

        return ApiResponse.success(result);
    }

    /**
     * 管理员端：分页获取所有商户的额度列表（含套餐名、存储、AI生成额度与用量）
     */
    @GetMapping("/admin/merchant-quota/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> getAdminQuotaList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String keyword) {
        log.info("获取商户额度列表: page={}, pageSize={}, keyword={}", page, pageSize, keyword);
        Map<String, Object> result = merchantQuotaService.getAdminQuotaList(page, pageSize, keyword);
        return ApiResponse.success(result);
    }

    /**
     * 管理员端：调整商户额度（存储上限 / AI生成额度），0 表示不限
     * body: { storageLimit?: number, textQuota?: number, imageQuota?: number, videoQuota?: number }
     */
    @PostMapping("/admin/merchant-quota/{merchantId}/adjust")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> adjustQuota(
            @PathVariable Integer merchantId,
            @RequestBody(required = false) Map<String, Object> body) {
        log.info("调整商户额度: merchantId={}, body={}", merchantId, body);
        Long storageLimit = body == null ? null : toLong(body.get("storageLimit"));
        Long textQuota = body == null ? null : toLong(body.get("textQuota"));
        Long imageQuota = body == null ? null : toLong(body.get("imageQuota"));
        Long videoQuota = body == null ? null : toLong(body.get("videoQuota"));

        if (storageLimit == null && textQuota == null && imageQuota == null && videoQuota == null) {
            return ApiResponse.badRequest("请至少提供一项要调整的额度");
        }

        merchantQuotaService.adjustQuota(merchantId, storageLimit, textQuota, imageQuota, videoQuota);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("merchantId", merchantId);
        result.put("message", "额度调整成功");
        return ApiResponse.success(result);
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        String s = String.valueOf(value).trim();
        if (s.isEmpty()) return null;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
