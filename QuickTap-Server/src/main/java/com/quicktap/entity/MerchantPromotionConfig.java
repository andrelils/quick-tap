package com.quicktap.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 商户推广配置表
 */
@Entity
@Table(name = "merchant_promotion_config", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"merchant_id", "type", "platform_id", "coupon_id"})
}, indexes = {
    @Index(name = "idx_merchant_id", columnList = "merchant_id"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_platform_id", columnList = "platform_id"),
    @Index(name = "idx_coupon_id", columnList = "coupon_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_merchant_config_status", columnList = "merchant_id,status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantPromotionConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "type", length = 32, nullable = false)
    private String type; // "platform" or "coupon"

    @Column(name = "platform_id")
    private Long platformId;

    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "params", columnDefinition = "JSON")
    private String params; // JSON formatted parameters

    @Column(name = "custom_name", length = 128)
    private String customName;

    @Column(name = "custom_icon", length = 255)
    private String customIcon;

    @Column(name = "sort")
    private Integer sort;

    @Column(name = "status")
    private Integer status; // 0=disabled, 1=enabled

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * 获取参数JSON对象
     */
    public JSONObject getParamsJson() {
        if (params == null) {
            return new JSONObject();
        }
        return JSON.parseObject(params);
    }

    /**
     * 设置参数JSON对象
     */
    public void setParamsJson(JSONObject obj) {
        this.params = obj.toJSONString();
    }
}
