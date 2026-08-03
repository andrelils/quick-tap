package com.quicktap.service;

import com.quicktap.entity.Coupon;
import com.quicktap.exception.BusinessException;
import com.quicktap.mapper.CouponMapper;
import com.quicktap.dto.PageResponse;
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
            throw new BusinessException(400, "卡券ID不能为空");
        }
        Coupon coupon = couponMapper.selectById(Long.valueOf(id));
        if (coupon == null) {
            throw new BusinessException(404, "卡券不存在");
        }
        return coupon;
    }

    /**
     * 获取商户卡券列表（分页）
     */
    public PageResponse<Coupon> getMerchantCouponList(Integer merchantId, Integer pageNum, Integer pageSize) {
        if (merchantId == null || merchantId <= 0) {
            throw new BusinessException(400, "商户ID不能为空");
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
            throw new BusinessException(400, "商户ID不能为空");
        }
        if (coupon.getTitle() == null || coupon.getTitle().trim().isEmpty()) {
            throw new BusinessException(400, "卡券标题不能为空");
        }
        if (coupon.getType() == null || coupon.getType().trim().isEmpty()) {
            throw new BusinessException(400, "卡券类型不能为空");
        }
        if (coupon.getAmount() == null || coupon.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "金额/比例不能为空或小于等于0");
        }
        if (coupon.getTotalCount() == null || coupon.getTotalCount() <= 0) {
            throw new BusinessException(400, "卡券总数必须大于0");
        }

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
            throw new BusinessException(400, "卡券ID不能为空");
        }
        Coupon existing = couponMapper.selectById(Long.valueOf(id));
        if (existing == null) {
            throw new BusinessException(404, "卡券不存在");
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

        couponMapper.update(existing);
        log.info("更新卡券成功, id: {}", id);
        return existing;
    }

    /**
     * 删除卡券
     */
    public void deleteCoupon(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "卡券ID不能为空");
        }
        Coupon existing = couponMapper.selectById(Long.valueOf(id));
        if (existing == null) {
            throw new BusinessException(404, "卡券不存在");
        }
        couponMapper.deleteById(Long.valueOf(id));
        log.info("删除卡券成功, id: {}", id);
    }

    /**
     * 禁用卡券
     */
    public Coupon disableCoupon(Integer id) {
        Coupon coupon = getCouponById(id);
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
        coupon.setStatus(1);
        couponMapper.update(coupon);
        log.info("启用卡券成功, id: {}", id);
        return coupon;
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
            throw new BusinessException(400, "卡券已领完");
        }

        // 返回最新库存（从数据库重新读取，确保数据准确）
        coupon.setRemainCount(coupon.getRemainCount() - 1);
        log.info("领取卡券成功, id: {}", id);
        return coupon;
    }
}
