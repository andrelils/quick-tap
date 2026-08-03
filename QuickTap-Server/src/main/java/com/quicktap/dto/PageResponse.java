package com.quicktap.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页响应封装类
 * 用于处理分页数据返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

    /**
     * 分页列表数据
     */
    private List<T> list;

    /**
     * 当前页码（从1开始）
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer totalPage;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    private Boolean hasPrev;

    /**
     * 创建分页响应
     */
    public static <T> PageResponse<T> of(List<T> list, Integer pageNum, Integer pageSize, Long total) {
        int totalPage = (int) ((total + pageSize - 1) / pageSize);
        return PageResponse.<T>builder()
            .list(list)
            .pageNum(pageNum)
            .pageSize(pageSize)
            .total(total)
            .totalPage(totalPage)
            .hasNext(pageNum < totalPage)
            .hasPrev(pageNum > 1)
            .build();
    }

    /**
     * 创建空分页响应
     */
    public static <T> PageResponse<T> empty(Integer pageNum, Integer pageSize) {
        return PageResponse.<T>builder()
            .list(new ArrayList<>())
            .pageNum(pageNum)
            .pageSize(pageSize)
            .total(0L)
            .totalPage(0)
            .hasNext(false)
            .hasPrev(false)
            .build();
    }

    /**
     * 判断是否为空
     */
    public boolean isEmpty() {
        return list == null || list.isEmpty();
    }

    /**
     * 获取记录数
     */
    public Integer getSize() {
        return list == null ? 0 : list.size();
    }
}
