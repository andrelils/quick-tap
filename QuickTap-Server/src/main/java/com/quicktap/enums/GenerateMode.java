package com.quicktap.enums;

/**
 * AI生成模式枚举
 */
public enum GenerateMode {
    NEW("new", "全新创作"),
    SECONDARY("secondary", "二次创作");

    private final String code;
    private final String name;

    GenerateMode(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static GenerateMode fromCode(String code) {
        for (GenerateMode mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        return null;
    }
}
