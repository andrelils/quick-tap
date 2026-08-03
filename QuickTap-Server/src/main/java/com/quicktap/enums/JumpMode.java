package com.quicktap.enums;

/**
 * 推广跳转模式枚举
 */
public enum JumpMode {
    SCHEME("scheme", "Scheme跳转"),
    WEBVIEW("webview", "H5跳转"),
    MINIPROGRAM("miniprogram", "小程序跳转"),
    COPY("copy", "复制链接");

    private final String code;
    private final String name;

    JumpMode(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static JumpMode fromCode(String code) {
        for (JumpMode mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        return null;
    }
}
