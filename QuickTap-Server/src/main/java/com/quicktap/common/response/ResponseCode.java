package com.quicktap.common.response;

/**
 * API响应枚举 - 标准响应码
 *
 * 定义了系统中所有可能的API响应码
 * 便于统一管理响应码和对应的消息
 *
 * 响应码分类：
 * - 2xx: 成功响应
 * - 4xx: 客户端错误
 * - 5xx: 服务器错误
 *
 * 使用示例：
 * {@code
 * // 使用枚举返回响应
 * return ApiResponse.success(ResponseCode.SUCCESS, "操作成功", data);
 *
 * // 检查响应码
 * if (response.getCode() == ResponseCode.UNAUTHORIZED.code) {
 *     // 处理未授权
 * }
 * }
 */
public enum ResponseCode {

    /**
     * 200 - 成功
     * 通用成功响应码，用于大多数成功场景
     */
    SUCCESS(200, "Success"),

    /**
     * 201 - 已创建
     * 表示资源创建成功
     */
    CREATED(201, "Created"),

    /**
     * 204 - 无内容
     * 表示请求成功但无响应体（通常用于DELETE）
     */
    NO_CONTENT(204, "No Content"),

    /**
     * 400 - 请求错误
     * 表示请求参数不正确或验证失败
     */
    BAD_REQUEST(400, "Bad Request"),

    /**
     * 400 - 参数验证失败
     * 更详细的参数错误
     */
    VALIDATION_ERROR(400, "Validation Error"),

    /**
     * 400 - 资源重复
     * 表示创建的资源已存在
     */
    DUPLICATE_RESOURCE(400, "Resource Already Exists"),

    /**
     * 401 - 未授权
     * 表示用户未进行身份验证或Token无效
     */
    UNAUTHORIZED(401, "Unauthorized"),

    /**
     * 401 - Token过期
     * 表示JWT Token已过期
     */
    TOKEN_EXPIRED(401, "Token Expired"),

    /**
     * 401 - Token无效
     * 表示Token格式错误或签名失败
     */
    TOKEN_INVALID(401, "Invalid Token"),

    /**
     * 403 - 禁止访问
     * 表示用户权限不足，无法执行该操作
     */
    FORBIDDEN(403, "Forbidden"),

    /**
     * 404 - 资源不存在
     * 表示请求的资源未找到
     */
    NOT_FOUND(404, "Not Found"),

    /**
     * 409 - 冲突
     * 表示请求冲突（如状态转换冲突）
     */
    CONFLICT(409, "Conflict"),

    /**
     * 500 - 服务器错误
     * 通用服务器错误
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),

    /**
     * 502 - 外部服务错误
     * 表示调用外部服务（如支付、AI）失败
     */
    EXTERNAL_SERVICE_ERROR(502, "External Service Error"),

    /**
     * 503 - 服务不可用
     * 表示服务暂时不可用（如维护中）
     */
    SERVICE_UNAVAILABLE(503, "Service Unavailable");

    /**
     * 响应码（HTTP状态码）
     */
    public final int code;

    /**
     * 默认消息
     */
    public final String message;

    /**
     * 构造枚举
     *
     * @param code HTTP状态码
     * @param message 默认消息
     */
    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据HTTP状态码查找对应的响应码枚举
     *
     * @param code HTTP状态码
     * @return 对应的ResponseCode枚举，如果未找到返回INTERNAL_SERVER_ERROR
     */
    public static ResponseCode findByCode(int code) {
        for (ResponseCode rc : ResponseCode.values()) {
            if (rc.code == code) {
                return rc;
            }
        }
        return INTERNAL_SERVER_ERROR;
    }
}
