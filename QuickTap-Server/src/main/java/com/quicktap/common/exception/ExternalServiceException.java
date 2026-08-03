package com.quicktap.common.exception;

/**
 * 外部服务异常
 *
 * 当调用外部服务（如第三方支付、短信服务、AI服务等）失败时抛出此异常
 * 最终将被全局异常处理器转换为 502 Bad Gateway HTTP状态码
 *
 * 使用场景：
 * - AI生成服务调用失败
 * - 支付网关请求失败
 * - 短信发送服务失败
 * - 邮件服务失败
 * - 第三方API超时
 *
 * 使用示例：
 * {@code
 * try {
 *     String response = aiService.generateText(prompt);
 * } catch (Exception e) {
 *     throw new ExternalServiceException("AI service unavailable", e);
 * }
 * }
 */
public class ExternalServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 异常码（用于API响应）
     */
    private Integer code;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 默认异常码为 502
     */
    private static final Integer DEFAULT_CODE = 502;

    /**
     * 使用默认异常码和消息构造异常
     *
     * @param message 异常消息
     */
    public ExternalServiceException(String message) {
        super(message);
        this.code = DEFAULT_CODE;
    }

    /**
     * 使用自定义异常码和消息构造异常
     *
     * @param code 异常码
     * @param message 异常消息
     */
    public ExternalServiceException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用消息和原因异常构造异常
     *
     * @param message 异常消息
     * @param cause 原因异常
     */
    public ExternalServiceException(String message, Throwable cause) {
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
    public ExternalServiceException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 使用服务名称、消息和原因异常构造异常
     *
     * @param serviceName 外部服务名称
     * @param message 异常消息
     * @param cause 原因异常
     */
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.code = DEFAULT_CODE;
    }

    /**
     * 获取异常码
     *
     * @return 异常码（通常为 502）
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

    /**
     * 获取服务名称
     *
     * @return 外部服务的名称
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * 设置服务名称
     *
     * @param serviceName 外部服务的名称
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
