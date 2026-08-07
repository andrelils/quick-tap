package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.dto.PageResponse;
import com.quicktap.dto.PlanCreateRequest;
import com.quicktap.entity.Plan;
import com.quicktap.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 套餐管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
@Validated
public class PlanController {
    private final PlanService planService;

    @GetMapping("/list")
    public ApiResponse<PageResponse<Plan>> listPlans(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<Plan> data = planService.getPlanList(pageNum, pageSize);
        return ApiResponse.success("获取成功", data);
    }

    @GetMapping("/all")
    public ApiResponse<List<Plan>> getAllPlans() {
        List<Plan> data = planService.getAllPlans();
        return ApiResponse.success("获取成功", data);
    }

    @GetMapping("/{id}")
    public ApiResponse<Plan> getPlan(@PathVariable @NotNull Integer id) {
        Plan plan = planService.getPlanById(id);
        return ApiResponse.success("获取成功", plan);
    }

    @GetMapping("/level/{level}")
    public ApiResponse<List<Plan>> getPlanByLevel(@PathVariable String level) {
        List<Plan> data = planService.getPlanByLevel(level);
        return ApiResponse.success("获取成功", data);
    }

    @GetMapping("/recommended")
    public ApiResponse<List<Plan>> getRecommendedPlans() {
        List<Plan> data = planService.getRecommendedPlans();
        return ApiResponse.success("获取成功", data);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Plan> createPlan(@Valid @RequestBody PlanCreateRequest request) {
        Plan plan = new Plan();
        plan.setName(request.getName());
        plan.setLevel(request.getLevel());
        plan.setPrice(request.getPrice());
        plan.setDurationMonths(request.getDurationMonths());
        plan.setDeviceCount(request.getDeviceCount());
        plan.setTextQuota(request.getTextQuota());
        plan.setImageQuota(request.getImageQuota());
        plan.setVideoQuota(request.getVideoQuota());
        plan.setStorageLimit(request.getStorageLimit());
        plan.setRecommend(request.getRecommend() != null ? request.getRecommend() : 0);
        plan.setSort(request.getSort() != null ? request.getSort() : 0);
        plan.setDescription(request.getDescription());
        plan.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        Plan created = planService.createPlan(plan);
        return ApiResponse.success("创建成功", created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Plan> updatePlan(
            @PathVariable @NotNull Integer id,
            @Valid @RequestBody PlanCreateRequest request) {
        Plan plan = new Plan();
        plan.setName(request.getName());
        plan.setLevel(request.getLevel());
        plan.setPrice(request.getPrice());
        plan.setDurationMonths(request.getDurationMonths());
        plan.setDeviceCount(request.getDeviceCount());
        plan.setTextQuota(request.getTextQuota());
        plan.setImageQuota(request.getImageQuota());
        plan.setVideoQuota(request.getVideoQuota());
        plan.setStorageLimit(request.getStorageLimit());
        plan.setRecommend(request.getRecommend() != null ? request.getRecommend() : 0);
        plan.setSort(request.getSort() != null ? request.getSort() : 0);
        plan.setDescription(request.getDescription());
        plan.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        Plan updated = planService.updatePlan(id, plan);
        return ApiResponse.success("更新成功", updated);
    }

    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Plan> disablePlan(@PathVariable @NotNull Integer id) {
        Plan plan = planService.disablePlan(id);
        return ApiResponse.success("禁用成功", plan);
    }

    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Plan> enablePlan(@PathVariable @NotNull Integer id) {
        Plan plan = planService.enablePlan(id);
        return ApiResponse.success("启用成功", plan);
    }

    @PutMapping("/{id}/recommend")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Plan> setRecommended(@PathVariable @NotNull Integer id) {
        Plan plan = planService.setRecommended(id);
        return ApiResponse.success("设置推荐成功", plan);
    }

    @PutMapping("/{id}/unrecommend")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Plan> unsetRecommended(@PathVariable @NotNull Integer id) {
        Plan plan = planService.unsetRecommended(id);
        return ApiResponse.success("取消推荐成功", plan);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> deletePlan(@PathVariable @NotNull Integer id) {
        planService.deletePlan(id);
        return ApiResponse.success("删除成功");
    }
}
