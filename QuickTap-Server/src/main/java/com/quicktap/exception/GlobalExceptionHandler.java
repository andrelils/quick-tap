package com.quicktap.exception;

import com.quicktap.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理应用中的各类异常
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * 根据异常中的错误码返回相应的 HTTP 状态码
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException ex, WebRequest request) {
        log.warn("业务异常: code={}, message={}", ex.getCode(), ex.getMessage());

        Integer code = ex.getCode();
        ApiResponse<?> response = ApiResponse.error(code, ex.getMessage());

        // 根据业务错误码映射到相应的 HTTP 状态码
        HttpStatus httpStatus;
        if (code == 401) {
            httpStatus = HttpStatus.UNAUTHORIZED;      // 未授权/认证失败
        } else if (code == 403) {
            httpStatus = HttpStatus.FORBIDDEN;         // 禁止访问
        } else if (code == 404) {
            httpStatus = HttpStatus.NOT_FOUND;         // 资源不存在
        } else if (code == 400) {
            httpStatus = HttpStatus.BAD_REQUEST;       // 请求错误
        } else if (code == 500) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;  // 服务器错误
        } else {
            httpStatus = HttpStatus.OK;                // 其他使用 200
        }

        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException ex, WebRequest request) {
        BindingResult bindingResult = ex.getBindingResult();
        String message = bindingResult.getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));

        log.warn("参数验证失败: {}", message);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.badRequest(message));
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException ex, WebRequest request) {
        String message = String.format("参数 '%s' 类型错误，期望类型: %s",
            ex.getName(), ex.getRequiredType().getSimpleName());
        log.warn(message);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.badRequest(message));
    }

    /**
     * 处理认证异常（用户名/密码错误、token 过期等）
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(
        Exception ex, WebRequest request) {
        log.warn("认证异常: {}", ex.getMessage());
        String message = ex.getMessage();
        if (ex instanceof BadCredentialsException) {
            message = "用户名或密码错误";
        } else if (ex instanceof DisabledException) {
            message = "该账号已被禁用";
        } else if (ex instanceof InternalAuthenticationServiceException) {
            message = "用户不存在或密码错误";
        }
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.unauthorized(message));
    }

    /**
     * 处理权限异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(
        AccessDeniedException ex, WebRequest request) {
        log.warn("权限异常: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.forbidden("您没有权限访问此资源"));
    }

    /**
     * 处理 404 错误
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse<?>> handleNoHandlerFoundException(
        NoHandlerFoundException ex, WebRequest request) {
        log.warn("资源不存在: {}", ex.getRequestURL());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.notFound("请求的资源不存在"));
    }

    /**
     * 处理通用异常（捕获所有未处理的异常）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<?>> handleException(Exception ex, WebRequest request) {
        log.error("未预期的异常", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.systemError("服务器内部错误，请稍后重试"));
    }
}
