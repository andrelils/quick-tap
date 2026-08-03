package com.quicktap.repository;

import com.quicktap.entity.PromotionClickLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 推广点击日志Repository
 */
@Repository
public interface PromotionClickLogRepository extends JpaRepository<PromotionClickLog, Long> {
    
    /**
     * 根据商户ID查询点击记录
     */
    List<PromotionClickLog> findByMerchantId(Long merchantId);

    /**
     * 根据平台ID查询点击记录
     */
    List<PromotionClickLog> findByPlatformId(Long platformId);

    /**
     * 根据用户ID查询点击记录
     */
    List<PromotionClickLog> findByUserId(Long userId);

    /**
     * 统计商户点击次数
     */
    long countByMerchantId(Long merchantId);

    /**
     * 统计平台点击次数
     */
    long countByPlatformId(Long platformId);

    /**
     * 统计商户在指定时间范围内的点击次数
     */
    long countByMerchantIdAndCreatedAtBetween(Long merchantId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 删除指定日期前的点击记录
     */
    void deleteByCreatedAtBefore(LocalDateTime beforeDate);
}
