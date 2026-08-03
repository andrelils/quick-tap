package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.dto.QrCodeDTO;
import com.quicktap.dto.GenerateQrCodeRequest;
import com.quicktap.dto.BatchGenerateQrCodeRequest;
import com.quicktap.dto.BindQrCodeRequest;
import com.quicktap.security.SecurityUtil;
import com.quicktap.service.QrCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 二维码 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/qrcode")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class QrCodeController {

    @Autowired
    private QrCodeService qrCodeService;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 生成单个二维码
     * 对应 Node: POST /api/admin/qrcode/generate
     */
    @PostMapping("/generate")
    public ApiResponse<QrCodeDTO> generateQrCode(
            @RequestParam(required = false) Long merchantId,
            @Valid @RequestBody GenerateQrCodeRequest request) {
        Long mid = resolveMerchantId(merchantId);
        log.info("生成二维码 | merchantId: {}", mid);
        if (mid == null) {
            return ApiResponse.badRequest("缺少 merchantId 参数");
        }

        QrCodeDTO result = qrCodeService.generateQrCode(mid, request);
        return ApiResponse.success("二维码生成成功", result);
    }

    /**
     * 批量生成二维码
     * 对应 Node: POST /api/admin/qrcode/batch
     */
    @PostMapping("/batch")
    public ApiResponse<List<QrCodeDTO>> batchGenerateQrCodes(
            @RequestParam(required = false) Long merchantId,
            @Valid @RequestBody BatchGenerateQrCodeRequest request) {
        Long mid = resolveMerchantId(merchantId);
        log.info("批量生成二维码 | merchantId: {} | count: {}", mid, request.getDeviceIds().size());
        if (mid == null) {
            return ApiResponse.badRequest("缺少 merchantId 参数");
        }

        List<QrCodeDTO> results = qrCodeService.batchGenerateQrCodes(mid, request);
        return ApiResponse.success("批量二维码生成成功", results);
    }

    /**
     * 绑定二维码到商户
     * 对应 Node: POST /api/admin/qrcode/bind
     */
    @PostMapping("/bind")
    public ApiResponse<QrCodeDTO> bindQrCode(@Valid @RequestBody BindQrCodeRequest request) {
        log.info("绑定二维码 | deviceId: {} | merchantId: {}", request.getDeviceId(), request.getMerchantId());

        QrCodeDTO result = qrCodeService.bindQrCode(request);
        return ApiResponse.success("二维码绑定成功", result);
    }

    /**
     * 查询二维码详情
     * 对应 Node: GET /api/admin/qrcode/:id
     */
    @GetMapping("/{id}")
    public ApiResponse<QrCodeDTO> getById(@PathVariable Long id) {
        log.info("查询二维码详情 | id: {}", id);

        QrCodeDTO result = qrCodeService.getById(id);
        return ApiResponse.success("二维码详情查询成功", result);
    }

    /**
     * 查询二维码列表
     * 对应 Node: GET /api/admin/qrcode/list
     * - ADMIN/SUPER_ADMIN：可通过 merchantId 参数指定查询的商户
     * - MERCHANT：从 token 取自己的 merchantId
     */
    @GetMapping("/list")
    public ApiResponse<List<QrCodeDTO>> list(@RequestParam(required = false) Long merchantId) {
        Long mid = resolveMerchantId(merchantId);
        log.info("查询二维码列表 | merchantId: {}", mid);

        List<QrCodeDTO> results = qrCodeService.listByMerchantId(mid);
        return ApiResponse.success("二维码列表查询成功", results);
    }

    /**
     * 删除二维码
     * 对应 Node: DELETE /api/admin/qrcode/:id
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        log.info("删除二维码 | id: {}", id);

        qrCodeService.delete(id);
        return ApiResponse.success("二维码删除成功");
    }

    /**
     * 更新二维码状态
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("更新二维码状态 | id: {} | status: {}", id, status);

        qrCodeService.updateStatus(id, status);
        return ApiResponse.success("状态更新成功");
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
