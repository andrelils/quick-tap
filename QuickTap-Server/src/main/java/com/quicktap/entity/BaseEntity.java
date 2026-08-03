package com.quicktap.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类 - 所有Entity的父类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    protected Integer id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
}
