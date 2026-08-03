package com.quicktap.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; /**
 * 更新语料库分类请求
 */
@Data
public class UpdateCorpusCategoryRequest {
    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "排序顺序不能为空")
    private Integer sortOrder;

    private Boolean enabled;
}
