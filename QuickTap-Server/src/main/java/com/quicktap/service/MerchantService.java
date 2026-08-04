package com.quicktap.service;

import com.quicktap.constant.Constants;
import com.quicktap.dto.MerchantCreateRequest;
import com.quicktap.dto.MerchantUpdateRequest;
import com.quicktap.entity.Merchant;
import com.quicktap.exception.BusinessException;
import com.quicktap.mapper.MerchantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商户管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantMapper merchantMapper;

    /**
     * 获取商户列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 商户列表
     */
    public List<Merchant> getMerchantList(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum <= 0) {
            pageNum = Constants.DEFAULT_PAGE_NUM;
        }
        if (pageSize == null || pageSize <= 0 || pageSize > Constants.MAX_PAGE_SIZE) {
            pageSize = Constants.DEFAULT_PAGE_SIZE;
        }

        int offset = (pageNum - 1) * pageSize;
        return merchantMapper.selectPage(offset, pageSize);
    }

    /**
     * 获取商户总数
     * @return 总数
     */
    public Long getMerchantCount() {
        int count = merchantMapper.countAll();
        return (long) count;
    }

    /**
     * 按审核状态获取商户列表
     * @param auditStatus 审核状态
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 商户列表
     */
    public List<Merchant> getMerchantByAuditStatus(Integer auditStatus, Integer pageNum, Integer pageSize) {
        if (auditStatus == null) {
            throw new BusinessException(400, "审核状态不能为空");
        }

        if (pageNum == null || pageNum <= 0) {
            pageNum = Constants.DEFAULT_PAGE_NUM;
        }
        if (pageSize == null || pageSize <= 0 || pageSize > Constants.MAX_PAGE_SIZE) {
            pageSize = Constants.DEFAULT_PAGE_SIZE;
        }

        int offset = (pageNum - 1) * pageSize;
        return merchantMapper.selectByAuditStatusPage(auditStatus, offset, pageSize);
    }

    /**
     * 获取商户详情
     * @param id 商户 ID
     * @return 商户详情
     */
    public Merchant getMerchantById(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "商户 ID 不能为空");
        }

        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(404, "商户不存在");
        }

        return merchant;
    }

    /**
     * 创建商户
     * @param request 创建请求
     * @return 创建后的商户
     */
    public Merchant createMerchant(MerchantCreateRequest request) {
        // 验证参数
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException(400, "商户名称不能为空");
        }
        if (request.getContactName() == null || request.getContactName().trim().isEmpty()) {
            throw new BusinessException(400, "联系人不能为空");
        }
        if (request.getContactPhone() == null || request.getContactPhone().trim().isEmpty()) {
            throw new BusinessException(400, "联系电话不能为空");
        }

        // 创建商户
        Merchant merchant = new Merchant();
        merchant.setName(request.getName());
        merchant.setLogo(request.getLogo());
        merchant.setContactName(request.getContactName());
        merchant.setContactPhone(request.getContactPhone());
        merchant.setContactEmail(request.getContactEmail());
        merchant.setWifiName(request.getWifiName());
        merchant.setWifiPassword(request.getWifiPassword());
        merchant.setAuditStatus(Constants.MERCHANT_AUDIT_PENDING);  // 待审核
        merchant.setStatus(Constants.MERCHANT_STATUS_NORMAL);
        merchant.setCreatedAt(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());

        int result = merchantMapper.insert(merchant);
        if (result <= 0) {
            throw new BusinessException(500, "创建商户失败");
        }

        log.info("创建商户成功: id={}, name={}", merchant.getId(), merchant.getName());
        return merchant;
    }

    /**
     * 更新商户信息
     * @param id 商户 ID
     * @param request 更新请求
     * @return 更新后的商户
     */
    public Merchant updateMerchant(Integer id, MerchantUpdateRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "商户 ID 不能为空");
        }

        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(404, "商户不存在");
        }

        // 更新基本信息
        if (request.getName() != null && !request.getName().isEmpty()) {
            merchant.setName(request.getName());
        }
        if (request.getLogo() != null) {
            merchant.setLogo(request.getLogo());
        }
        if (request.getContactName() != null && !request.getContactName().isEmpty()) {
            merchant.setContactName(request.getContactName());
        }
        if (request.getContactPhone() != null && !request.getContactPhone().isEmpty()) {
            merchant.setContactPhone(request.getContactPhone());
        }
        if (request.getContactEmail() != null) {
            merchant.setContactEmail(request.getContactEmail());
        }
        if (request.getWifiName() != null) {
            merchant.setWifiName(request.getWifiName());
        }
        if (request.getWifiPassword() != null) {
            merchant.setWifiPassword(request.getWifiPassword());
        }

        merchant.setUpdatedAt(LocalDateTime.now());

        int result = merchantMapper.update(merchant);
        if (result <= 0) {
            throw new BusinessException(500, "更新商户失败");
        }

        log.info("更新商户成功: id={}", id);
        return merchant;
    }

    /**
     * 审核通过商户
     * @param id 商户 ID
     */
    public void approveMerchant(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "商户 ID 不能为空");
        }

        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(404, "商户不存在");
        }

        merchant.setAuditStatus(Constants.MERCHANT_AUDIT_APPROVED);
        merchant.setStatus(Constants.MERCHANT_STATUS_NORMAL);
        merchant.setUpdatedAt(LocalDateTime.now());

        int result = merchantMapper.update(merchant);
        if (result <= 0) {
            throw new BusinessException(500, "审核失败");
        }

        log.info("商户审核通过: id={}", id);
    }

    /**
     * 审核拒绝商户
     * @param id 商户 ID
     */
    public void rejectMerchant(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "商户 ID 不能为空");
        }

        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(404, "商户不存在");
        }

        merchant.setAuditStatus(Constants.MERCHANT_AUDIT_REJECTED);
        merchant.setUpdatedAt(LocalDateTime.now());

        int result = merchantMapper.update(merchant);
        if (result <= 0) {
            throw new BusinessException(500, "审核失败");
        }

        log.info("商户审核拒绝: id={}", id);
    }

    /**
     * 禁用商户
     * @param id 商户 ID
     */
    public void disableMerchant(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "商户 ID 不能为空");
        }

        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(404, "商户不存在");
        }

        merchant.setStatus(Constants.MERCHANT_STATUS_SUSPENDED);
        merchant.setUpdatedAt(LocalDateTime.now());

        int result = merchantMapper.update(merchant);
        if (result <= 0) {
            throw new BusinessException(500, "禁用商户失败");
        }

        log.info("禁用商户成功: id={}", id);
    }

    /**
     * 启用商户
     * @param id 商户 ID
     */
    public void enableMerchant(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "商户 ID 不能为空");
        }

        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(404, "商户不存在");
        }

        merchant.setStatus(Constants.MERCHANT_STATUS_NORMAL);
        merchant.setUpdatedAt(LocalDateTime.now());

        int result = merchantMapper.update(merchant);
        if (result <= 0) {
            throw new BusinessException(500, "启用商户失败");
        }

        log.info("启用商户成功: id={}", id);
    }

    /**
     * 删除商户
     * @param id 商户 ID
     */
    public void deleteMerchant(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "商户 ID 不能为空");
        }

        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(404, "商户不存在");
        }

        int result = merchantMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(500, "删除商户失败");
        }

        log.info("删除商户成功: id={}", id);
    }
}
