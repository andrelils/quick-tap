package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 卡券表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Integer merchantId;     // 商户ID
    private String title;           // 卡券标题
    private String type;            // 卡券类型: cash(现金)/discount(折扣)
    private java.math.BigDecimal amount;      // 金额/比例
    private java.math.BigDecimal minAmount;   // 最低消费金额
    private Integer totalCount;     // 总数
    private Integer remainCount;    // 剩余数
    private LocalDateTime startTime;// 有效期开始
    private LocalDateTime endTime;  // 有效期结束
    private Integer status;         // 状态：1启用/0停用
    private String link;            // 第三方平台跳转链接，用户点击后跳转到此链接领取优惠券
}
