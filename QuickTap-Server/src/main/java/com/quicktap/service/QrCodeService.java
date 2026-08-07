package com.quicktap.service;

import com.quicktap.dto.QrCodeDTO;
import com.quicktap.dto.GenerateQrCodeRequest;
import com.quicktap.dto.BatchGenerateQrCodeRequest;
import com.quicktap.dto.BindQrCodeRequest;
import com.quicktap.entity.QrCode;
import com.quicktap.exception.BusinessException;
import com.quicktap.mapper.QrCodeMapper;
import com.quicktap.utils.QrCodeGeneratorUtil;
import com.quicktap.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 二维码 Service
 */
@Slf4j
@Service
public class QrCodeService {

    @Autowired
    private QrCodeMapper qrCodeMapper;

    @Autowired
    private QrCodeGeneratorUtil qrCodeGeneratorUtil;

    /**
     * 生成单个二维码
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "qr_codes", allEntries = true)
    public QrCodeDTO generateQrCode(Long merchantId, GenerateQrCodeRequest request) {
        log.info("生成二维码 | merchantId: {} | deviceId: {}", merchantId, request.getDeviceId());

        String code = UUID.randomUUID().toString();
        String qrImageUrl = qrCodeGeneratorUtil.generateQrCode(request.getQrData());

        QrCode qrCode = QrCode.builder()
                .code(code)
                .deviceId(request.getDeviceId())
                .merchantId(merchantId)
                .qrData(request.getQrData())
                .qrImageUrl(qrImageUrl)
                .type(request.getType())
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        qrCodeMapper.insert(qrCode);
        log.info("二维码生成成功 | code: {}", code);

        return convertToDTO(qrCode);
    }

    /**
     * 批量生成二维码
     * 使用 batchInsert 一次性插入，减少数据库往返次数，提升批量场景性能
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "qr_codes", allEntries = true)
    public List<QrCodeDTO> batchGenerateQrCodes(Long merchantId, BatchGenerateQrCodeRequest request) {
        log.info("批量生成二维码 | merchantId: {} | count: {}", merchantId, request.getDeviceIds().size());

        List<QrCode> qrCodes = new ArrayList<>(request.getDeviceIds().size());
        for (Long deviceId : request.getDeviceIds()) {
            String code = UUID.randomUUID().toString();
            String qrImageUrl = qrCodeGeneratorUtil.generateQrCode(code);

            QrCode qrCode = QrCode.builder()
                    .code(code)
                    .deviceId(deviceId)
                    .merchantId(merchantId)
                    .qrData(code)
                    .qrImageUrl(qrImageUrl)
                    .type(request.getType())
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            qrCodes.add(qrCode);
        }

        // 一次性批量插入
        qrCodeMapper.batchInsert(qrCodes);
        log.info("批量二维码生成成功 | count: {}", qrCodes.size());

        return qrCodes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 绑定二维码到商户
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "qr_codes", allEntries = true)
    public QrCodeDTO bindQrCode(BindQrCodeRequest request) {
        log.info("绑定二维码 | deviceId: {} | merchantId: {}", request.getDeviceId(), request.getMerchantId());

        List<QrCode> qrCodes = qrCodeMapper.selectByDeviceId(request.getDeviceId());
        if (qrCodes.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "该设备未生成二维码");
        }

        // 绑定第一个二维码
        QrCode qrCode = qrCodes.get(0);
        qrCode.setMerchantId(request.getMerchantId());
        qrCode.setUpdatedAt(LocalDateTime.now());
        qrCodeMapper.update(qrCode);

        log.info("二维码绑定成功 | code: {} | merchantId: {}", qrCode.getCode(), request.getMerchantId());
        return convertToDTO(qrCode);
    }

    /**
     * 查询二维码详情
     */
    @Cacheable(value = "qr_code", key = "#id", unless = "#result == null")
    public QrCodeDTO getById(Long id) {
        QrCode qrCode = qrCodeMapper.selectById(id);
        if (qrCode == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "二维码不存在");
        }
        return convertToDTO(qrCode);
    }

    /**
     * 按编码查询
     */
    @Cacheable(value = "qr_code_by_code", key = "#code", unless = "#result == null")
    public QrCodeDTO getByCode(String code) {
        QrCode qrCode = qrCodeMapper.selectByCode(code);
        if (qrCode == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "二维码不存在");
        }
        return convertToDTO(qrCode);
    }

    /**
     * 查询商户的二维码列表
     */
    @Cacheable(value = "qr_codes_merchant", key = "#merchantId", unless = "#result == null")
    public List<QrCodeDTO> listByMerchantId(Long merchantId) {
        List<QrCode> qrCodes = qrCodeMapper.selectByMerchantId(merchantId);
        return qrCodes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 删除二维码
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"qr_codes", "qr_code", "qr_code_by_code"}, allEntries = true)
    public void delete(Long id) {
        log.info("删除二维码 | id: {}", id);

        int deleted = qrCodeMapper.deleteById(id);
        if (deleted == 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "二维码删除失败");
        }
    }

    /**
     * 更新二维码状态
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"qr_codes", "qr_code", "qr_code_by_code"}, allEntries = true)
    public void updateStatus(Long id, String status) {
        qrCodeMapper.updateStatus(id, status);
    }

    /**
     * 转换为 DTO
     */
    private QrCodeDTO convertToDTO(QrCode qrCode) {
        return QrCodeDTO.builder()
                .id(Long.valueOf(qrCode.getId()))
                .code(qrCode.getCode())
                .deviceId(qrCode.getDeviceId())
                .merchantId(qrCode.getMerchantId())
                .qrData(qrCode.getQrData())
                .qrImageUrl(qrCode.getQrImageUrl())
                .type(qrCode.getType())
                .status(qrCode.getStatus())
                .createdAt(qrCode.getCreatedAt())
                .updatedAt(qrCode.getUpdatedAt())
                .build();
    }
}
