package com.quicktap.enums;

/**
 * 卡券状态枚举
 */
public enum CouponStatus {
    UNUSED(1, "未使用"),
    USED(2, "已使用"),
    EXPIRED(3, "已过期");

    private final Integer code;
    private final String name;

    CouponStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static CouponStatus fromCode(Integer code) {
        for (CouponStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
