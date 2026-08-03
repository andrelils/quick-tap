package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.dto.PageResponse;
import com.quicktap.entity.Corpus;
import com.quicktap.security.SecurityUtil;
import com.quicktap.service.CorpusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;

/**
 * 知识库内容管理控制器
 * 提供内容 CRUD、分类管理、回收站等功能
 * 匹配 Node.js: /api//corpus/*
 */
@Slf4j
@RestController
@RequestMapping("/api")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CorpusController {

    private final CorpusService corpusService;
    private final SecurityUtil securityUtil;

    /**
     * 创建知识库内容（商户端/管理员端）
     * 匹配 Node.js: POST /api/merchant/corpus
     */
    @PostMapping("/merchant/corpus")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Corpus> createCorpus(
            @RequestParam(required = false) Integer merchantId,
            @RequestBody Corpus corpus) {
        Integer mid = resolveMerchantId(merchantId);
        if (mid != null && corpus.getMerchantId() == null) {
            corpus.setMerchantId(mid);
        }
        log.info("创建知识库内容: merchantId={}, title={}", corpus.getMerchantId(), corpus.getTitle());
        Corpus result = corpusService.createCorpus(corpus);
        return ApiResponse.success("创建成功", result);
    }

    /**
     * 获取知识库内容详情
     * 匹配 Node.js: GET /api//corpus/:corpusId
     */
    @GetMapping("/merchant/corpus/{corpusId}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Corpus> getCorpus(
            @NotBlank(message = "内容ID不能为空") @PathVariable String corpusId) {
        log.info("获取知识库内容: corpusId={}", corpusId);
        Corpus result = corpusService.getCorpus(corpusId);
        return ApiResponse.success("获取成功", result);
    }

    /**
     * 更新知识库内容（商户端/管理员端）
     * 匹配 Node.js: PUT /api/merchant/corpus/:corpusId
     */
    @PutMapping("/merchant/corpus/{corpusId}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Corpus> updateCorpus(
            @NotBlank(message = "内容ID不能为空") @PathVariable String corpusId,
            @RequestBody Corpus corpus) {
        log.info("更新知识库内容: corpusId={}", corpusId);
        corpus.setCorpusId(corpusId);
        Corpus result = corpusService.updateCorpus(corpus);
        return ApiResponse.success("更新成功", result);
    }

    /**
     * 删除知识库内容（软删除）（商户端/管理员端）
     * 匹配 Node.js: DELETE /api/merchant/corpus/:corpusId
     */
    @DeleteMapping("/merchant/corpus/{corpusId}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Void> deleteCorpus(
            @NotBlank(message = "内容ID不能为空") @PathVariable String corpusId) {
        log.info("删除知识库内容: corpusId={}", corpusId);
        corpusService.deleteCorpus(corpusId);
        return ApiResponse.success("删除成功");
    }

    /**
     * 永久删除知识库内容（商户端/管理员端）
     * 匹配 Node.js: DELETE /api/merchant/corpus/:corpusId/permanent
     */
    @DeleteMapping("/merchant/corpus/{corpusId}/permanent")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Void> permanentDelete(
            @NotBlank(message = "内容ID不能为空") @PathVariable String corpusId) {
        log.info("永久删除知识库内容: corpusId={}", corpusId);
        corpusService.permanentDelete(corpusId);
        return ApiResponse.success("永久删除成功");
    }

    /**
     * 获取商户的知识库内容列表（商户端/管理员端）
     * 匹配 Node.js: GET /api/merchant/corpus
     */
    @GetMapping("/merchant/corpus")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<PageResponse<Corpus>> getMerchantCorpus(
            @RequestParam(required = false) Integer merchantId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Integer mid = resolveMerchantId(merchantId);
        log.info("获取商户知识库: merchantId={}", mid);
        if (mid == null) {
            return ApiResponse.badRequest("缺少 merchantId 参数");
        }
        PageResponse<Corpus> result = corpusService.getMerchantCorpus(mid, pageNum, pageSize);
        return ApiResponse.success("获取成功", result);
    }

    /**
     * 按分类获取知识库内容
     * 匹配 Node.js: GET /api//corpus/category/:category
     */
    @GetMapping("/merchant/corpus/category/{category}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<PageResponse<Corpus>> getByCategory(
            @NotBlank(message = "分类不能为空") @PathVariable String category,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("按分类获取知识库内容: category={}", category);
        PageResponse<Corpus> result = corpusService.getByCategory(category, pageNum, pageSize);
        return ApiResponse.success("获取成功", result);
    }

    /**
     * 获取回收站内容（已删除的内容）（商户端/管理员端）
     * 匹配 Node.js: GET /api/merchant/corpus/trash
     */
    @GetMapping("/merchant/corpus/trash")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<PageResponse<Corpus>> getTrash(
            @RequestParam(required = false) Integer merchantId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Integer mid = resolveMerchantId(merchantId);
        log.info("获取回收站: merchantId={}", mid);
        if (mid == null) {
            return ApiResponse.badRequest("缺少 merchantId 参数");
        }
        PageResponse<Corpus> result = corpusService.getTrash(mid, pageNum, pageSize);
        return ApiResponse.success("获取成功", result);
    }

    /**
     * 恢复删除的内容（商户端/管理员端）
     * 匹配 Node.js: POST /api/merchant/corpus/:corpusId/restore
     */
    @PostMapping("/merchant/corpus/{corpusId}/restore")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<Void> restoreCorpus(
            @NotBlank(message = "内容ID不能为空") @PathVariable String corpusId) {
        log.info("恢复知识库内容: corpusId={}", corpusId);
        corpusService.restoreCorpus(corpusId);
        return ApiResponse.success("恢复成功");
    }

    /**
     * 搜索知识库内容
     * 匹配 Node.js: GET /api//corpus/search
     */
    @GetMapping("/merchant/corpus/search")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<PageResponse<Corpus>> search(
            @NotBlank(message = "搜索关键词不能为空") @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("搜索知识库内容: keyword={}", keyword);
        PageResponse<Corpus> result = corpusService.search(keyword, pageNum, pageSize);
        return ApiResponse.success("获取成功", result);
    }

    /**
     * 获取所有知识库内容（管理员端）
     * 匹配 Node.js: GET /api/admin/corpus
     */
    @GetMapping("/admin/corpus")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<PageResponse<Corpus>> getAllCorpus(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取所有知识库内容");
        PageResponse<Corpus> result = PageResponse.empty(pageNum, pageSize);
        return ApiResponse.success("获取成功", result);
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
}
