package com.quicktap.service;

import com.quicktap.constant.Constants;
import com.quicktap.dto.MerchantCreateRequest;
import com.quicktap.dto.MerchantUpdateRequest;
import com.quicktap.entity.Admin;
import com.quicktap.entity.Merchant;
import com.quicktap.exception.BusinessException;
import com.quicktap.mapper.AdminMapper;
import com.quicktap.mapper.MerchantMapper;
import com.quicktap.security.OwnershipChecker;
import com.quicktap.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantMapper merchantMapper;
    private final AdminMapper adminMapper;
    private final OwnershipChecker ownershipChecker;

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
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "审核状态不能为空");
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
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "商户 ID 不能为空");
        }

        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
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
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "商户名称不能为空");
        }
        if (request.getContactName() == null || request.getContactName().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "联系人不能为空");
        }
        if (request.getContactPhone() == null || request.getContactPhone().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "联系电话不能为空");
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
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "创建商户失败");
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
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "商户 ID 不能为空");
        }

        // 越权校验：商户只能修改自己的信息，管理员可改任意商户
        ownershipChecker.checkSelfMerchant(id.longValue());

        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
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

        // C 端展示配置字段（轮播图、店铺图、营业时间等）
        if (request.getAddress() != null) {
            merchant.setAddress(request.getAddress());
        }
        if (request.getBannerImages() != null) {
            merchant.setBannerImages(request.getBannerImages());
        }
        if (request.getShopImages() != null) {
            merchant.setShopImages(request.getShopImages());
        }
        if (request.getBossWechat() != null) {
            merchant.setBossWechat(request.getBossWechat());
        }
        if (request.getBusinessHours() != null) {
            merchant.setBusinessHours(request.getBusinessHours());
        }
        if (request.getReferrerCode() != null) {
            merchant.setReferrerCode(request.getReferrerCode());
        }

        // 管理员专属字段：套餐 ID、存储限制（商户角色传值也会被越权校验放行，但不影响安全）
        if (request.getPlanId() != null) {
            merchant.setPlanId(request.getPlanId());
        }
        if (request.getStorageLimit() != null) {
            merchant.setStorageLimit(request.getStorageLimit());
        }

        merchant.setUpdatedAt(LocalDateTime.now());

        int result = merchantMapper.update(merchant);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "更新商户失败");
        }

        log.info("更新商户成功: id={}", id);
        return merchant;
    }

    /**
     * 审核通过商户
     * 同步启用关联的 admin 账号，保证审核流程状态一致。
     * @param id 商户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveMerchant(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "商户 ID 不能为空");
        }

        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }

        merchant.setAuditStatus(Constants.MERCHANT_AUDIT_APPROVED);
        merchant.setStatus(Constants.MERCHANT_STATUS_NORMAL);
        merchant.setUpdatedAt(LocalDateTime.now());

        int result = merchantMapper.update(merchant);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "审核失败");
        }

        // 同步启用关联的 admin 账号（商家注册时 admin.status=0 待审核）
        enableAdminsByMerchantId(id);

        log.info("商户审核通过: id={}", id);
    }

    /**
     * 审核拒绝商户
     * 同步禁用关联的 admin 账号，防止拒绝后仍可登录。
     * @param id 商户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void rejectMerchant(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "商户 ID 不能为空");
        }

        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }

        merchant.setAuditStatus(Constants.MERCHANT_AUDIT_REJECTED);
        merchant.setUpdatedAt(LocalDateTime.now());

        int result = merchantMapper.update(merchant);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "审核失败");
        }

        // 同步禁用关联的 admin 账号
        disableAdminsByMerchantId(id);

        log.info("商户审核拒绝: id={}", id);
    }

    /**
     * 启用指定商户关联的所有 admin 账号
     */
    private void enableAdminsByMerchantId(Integer merchantId) {
        List<Admin> admins = adminMapper.selectByMerchantId(merchantId);
        for (Admin admin : admins) {
            if (!Constants.ADMIN_STATUS_ENABLED.equals(admin.getStatus())) {
                admin.setStatus(Constants.ADMIN_STATUS_ENABLED);
                adminMapper.update(admin);
                log.info("启用 admin 账号: username={}, merchantId={}", admin.getUsername(), merchantId);
            }
        }
    }

    /**
     * 禁用指定商户关联的所有 admin 账号
     */
    private void disableAdminsByMerchantId(Integer merchantId) {
        List<Admin> admins = adminMapper.selectByMerchantId(merchantId);
        for (Admin admin : admins) {
            if (!Constants.ADMIN_STATUS_DISABLED.equals(admin.getStatus())) {
                admin.setStatus(Constants.ADMIN_STATUS_DISABLED);
                adminMapper.update(admin);
                log.info("禁用 admin 账号: username={}, merchantId={}", admin.getUsername(), merchantId);
            }
        }
    }

    /**
     * 禁用商户
     * @param id 商户 ID
     */
    public void disableMerchant(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "商户 ID 不能为空");
        }

        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }

        merchant.setStatus(Constants.MERCHANT_STATUS_SUSPENDED);
        merchant.setUpdatedAt(LocalDateTime.now());

        int result = merchantMapper.update(merchant);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "禁用商户失败");
        }

        log.info("禁用商户成功: id={}", id);
    }

    /**
     * 启用商户
     * @param id 商户 ID
     */
    public void enableMerchant(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "商户 ID 不能为空");
        }

        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }

        merchant.setStatus(Constants.MERCHANT_STATUS_NORMAL);
        merchant.setUpdatedAt(LocalDateTime.now());

        int result = merchantMapper.update(merchant);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "启用商户失败");
        }

        log.info("启用商户成功: id={}", id);
    }

    /**
     * 删除商户
     * @param id 商户 ID
     */
    public void deleteMerchant(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "商户 ID 不能为空");
        }

        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }

        int result = merchantMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "删除商户失败");
        }

        log.info("删除商户成功: id={}", id);
    }

    /**
     * 按商户 ID 批量查询商户名称映射（id -> name）
     * 用于设备列表等关联查询，避免 Controller 直接操作 JdbcTemplate。
     * @param merchantIds 商户 ID 列表
     * @return id -> name 的映射，空输入返回空 Map
     */
    public Map<Integer, String> getMerchantNameMap(List<Integer> merchantIds) {
        if (merchantIds == null || merchantIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Merchant> merchants = merchantMapper.selectByIds(merchantIds);
        Map<Integer, String> nameMap = new HashMap<>(merchants.size());
        for (Merchant m : merchants) {
            nameMap.put(m.getId(), m.getName());
        }
        return nameMap;
    }
}
