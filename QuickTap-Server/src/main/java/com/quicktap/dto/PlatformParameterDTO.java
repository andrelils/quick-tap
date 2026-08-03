package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推广平台参数定义DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformParameterDTO {
    private String key;                    // 参数键
    private String label;                  // 参数标签
    private String placeholder;            // 占位符提示
    private Boolean required;              // 是否必需
    private String description;            // 参数描述
}
