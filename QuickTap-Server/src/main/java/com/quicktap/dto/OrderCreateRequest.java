package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建订单请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {
    @NotNull(message = "商户ID不能为空")
    private Integer merchantId;

    @NotNull(message = "套餐ID不能为空")
    private Integer planId;

    @NotNull(message = "订单金额不能为空")
    @DecimalMin(value = "0", message = "订单金额不能为负数")
    private BigDecimal amount;
}
