package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * 创建套餐请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanCreateRequest {
    @NotBlank(message = "套餐名称不能为空")
    private String name;

    @NotBlank(message = "套餐等级不能为空")
    private String level;  // basic/pro/enterprise

    @NotNull(message = "套餐价格不能为空")
    @DecimalMin(value = "0", message = "套餐价格不能为负数")
    private BigDecimal price;

    @NotNull(message = "购买时长不能为空")
    @Positive(message = "购买时长必须大于0")
    private Integer durationMonths;

    @NotNull(message = "设备数量限制不能为空")
    @Positive(message = "设备数量限制必须大于0")
    private Integer deviceCount;

    @PositiveOrZero(message = "文字生成额度不能为负数")
    private Integer textQuota;

    @PositiveOrZero(message = "图片生成额度不能为负数")
    private Integer imageQuota;

    @PositiveOrZero(message = "视频生成额度不能为负数")
    private Integer videoQuota;

    @PositiveOrZero(message = "存储空间限制不能为负数")
    private Long storageLimit;

    private Integer recommend;      // 是否推荐: 0否/1是
    private Integer sort;           // 排序
    private String description;     // 套餐描述
    private Integer status;         // 状态：1启用/0停用
}
