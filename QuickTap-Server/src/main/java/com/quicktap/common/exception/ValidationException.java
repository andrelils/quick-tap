package com.quicktap.common.exception;

/**
 * 参数验证异常
 *
 * 当请求的参数不符合验证规则时抛出此异常
 * 最终将被全局异常处理器转换为 400 Bad Request HTTP状态码
 *
 * 使用场景：
 * - 参数为空或null
 * - 参数格式不正确（如邮箱、手机号）
 * - 参数值超出允许范围
 * - 参数类型不匹配
 *
 * 使用示例：
 * {@code
 * if (StringUtil.isEmpty(request.getUsername())) {
 *     throw new ValidationException("Username cannot be empty");
 * }
 *
 * if (!EmailValidator.isValid(request.getEmail())) {
 *     throw new ValidationException("Invalid email format");
 * }
 * }
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 异常码（用于API响应）
     */
    private Integer code;

    /**
     * 默认异常码为 400
     */
    private static final Integer DEFAULT_CODE = 400;

    /**
     * 使用默认异常码和消息构造异常
     *
     * @param message 异常消息，通常说明哪个参数验证失败及原因
     */
    public ValidationException(String message) {
        super(message);
        this.code = DEFAULT_CODE;
    }

    /**
     * 使用自定义异常码和消息构造异常
     *
     * @param code 异常码
     * @param message 异常消息
     */
    public ValidationException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用消息和原因异常构造异常
     *
     * @param message 异常消息
     * @param cause 原因异常
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.code = DEFAULT_CODE;
    }

    /**
     * 使用自定义异常码、消息和原因异常构造异常
     *
     * @param code 异常码
     * @param message 异常消息
     * @param cause 原因异常
     */
    public ValidationException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 获取异常码
     *
     * @return 异常码（通常为 400）
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 设置异常码
     *
     * @param code 异常码
     */
    public void setCode(Integer code) {
        this.code = code;
    }
}
