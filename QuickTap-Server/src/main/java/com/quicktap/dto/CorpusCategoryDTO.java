package com.quicktap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 语料库分类 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorpusCategoryDTO {

    /**
     * 分类 ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 商户 ID
     */
    private Long merchantId;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 该分类下的语料数量
     */
    private Integer corpusCount;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

