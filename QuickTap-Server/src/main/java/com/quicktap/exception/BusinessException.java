package com.quicktap.exception;

import com.quicktap.common.ErrorCode;
import lombok.Getter;

/**
 * Business Logic Exception
 * Thrown when business rules or validation fail
 *
 * Uses centralized ErrorCode enum for consistent error responses.
 * Each exception has an ErrorCode and optional detailed message.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail;

    /**
     * Create exception with error code only
     * Uses error code's default message
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    /**
     * Create exception with error code and detailed message
     * Detailed message provides additional context
     */
    public BusinessException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage() + (detail != null ? ": " + detail : ""));
        this.errorCode = errorCode;
        this.detail = detail;
    }

    /**
     * Create exception with error code and cause
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.detail = null;
    }

    /**
     * Create exception with error code, detailed message, and cause
     */
    public BusinessException(ErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode.getMessage() + (detail != null ? ": " + detail : ""), cause);
        this.errorCode = errorCode;
        this.detail = detail;
    }

    /**
     * Get error code as string (for backward compatibility)
     */
    public String getCode() {
        return errorCode.getCode();
    }
}
