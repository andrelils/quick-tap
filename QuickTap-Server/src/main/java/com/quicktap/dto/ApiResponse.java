package com.quicktap.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quicktap.common.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Unified API Response Wrapper
 *
 * Provides consistent response format for all API endpoints:
 * - Success responses with data
 * - Error responses with ErrorCode enum
 * - Includes timestamp, trace ID, and status
 *
 * Features:
 * - Supports both legacy integer codes and new ErrorCode enum
 * - Includes HTTP status codes and error details
 * - Automatically includes server timestamp
 * - Optional trace ID for debugging
 *
 * Usage:
 * Success: ApiResponse.success(data)
 * Error: ApiResponse.error(ErrorCode.INVALID_REQUEST, "Additional details")
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Response status code (string format for new error codes)
     * Example: "1000", "2001", "1003"
     */
    private String code;

    /**
     * Response message/description
     */
    private String message;

    /**
     * Response data (only in success responses)
     */
    private T data;

    /**
     * HTTP status code (200, 400, 401, 403, 404, 500, etc.)
     */
    private Integer status;

    /**
     * Additional error details/context
     */
    private String detail;

    /**
     * Server timestamp in ISO-8601 format
     */
    private LocalDateTime timestamp;

    /**
     * Request trace ID (for debugging and request tracking)
     */
    private String traceId;

    /**
     * Create successful response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code("1000")
                .message("Request successful")
                .data(data)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create successful response with custom message and data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code("1000")
                .message(message)
                .data(data)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create successful response without data (legacy support)
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .code("1000")
                .message("Request successful")
                .status(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create successful response with custom message
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .code("1000")
                .message(message)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response with ErrorCode
     * Uses ErrorCode's message and maps to appropriate HTTP status
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return error(errorCode, null, null);
    }

    /**
     * Create error response with ErrorCode and detail message
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String detail) {
        return error(errorCode, detail, null);
    }

    /**
     * Create error response with ErrorCode, detail, and trace ID
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String detail, String traceId) {
        return ApiResponse.<T>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .detail(detail)
                .status(mapErrorCodeToHttpStatus(errorCode))
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .build();
    }

    /**
     * Create error response with custom message (legacy)
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .code("1003")
                .message(message)
                .status(500)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response with code and message (legacy)
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(String.valueOf(code))
                .message(message)
                .status(code)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response with code, message, and data (legacy)
     */
    public static <T> ApiResponse<T> error(Integer code, String message, T data) {
        return ApiResponse.<T>builder()
                .code(String.valueOf(code))
                .message(message)
                .data(data)
                .status(code)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create bad request error (400)
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.<T>builder()
                .code("1001")
                .message(message)
                .status(400)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create unauthorized error (401)
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.<T>builder()
                .code("2001")
                .message(message)
                .status(401)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create forbidden error (403)
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return ApiResponse.<T>builder()
                .code("3001")
                .message(message)
                .status(403)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create not found error (404)
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
                .code("1002")
                .message(message)
                .status(404)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create system/internal server error (500)
     */
    public static <T> ApiResponse<T> systemError(String message) {
        return ApiResponse.<T>builder()
                .code("1003")
                .message(message)
                .status(500)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Map ErrorCode to appropriate HTTP status code
     *
     * Error Code Ranges:
     * 1000-1999: General (400, 404, 503, 408, etc.)
     * 2000-2999: Authentication (401)
     * 3000-3999: Authorization (403)
     * 4000-4999: User (400, 404, 409)
     * 5000-5999: Business Logic (400, 409)
     * 6000-6999: File Upload (400, 413, 507)
     * 7000-7999: Database (500)
     * 8000-8999: External Services (502, 503)
     * 9000-9999: Rate Limiting (429)
     */
    private static Integer mapErrorCodeToHttpStatus(ErrorCode errorCode) {
        String code = errorCode.getCode();
        int codeInt = Integer.parseInt(code);

        if (codeInt == 1000) return 200;  // SUCCESS
        if (codeInt >= 1001 && codeInt <= 1005) return 400;  // INVALID_REQUEST, INTERNAL_ERROR, etc.
        if (codeInt == 1002) return 404;  // RESOURCE_NOT_FOUND
        if (codeInt == 1004) return 503;  // SERVICE_UNAVAILABLE
        if (codeInt == 1005) return 408;  // REQUEST_TIMEOUT
        if (codeInt >= 1006 && codeInt <= 1009) return 400;  // DUPLICATE, INVALID_OP, VALIDATION_FAILED, UNSUPPORTED

        if (codeInt >= 2001 && codeInt <= 2999) return 401;  // AUTHENTICATION errors

        if (codeInt >= 3001 && codeInt <= 3999) return 403;  // AUTHORIZATION errors

        if (codeInt >= 4001 && codeInt <= 4999) {  // USER errors
            if (codeInt == 4001 || codeInt == 4003 || codeInt == 4004) return 404;  // NOT_FOUND
            if (codeInt == 4002 || codeInt == 4005) return 409;  // EXISTS (Conflict)
            return 400;  // Other user errors
        }

        if (codeInt >= 5001 && codeInt <= 5999) {  // BUSINESS errors
            if (codeInt == 5002 || codeInt == 5007) return 409;  // EXISTS (Conflict)
            return 400;  // Other business errors
        }

        if (codeInt >= 6001 && codeInt <= 6999) {  // FILE UPLOAD errors
            if (codeInt == 6002) return 413;  // PAYLOAD_TOO_LARGE
            if (codeInt == 6005) return 507;  // INSUFFICIENT_STORAGE
            return 400;  // Other file errors
        }

        if (codeInt >= 7001 && codeInt <= 7999) return 500;  // DATABASE errors

        if (codeInt >= 8001 && codeInt <= 8999) return 502;  // EXTERNAL SERVICE errors (Bad Gateway)

        if (codeInt >= 9001 && codeInt <= 9999) return 429;  // RATE_LIMITING errors

        return 500;  // Default to Internal Server Error
    }
}
