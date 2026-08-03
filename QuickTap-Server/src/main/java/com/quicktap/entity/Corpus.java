package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库内容表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Corpus extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String corpusId;           // 内容ID（UUID）
    private Integer merchantId;        // 商户ID
    private String title;              // 标题
    private String content;            // 内容
    private String category;           // 分类
    private String tags;               // 标签（JSON数组）
    private Integer status;            // 状态：1正常/0删除
    private String imageUrl;           // 缩略图URL
    private Integer viewCount;         // 浏览次数
    private String createdBy;          // 创建人
    private String updatedBy;          // 更新人
}
