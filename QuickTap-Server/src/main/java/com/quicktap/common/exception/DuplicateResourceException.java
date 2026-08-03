package com.quicktap.common.exception;

/**
 * 资源重复异常
 *
 * 当创建的资源已存在时抛出此异常
 * 最终将被全局异常处理器转换为 400 Bad Request HTTP状态码
 *
 * 使用场景：
 * - 用户名已存在
 * - 邮箱已被注册
 * - 设备编号已存在
 * - 优惠券代码重复
 *
 * 使用示例：
 * {@code
 * Admin existingAdmin = adminRepository.findByUsername(request.getUsername());
 * if (existingAdmin != null) {
 *     throw new DuplicateResourceException("Username already exists");
 * }
 * }
 */
public class DuplicateResourceException extends RuntimeException {

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
     * @param message 异常消息，说明哪个资源重复
     */
    public DuplicateResourceException(String message) {
        super(message);
        this.code = DEFAULT_CODE;
    }

    /**
     * 使用自定义异常码和消息构造异常
     *
     * @param code 异常码
     * @param message 异常消息
     */
    public DuplicateResourceException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用消息和原因异常构造异常
     *
     * @param message 异常消息
     * @param cause 原因异常
     */
    public DuplicateResourceException(String message, Throwable cause) {
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
    public DuplicateResourceException(Integer code, String message, Throwable cause) {
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
