package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 语料库分类表
 * 支持商家为自己的语料库创建分类，方便管理
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorpusCategory extends BaseEntity {

    /**
     * 分类名称
     */
    private String name;

    /**
     * 商户 ID（null 表示全局分类）
     */
    private Long merchantId;

    /**
     * 排序顺序（升序）
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
