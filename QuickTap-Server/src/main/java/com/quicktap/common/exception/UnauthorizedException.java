package com.quicktap.common.exception;

/**
 * 权限不足异常
 *
 * 当用户尝试执行没有权限的操作时抛出此异常
 * 最终将被全局异常处理器转换为 403 Forbidden HTTP状态码
 *
 * 使用场景：
 * - 访问受限资源（如其他用户的数据）
 * - 执行需要特定角色的操作（如删除管理员）
 * - 跨商户操作限制
 *
 * 使用示例：
 * {@code
 * if (!currentUser.isMerchantOwner(merchantId)) {
 *     throw new UnauthorizedException("You don't have permission to modify this merchant");
 * }
 * }
 */
public class UnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 异常码（用于API响应）
     */
    private Integer code;

    /**
     * 默认异常码为 403
     */
    private static final Integer DEFAULT_CODE = 403;

    /**
     * 使用默认异常码和消息构造异常
     *
     * @param message 异常消息
     */
    public UnauthorizedException(String message) {
        super(message);
        this.code = DEFAULT_CODE;
    }

    /**
     * 使用自定义异常码和消息构造异常
     *
     * @param code 异常码
     * @param message 异常消息
     */
    public UnauthorizedException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用消息和原因异常构造异常
     *
     * @param message 异常消息
     * @param cause 原因异常
     */
    public UnauthorizedException(String message, Throwable cause) {
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
    public UnauthorizedException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 获取异常码
     *
     * @return 异常码（通常为 403）
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
