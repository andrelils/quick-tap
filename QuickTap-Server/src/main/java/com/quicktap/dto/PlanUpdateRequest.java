package com.quicktap.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * 更新套餐请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanUpdateRequest {
    private String name;
    private String level;
    private BigDecimal price;
    private Integer durationMonths;
    private Integer deviceCount;
    private Integer textQuota;
    private Integer imageQuota;
    private Integer videoQuota;
    private Long storageLimit;
    private Integer recommend;
    private Integer status;
}
