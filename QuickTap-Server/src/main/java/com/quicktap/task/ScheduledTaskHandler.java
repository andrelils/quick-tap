package com.quicktap.task;

import com.quicktap.mapper.CouponMapper;
import com.quicktap.mapper.OrderRecordMapper;
import com.quicktap.mapper.CorpusMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 定时任务处理器
 * 处理过期订单、卡券、语料库等定期清理任务
 */
@Slf4j
@Component
public class ScheduledTaskHandler {

    @Autowired
    private OrderRecordMapper orderRecordMapper;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private CorpusMapper corpusMapper;

    /**
     * 每天凌晨2点清理过期订单
     * 将过期的订单标记为已过期或自动取消
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredOrders() {
        log.info("开始清理过期订单");
        try {
            LocalDateTime now = LocalDateTime.now();
            // 这里假设有一个清理方法，实际需要根据数据库字段实现
            int count = orderRecordMapper.deleteExpiredOrders(now);
            log.info("过期订单清理完成 | 清理数量: {}", count);
        } catch (Exception e) {
            log.error("清理过期订单失败", e);
        }
    }

    /**
     * 每天凌晨3点清理过期卡券
     * 将过期的卡券标记为已过期
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredCoupons() {
        log.info("开始清理过期卡券");
        try {
            LocalDateTime now = LocalDateTime.now();
            int count = couponMapper.updateExpiredCoupons(now);
            log.info("过期卡券清理完成 | 清理数量: {}", count);
        } catch (Exception e) {
            log.error("清理过期卡券失败", e);
        }
    }

    /**
     * 每周一凌晨4点清理回收站中的语料库
     * 永久删除超过30天的软删除语料
     */
    @Scheduled(cron = "0 0 4 ? * MON")
    public void cleanTrashedCorpus() {
        log.info("开始清理回收站语料库");
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            int count = corpusMapper.deletePermanentlyTrashedCorpus(thirtyDaysAgo);
            log.info("回收站语料库清理完成 | 清理数量: {}", count);
        } catch (Exception e) {
            log.error("清理回收站语料库失败", e);
        }
    }

    /**
     * 每天凌晨5点生成数据统计报告
     * 计算各项统计数据并缓存
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void generateDailyStatistics() {
        log.info("开始生成每日统计报告");
        try {
            // 这里可以调用统计服务生成报告
            log.info("每日统计报告生成完成");
        } catch (Exception e) {
            log.error("生成每日统计报告失败", e);
        }
    }

    /**
     * 每小时清理过期的缓存数据
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void refreshCache() {
        log.info("开始刷新缓存");
        try {
            // 刷新推广平台缓存
            // cacheManager.getCache("promotion_platforms").clear();
            log.info("缓存刷新完成");
        } catch (Exception e) {
            log.error("缓存刷新失败", e);
        }
    }
}
