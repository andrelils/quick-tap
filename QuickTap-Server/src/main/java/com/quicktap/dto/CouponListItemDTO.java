package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 卡券列表项DTO（包含卡券完整信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponListItemDTO {
    private Long id;
    private Long merchantId;
    private String name;                    // 卡券名称
    private String description;             // 描述
    private Integer type;                   // 类型
    private Long value;                     // 面额(分)
    private Long threshold;                 // 门槛(分)
    private Integer totalCount;             // 总数量
    private Integer remainCount;            // 剩余数量
    private LocalDateTime startTime;        // 开始时间
    private LocalDateTime endTime;          // 结束时间
    private Integer status;                 // 状态
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
