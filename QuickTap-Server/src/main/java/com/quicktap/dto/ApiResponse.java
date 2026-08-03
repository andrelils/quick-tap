package com.quicktap.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应封装类
 * 所有 API 响应都使用此类进行包装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 响应状态码
     * 0: 成功
     * 1: 系统错误
     * 400: 请求参数错误
     * 401: 未授权
     * 403: 禁止访问
     * 404: 资源不存在
     * 500: 服务器错误
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
            .code(0)
            .message("请求成功")
            .timestamp(System.currentTimeMillis())
            .build();
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .code(0)
            .message("请求成功")
            .data(data)
            .timestamp(System.currentTimeMillis())
            .build();
    }

    /**
     * 成功响应（自定义消息，无数据）
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
            .code(0)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }

    /**
     * 成功响应（自定义消息和数据）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
            .code(0)
            .message(message)
            .data(data)
            .timestamp(System.currentTimeMillis())
            .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .code(500)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
            .code(code)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }

    /**
     * 失败响应（带数据）
     */
    public static <T> ApiResponse<T> error(Integer code, String message, T data) {
        return ApiResponse.<T>builder()
            .code(code)
            .message(message)
            .data(data)
            .timestamp(System.currentTimeMillis())
            .build();
    }

    /**
     * 参数验证错误
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.<T>builder()
            .code(400)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }

    /**
     * 未授权错误
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.<T>builder()
            .code(401)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }

    /**
     * 禁止访问错误
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return ApiResponse.<T>builder()
            .code(403)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }

    /**
     * 资源不存在错误
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
            .code(404)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }

    /**
     * 系统错误
     */
    public static <T> ApiResponse<T> systemError(String message) {
        return ApiResponse.<T>builder()
            .code(500)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
}
