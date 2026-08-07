package com.quicktap.common;

import lombok.Getter;

/**
 * API Error Code Enum
 *
 * Centralized definition of all API error codes
 * Ensures consistent error responses across the application
 */
@Getter
public enum ErrorCode {

    // General Errors (1000-1999)
    SUCCESS("1000", "Operation successful"),
    INVALID_REQUEST("1001", "Invalid request parameters"),
    RESOURCE_NOT_FOUND("1002", "Resource not found"),
    INTERNAL_SERVER_ERROR("1003", "Internal server error"),
    SERVICE_UNAVAILABLE("1004", "Service temporarily unavailable"),
    REQUEST_TIMEOUT("1005", "Request timeout"),
    DUPLICATE_RESOURCE("1006", "Resource already exists"),
    INVALID_OPERATION("1007", "Invalid operation"),
    DATA_VALIDATION_FAILED("1008", "Data validation failed"),
    UNSUPPORTED_OPERATION("1009", "Operation not supported"),

    // Authentication Errors (2000-2999)
    UNAUTHORIZED("2001", "User not authenticated"),
    TOKEN_INVALID("2002", "Token invalid or expired"),
    TOKEN_EXPIRED("2003", "Token has expired"),
    INVALID_CREDENTIALS("2004", "Invalid username or password"),
    ACCOUNT_DISABLED("2005", "Account is disabled"),
    ACCOUNT_LOCKED("2006", "Account is locked"),
    PASSWORD_INCORRECT("2007", "Incorrect password"),
    PASSWORD_EXPIRED("2008", "Password has expired"),

    // Authorization Errors (3000-3999)
    FORBIDDEN("3001", "Access denied"),
    PERMISSION_DENIED("3002", "You don't have permission to perform this action"),
    ROLE_REQUIRED("3003", "Required role not found"),
    INSUFFICIENT_PRIVILEGES("3004", "Insufficient privileges"),

    // User Errors (4000-4999)
    USER_NOT_FOUND("4001", "User not found"),
    USER_EXISTS("4002", "User already exists"),
    EMAIL_EXISTS("4003", "Email already registered"),
    PHONE_EXISTS("4004", "Phone number already registered"),
    USERNAME_EXISTS("4005", "Username already taken"),
    USER_INVALID_DATA("4006", "Invalid user data"),
    PASSWORD_TOO_SHORT("4007", "Password too short"),
    PASSWORD_TOO_SIMPLE("4008", "Password not complex enough"),
    OLD_PASSWORD_WRONG("4009", "Old password incorrect"),

    // Business Logic Errors (5000-5999)
    MERCHANT_NOT_FOUND("5001", "Merchant not found"),
    MERCHANT_EXISTS("5002", "Merchant already exists"),
    MERCHANT_DISABLED("5003", "Merchant is disabled"),
    MERCHANT_INVALID_DATA("5004", "Invalid merchant data"),
    MERCHANT_QUOTA_EXCEEDED("5005", "Merchant quota exceeded"),
    DEVICE_NOT_FOUND("5006", "Device not found"),
    DEVICE_ALREADY_REGISTERED("5007", "Device already registered"),
    INVALID_QUOTA("5008", "Invalid quota amount"),
    INSUFFICIENT_QUOTA("5009", "Insufficient quota"),

    // File Upload Errors (6000-6999)
    FILE_UPLOAD_FAILED("6001", "File upload failed"),
    FILE_TOO_LARGE("6002", "File size exceeds maximum limit"),
    INVALID_FILE_TYPE("6003", "File type not allowed"),
    FILE_NOT_FOUND("6004", "File not found"),
    STORAGE_LIMIT_EXCEEDED("6005", "Storage quota exceeded"),

    // Database Errors (7000-7999)
    DATABASE_ERROR("7001", "Database operation failed"),
    DATABASE_CONSTRAINT_VIOLATION("7002", "Database constraint violated"),
    TRANSACTION_FAILED("7003", "Transaction failed"),

    // External Service Errors (8000-8999)
    EXTERNAL_SERVICE_ERROR("8001", "External service error"),
    PAYMENT_SERVICE_ERROR("8002", "Payment service error"),
    SMS_SERVICE_ERROR("8003", "SMS service error"),
    EMAIL_SERVICE_ERROR("8004", "Email service error"),
    THIRD_PARTY_API_ERROR("8005", "Third-party API error"),

    // Rate Limiting & Quota (9000-9999)
    RATE_LIMIT_EXCEEDED("9001", "Request rate limit exceeded"),
    QUOTA_EXCEEDED("9002", "Quota exceeded"),
    TOO_MANY_REQUESTS("9003", "Too many requests");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
