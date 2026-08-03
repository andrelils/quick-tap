package com.quicktap.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; /**
 * 创建语料库分类请求
 */
@Data
public class CreateCorpusCategoryRequest {
    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "排序顺序不能为空")
    private Integer sortOrder;

    private Boolean enabled = true;
}
