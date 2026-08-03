package com.quicktap.common.exception;

/**
 * 资源不存在异常
 *
 * 当请求的资源（如用户、商户、订单等）不存在时抛出此异常
 * 最终将被全局异常处理器转换为 404 Not Found HTTP状态码
 *
 * 使用示例：
 * {@code
 * User user = userRepository.findById(userId);
 * if (user == null) {
 *     throw new ResourceNotFoundException("User not found with id: " + userId);
 * }
 * }
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 异常码（用于API响应）
     */
    private Integer code;

    /**
     * 默认异常码为 404
     */
    private static final Integer DEFAULT_CODE = 404;

    /**
     * 使用默认异常码和消息构造异常
     *
     * @param message 异常消息
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.code = DEFAULT_CODE;
    }

    /**
     * 使用自定义异常码和消息构造异常
     *
     * @param code 异常码
     * @param message 异常消息
     */
    public ResourceNotFoundException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用消息和原因异常构造异常
     *
     * @param message 异常消息
     * @param cause 原因异常
     */
    public ResourceNotFoundException(String message, Throwable cause) {
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
    public ResourceNotFoundException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 获取异常码
     *
     * @return 异常码（通常为 404）
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
