package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String orderNo;         // 订单号（唯一）
    private Integer merchantId;     // 商户ID
    private Integer planId;         // 套餐ID
    private java.math.BigDecimal amount;  // 订单金额
    private String status;          // 订单状态: pending/paid/expired
    private java.time.LocalDateTime expireAt;  // 订单过期时间
}
