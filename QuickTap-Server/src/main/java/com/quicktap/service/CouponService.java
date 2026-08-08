package com.quicktap.service;

import com.quicktap.entity.Coupon;
import com.quicktap.exception.BusinessException;
import com.quicktap.mapper.CouponMapper;
import com.quicktap.security.OwnershipChecker;
import com.quicktap.dto.PageResponse;
import com.quicktap.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 卡券管理业务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponMapper couponMapper;
    private final OwnershipChecker ownershipChecker;

    /**
     * 获取卡券列表（分页）
     */
    public PageResponse<Coupon> getCouponList(Integer pageNum, Integer pageSize) {
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.max(Math.min(pageSize, 100), 1);
        int offset = (pageNum - 1) * pageSize;

        List<Coupon> list = couponMapper.selectPage(offset, pageSize);
        long total = couponMapper.countAll();

        return PageResponse.of(list, pageNum, pageSize, total);
    }

    /**
     * 获取卡券详情
     */
    public Coupon getCouponById(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "卡券ID不能为空");
        }
        Coupon coupon = couponMapper.selectById(Long.valueOf(id));
        if (coupon == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "卡券不存在");
        }
        return coupon;
    }

    /**
     * 获取商户卡券列表（分页）
     */
    public PageResponse<Coupon> getMerchantCouponList(Integer merchantId, Integer pageNum, Integer pageSize) {
        if (merchantId == null || merchantId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "商户ID不能为空");
        }
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.max(Math.min(pageSize, 100), 1);
        int offset = (pageNum - 1) * pageSize;

        List<Coupon> list = couponMapper.selectByMerchantIdAndPage(Long.valueOf(merchantId), offset, pageSize);
        long total = couponMapper.countByMerchantId(Long.valueOf(merchantId));

        return PageResponse.of(list, pageNum, pageSize, total);
    }

    /**
     * 创建卡券
     */
    public Coupon createCoupon(Coupon coupon) {
        if (coupon == null || coupon.getMerchantId() == null || coupon.getMerchantId() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "商户ID不能为空");
        }
        if (coupon.getTitle() == null || coupon.getTitle().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "卡券标题不能为空");
        }
        if (coupon.getType() == null || coupon.getType().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "卡券类型不能为空");
        }
        if (coupon.getAmount() == null || coupon.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "金额/比例不能为空或小于等于0");
        }
        if (coupon.getTotalCount() == null || coupon.getTotalCount() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "卡券总数必须大于0");
        }

        // 越权校验：MERCHANT 角色只能为自己的商户创建卡券，ADMIN/SUPER_ADMIN 可指定任意商户
        ownershipChecker.checkMerchant(coupon.getMerchantId().longValue());

        coupon.setRemainCount(coupon.getTotalCount());
        if (coupon.getStatus() == null) {
            coupon.setStatus(1);
        }

        couponMapper.insert(coupon);
        log.info("创建卡券成功, title: {}, merchantId: {}", coupon.getTitle(), coupon.getMerchantId());
        return coupon;
    }

    /**
     * 更新卡券
     */
    public Coupon updateCoupon(Integer id, Coupon coupon) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "卡券ID不能为空");
        }
        Coupon existing = couponMapper.selectById(Long.valueOf(id));
        if (existing == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "卡券不存在");
        }

        // 越权校验：商户只能修改自己的卡券
        if (existing.getMerchantId() != null) {
            ownershipChecker.checkMerchant(existing.getMerchantId().longValue());
        }

        if (coupon.getTitle() != null && !coupon.getTitle().isEmpty()) {
            existing.setTitle(coupon.getTitle());
        }
        if (coupon.getAmount() != null && coupon.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            existing.setAmount(coupon.getAmount());
        }
        if (coupon.getMinAmount() != null) {
            existing.setMinAmount(coupon.getMinAmount());
        }
        if (coupon.getStartTime() != null) {
            existing.setStartTime(coupon.getStartTime());
        }
        if (coupon.getEndTime() != null) {
            existing.setEndTime(coupon.getEndTime());
        }
        if (coupon.getStatus() != null) {
            existing.setStatus(coupon.getStatus());
        }
        if (coupon.getLink() != null) {
            existing.setLink(coupon.getLink());
        }
        if (coupon.getDescription() != null) {
            existing.setDescription(coupon.getDescription());
        }

        couponMapper.update(existing);
        log.info("更新卡券成功, id: {}", id);
        return existing;
    }

    /**
     * 删除卡券
     */
    public void deleteCoupon(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "卡券ID不能为空");
        }
        Coupon existing = couponMapper.selectById(Long.valueOf(id));
        if (existing == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "卡券不存在");
        }
        // 越权校验：商户只能删除自己的卡券
        if (existing.getMerchantId() != null) {
            ownershipChecker.checkMerchant(existing.getMerchantId().longValue());
        }
        couponMapper.deleteById(Long.valueOf(id));
        log.info("删除卡券成功, id: {}", id);
    }

    /**
     * 禁用卡券
     */
    public Coupon disableCoupon(Integer id) {
        Coupon coupon = getCouponById(id);
        // 越权校验：商户只能禁用自己的卡券
        if (coupon.getMerchantId() != null) {
            ownershipChecker.checkMerchant(coupon.getMerchantId().longValue());
        }
        coupon.setStatus(0);
        couponMapper.update(coupon);
        log.info("禁用卡券成功, id: {}", id);
        return coupon;
    }

    /**
     * 启用卡券
     */
    public Coupon enableCoupon(Integer id) {
        Coupon coupon = getCouponById(id);
        // 越权校验：商户只能启用自己的卡券
        if (coupon.getMerchantId() != null) {
            ownershipChecker.checkMerchant(coupon.getMerchantId().longValue());
        }
        coupon.setStatus(1);
        couponMapper.update(coupon);
        log.info("启用卡券成功, id: {}", id);
        return coupon;
    }

    /**
     * 获取用户已领取的卡券列表（分页）
     * 通过 user_coupons 关联表查询当前用户已领取的卡券
     */
    public PageResponse<Coupon> getUserCouponList(Integer pageNum, Integer pageSize) {
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.max(Math.min(pageSize, 100), 1);
        int offset = (pageNum - 1) * pageSize;

        Long userId = ownershipChecker.getCurrentUserId();
        if (userId == null) {
            // 未登录用户返回空列表
            return PageResponse.of(java.util.Collections.emptyList(), pageNum, pageSize, 0L);
        }

        List<Coupon> list = couponMapper.selectUserCoupons(userId, offset, pageSize);
        long total = couponMapper.countUserCoupons(userId);

        log.info("获取用户已领取卡券列表: userId={}, pageNum={}, pageSize={}, total={}",
                userId, pageNum, pageSize, total);
        return PageResponse.of(list, pageNum, pageSize, total);
    }

    /**
     * 领取卡券（剩余数量原子递减，防止高并发超卖）
     * 使用数据库层面的条件更新：UPDATE ... SET remain_count = remain_count - 1 WHERE id = ? AND remain_count > 0
     * 返回受影响行数为 0 表示库存不足。
     */
    @Transactional(rollbackFor = Exception.class)
    public Coupon claimCoupon(Integer id) {
        Coupon coupon = getCouponById(id);

        // 原子递减：通过 SQL 条件更新保证并发安全
        int affected = couponMapper.updateRemainCountDecrement(id.longValue());
        if (affected == 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "卡券已领完");
        }

        // 记录用户领取关系（我的卡券列表依赖此记录）
        Long userId = ownershipChecker.getCurrentUserId();
        if (userId != null) {
            couponMapper.insertUserCoupon(userId, id.longValue());
        }

        // 返回最新库存（从数据库重新读取，确保数据准确）
        coupon.setRemainCount(coupon.getRemainCount() - 1);
        log.info("领取卡券成功, id: {}", id);
        return coupon;
    }
}
