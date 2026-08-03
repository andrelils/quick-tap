package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.dto.CorpusCategoryDTO;
import com.quicktap.dto.CreateCorpusCategoryRequest;
import com.quicktap.dto.UpdateCorpusCategoryRequest;
import com.quicktap.security.SecurityUtil;
import com.quicktap.service.CorpusCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 语料库分类 Controller
 * 提供分类的增删改查接口
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant/corpus/categories")
@PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
public class CorpusCategoryController {

    @Autowired
    private CorpusCategoryService corpusCategoryService;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 创建分类
     * 对应 Node: POST /api/admin/corpus/categories
     */
    @PostMapping
    public ApiResponse<CorpusCategoryDTO> create(
            @RequestParam(required = false) Long merchantId,
            @Valid @RequestBody CreateCorpusCategoryRequest request) {
        Long mid = resolveMerchantId(merchantId);
        log.info("创建语料库分类 | merchantId: {} | name: {}", mid, request.getName());
        if (mid == null) {
            return ApiResponse.badRequest("缺少 merchantId 参数");
        }

        CorpusCategoryDTO result = corpusCategoryService.create(mid, request);
        return ApiResponse.success("分类创建成功", result);
    }

    /**
     * 查询分类列表
     * 对应 Node: GET /api/admin/corpus/categories
     */
    @GetMapping
    public ApiResponse<List<CorpusCategoryDTO>> list(@RequestParam(required = false) Long merchantId) {
        Long mid = resolveMerchantId(merchantId);
        log.info("查询语料库分类列表 | merchantId: {}", mid);
        if (mid == null) {
            return ApiResponse.success("分类列表查询成功", java.util.Collections.emptyList());
        }
        List<CorpusCategoryDTO> result = corpusCategoryService.listByMerchantId(mid);
        return ApiResponse.success("分类列表查询成功", result);
    }

    /**
     * 查询分类详情
     * 对应 Node: GET /api/admin/corpus/categories/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<CorpusCategoryDTO> getById(
            @PathVariable Long id,
            @RequestParam(required = false) Long merchantId) {
        Long mid = resolveMerchantId(merchantId);
        log.info("查询语料库分类详情 | id: {} | merchantId: {}", id, mid);

        CorpusCategoryDTO result = corpusCategoryService.getById(id, mid);
        return ApiResponse.success("分类详情查询成功", result);
    }

    /**
     * 更新分类
     * 对应 Node: PUT /api/admin/corpus/categories/{id}
     */
    @PutMapping("/{id}")
    public ApiResponse<CorpusCategoryDTO> update(
            @PathVariable Long id,
            @RequestParam(required = false) Long merchantId,
            @Valid @RequestBody UpdateCorpusCategoryRequest request) {
        Long mid = resolveMerchantId(merchantId);
        log.info("更新语料库分类 | id: {} | merchantId: {}", id, mid);
        if (mid == null) {
            return ApiResponse.badRequest("缺少 merchantId 参数");
        }

        CorpusCategoryDTO result = corpusCategoryService.update(id, mid, request);
        return ApiResponse.success("分类更新成功", result);
    }

    /**
     * 删除分类
     * 对应 Node: DELETE /api/admin/corpus/categories/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @RequestParam(required = false) Long merchantId) {
        Long mid = resolveMerchantId(merchantId);
        log.info("删除语料库分类 | id: {} | merchantId: {}", id, mid);
        if (mid == null) {
            return ApiResponse.badRequest("缺少 merchantId 参数");
        }

        corpusCategoryService.delete(id, mid);
        return ApiResponse.success("分类删除成功");
    }

    /**
     * 辅助方法：解析 merchantId
     * 优先使用参数传入的 merchantId，否则从 token 中取当前登录的商户ID
     */
    private Long resolveMerchantId(Long merchantId) {
        if (merchantId != null && merchantId > 0) {
            return merchantId;
        }
        try {
            return securityUtil.getCurrentMerchantId();
        } catch (Exception e) {
            return null;
        }
    }
}
