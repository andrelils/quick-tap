package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 套餐表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plan extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String name;            // 套餐名称
    private String level;           // 套餐等级: basic/pro/enterprise
    private java.math.BigDecimal price;     // 套餐价格
    private Integer durationMonths; // 购买时长(月)
    private Integer deviceCount;    // 设备数量限制
    private Integer textQuota;      // 文字生成额度
    private Integer imageQuota;     // 图片生成额度
    private Integer videoQuota;     // 视频生成额度
    private Long storageLimit;      // 存储空间限制(MB)
    private Integer recommend;      // 是否推荐: 0否/1是
    private Integer status;         // 状态：1启用/0停用
}
