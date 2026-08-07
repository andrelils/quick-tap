package com.quicktap.service;

import com.quicktap.entity.Plan;
import com.quicktap.mapper.PlanMapper;
import com.quicktap.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 套餐管理业务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanMapper planMapper;

    /**
     * 获取套餐列表（分页）
     */
    public PageResponse<Plan> getPlanList(Integer pageNum, Integer pageSize) {
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.max(Math.min(pageSize, 100), 1);
        int offset = (pageNum - 1) * pageSize;

        List<Plan> list = planMapper.selectPage(offset, pageSize);
        long total = planMapper.countAll();

        return PageResponse.of(list, pageNum, pageSize, total);
    }

    /**
     * 获取所有套餐（不分页）
     */
    public List<Plan> getAllPlans() {
        return planMapper.selectAll();
    }

    /**
     * 获取套餐详情
     */
    public Plan getPlanById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("套餐ID不能为空");
        }
        Plan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new IllegalArgumentException("套餐不存在");
        }
        return plan;
    }

    /**
     * 按等级获取套餐
     */
    public List<Plan> getPlanByLevel(String level) {
        if (level == null || level.trim().isEmpty()) {
            throw new IllegalArgumentException("套餐等级不能为空");
        }
        return planMapper.selectByLevel(level);
    }

    /**
     * 获取推荐套餐
     */
    public List<Plan> getRecommendedPlans() {
        return planMapper.selectRecommended();
    }

    /**
     * 获取启用的套餐
     */
    public List<Plan> getEnabledPlans() {
        return planMapper.selectByStatus(1);
    }

    /**
     * 创建套餐
     */
    public Plan createPlan(Plan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("套餐信息不能为空");
        }
        if (plan.getName() == null || plan.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("套餐名称不能为空");
        }
        if (plan.getLevel() == null || plan.getLevel().trim().isEmpty()) {
            throw new IllegalArgumentException("套餐等级不能为空");
        }
        if (plan.getPrice() == null || plan.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("套餐价格不能为空或为负数");
        }
        if (plan.getDurationMonths() == null || plan.getDurationMonths() <= 0) {
            throw new IllegalArgumentException("购买时长必须大于0");
        }
        if (plan.getDeviceCount() == null || plan.getDeviceCount() <= 0) {
            throw new IllegalArgumentException("设备数量限制必须大于0");
        }

        if (plan.getStatus() == null) {
            plan.setStatus(1);
        }
        if (plan.getRecommend() == null) {
            plan.setRecommend(0);
        }

        planMapper.insert(plan);
        log.info("创建套餐成功, name: {}, level: {}, price: {}", plan.getName(), plan.getLevel(), plan.getPrice());
        return plan;
    }

    /**
     * 更新套餐
     */
    public Plan updatePlan(Integer id, Plan plan) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("套餐ID不能为空");
        }
        Plan existing = planMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("套餐不存在");
        }

        if (plan.getName() != null && !plan.getName().isEmpty()) {
            existing.setName(plan.getName());
        }
        if (plan.getLevel() != null && !plan.getLevel().isEmpty()) {
            existing.setLevel(plan.getLevel());
        }
        if (plan.getPrice() != null && plan.getPrice().compareTo(java.math.BigDecimal.ZERO) >= 0) {
            existing.setPrice(plan.getPrice());
        }
        if (plan.getDurationMonths() != null && plan.getDurationMonths() > 0) {
            existing.setDurationMonths(plan.getDurationMonths());
        }
        if (plan.getDeviceCount() != null && plan.getDeviceCount() > 0) {
            existing.setDeviceCount(plan.getDeviceCount());
        }
        if (plan.getTextQuota() != null) {
            existing.setTextQuota(plan.getTextQuota());
        }
        if (plan.getImageQuota() != null) {
            existing.setImageQuota(plan.getImageQuota());
        }
        if (plan.getVideoQuota() != null) {
            existing.setVideoQuota(plan.getVideoQuota());
        }
        if (plan.getStorageLimit() != null) {
            existing.setStorageLimit(plan.getStorageLimit());
        }
        if (plan.getRecommend() != null) {
            existing.setRecommend(plan.getRecommend());
        }
        if (plan.getStatus() != null) {
            existing.setStatus(plan.getStatus());
        }
        if (plan.getSort() != null) {
            existing.setSort(plan.getSort());
        }
        if (plan.getDescription() != null) {
            existing.setDescription(plan.getDescription());
        }

        planMapper.update(existing);
        log.info("更新套餐成功, id: {}", id);
        return existing;
    }

    /**
     * 删除套餐
     */
    public void deletePlan(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("套餐ID不能为空");
        }
        Plan existing = planMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("套餐不存在");
        }
        planMapper.deleteById(id);
        log.info("删除套餐成功, id: {}", id);
    }

    /**
     * 禁用套餐
     */
    public Plan disablePlan(Integer id) {
        Plan plan = getPlanById(id);
        plan.setStatus(0);
        planMapper.update(plan);
        log.info("禁用套餐成功, id: {}", id);
        return plan;
    }

    /**
     * 启用套餐
     */
    public Plan enablePlan(Integer id) {
        Plan plan = getPlanById(id);
        plan.setStatus(1);
        planMapper.update(plan);
        log.info("启用套餐成功, id: {}", id);
        return plan;
    }

    /**
     * 设置套餐为推荐
     */
    public Plan setRecommended(Integer id) {
        Plan plan = getPlanById(id);
        plan.setRecommend(1);
        planMapper.update(plan);
        log.info("设置套餐为推荐成功, id: {}", id);
        return plan;
    }

    /**
     * 取消推荐
     */
    public Plan unsetRecommended(Integer id) {
        Plan plan = getPlanById(id);
        plan.setRecommend(0);
        planMapper.update(plan);
        log.info("取消推荐成功, id: {}", id);
        return plan;
    }
}
