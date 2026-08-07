package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建卡券请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponCreateRequest {
    @NotNull(message = "商户ID不能为空")
    private Integer merchantId;

    @NotBlank(message = "卡券标题不能为空")
    private String title;

    @NotBlank(message = "卡券类型不能为空")
    private String type;  // cash/discount

    @NotNull(message = "金额/比例不能为空")
    @DecimalMin(value = "0.01", message = "金额/比例必须大于0")
    private BigDecimal amount;

    @DecimalMin(value = "0", message = "最低消费金额不能为负")
    private BigDecimal minAmount;

    @NotNull(message = "卡券总数不能为空")
    @Positive(message = "卡券总数必须大于0")
    private Integer totalCount;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @NotBlank(message = "跳转链接不能为空")
    private String link;  // 第三方平台跳转链接

    private String description;  // 使用说明（选填）
}
