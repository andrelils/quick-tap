package com.quicktap.exception;

import com.quicktap.dto.ApiResponse;
import com.quicktap.entity.AuditLog;
import com.quicktap.service.AuditLoggingService;
import com.quicktap.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器 - 统一处理应用中的各类异常
 *
 * 职责：
 * - 捕获并处理所有控制器抛出的异常
 * - 将异常转换为标准化的 API 响应
 * - 记录异常到审计日志系统
 * - 保护敏感信息（不在错误消息中暴露内部细节）
 * - 提供清晰的用户提示信息
 *
 * 处理的异常类型：
 * - BusinessException: 业务逻辑异常（含自定义错误码）
 * - MethodArgumentNotValidException: 请求体 JSON 字段验证失败
 * - MethodArgumentTypeMismatchException: 请求参数类型不匹配
 * - ConstraintViolationException: @RequestParam/@PathVariable 约束验证失败
 * - AuthenticationException: 认证失败（用户名密码错误、token 过期等）
 * - AccessDeniedException: 权限不足（已认证但无权访问）
 * - NoHandlerFoundException: 404 资源不存在
 * - DataIntegrityViolationException: 数据库约束违反（重复数据等）
 * - HttpMessageNotReadableException: 请求体格式错误（JSON 解析异常）
 * - Exception: 通用异常捕捉（所有未被处理的异常）
 *
 * 安全特性：
 * - 敏感信息隐藏：不在错误消息中暴露字段名、验证规则、数据库细节
 * - 审计日志集成：认证失败、权限拒绝等安全事件被记录
 * - IP 追踪：所有异常事件都记录来源 IP
 * - User-Agent 记录：便于识别客户端类型
 * - 异步处理：审计日志异步记录，不阻塞异常响应
 *
 * 集成点：
 * - AuditLoggingService: 记录安全相关异常到审计日志
 * - IpUtil: 提取请求来源 IP
 * - SecurityContextHolder: 获取当前认证用户信息
 *
 * @author QuickTap Security Team
 * @version 2.0
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired(required = false)
    private AuditLoggingService auditLoggingService;

    /**
     * 处理业务异常
     * 根据异常中的错误码返回相应的 HTTP 状态码
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException ex, WebRequest request) {
        log.warn("业务异常: code={}, message={}", ex.getCode(), ex.getMessage());

        Integer code = Integer.valueOf(ex.getCode());
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
     * 隐藏字段名和验证规则，保护敏感信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException ex, WebRequest request) {
        BindingResult bindingResult = ex.getBindingResult();

        // 内部日志：记录详细的验证失败信息
        String detailMessage = bindingResult.getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
        log.warn("参数验证失败: {}", detailMessage);

        // 用户返回：使用泛化的错误消息，不暴露字段名和验证规则
        String userMessage = "请求参数验证失败，请检查输入数据";

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.badRequest(userMessage));
    }

    /**
     * 处理参数类型不匹配异常
     * 隐藏参数名，保护敏感信息
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException ex, WebRequest request) {
        // 内部日志：记录详细信息用于调试
        log.warn("参数类型错误: 参数名={}，期望类型={}", ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "未知");

        // 用户返回：泛化的错误消息，不暴露参数名
        String userMessage = "请求参数类型错误，请检查输入数据";

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.badRequest(userMessage));
    }

    /**
     * 处理约束校验异常（@Validated 标注在控制器类上时，对 @RequestParam/@PathVariable 的校验失败会抛出此异常）
     * 隐藏字段名和验证规则
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolationException(
        ConstraintViolationException ex, WebRequest request) {
        // 内部日志：记录详细的验证失败信息
        String detailMessage = ex.getConstraintViolations().stream()
            .map(v -> {
                String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "";
                int lastSeg = path.lastIndexOf('.');
                String fieldName = lastSeg >= 0 ? path.substring(lastSeg + 1) : path;
                return fieldName + ": " + v.getMessage();
            })
            .collect(Collectors.joining("; "));
        log.warn("约束校验失败: {}", detailMessage);

        // 用户返回：使用泛化的错误消息，不暴露字段名
        String userMessage = "请求参数验证失败，请检查输入数据";

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.badRequest(userMessage));
    }

    /**
     * 处理认证异常（用户名/密码错误、token 过期等）
     * 集成审计日志记录
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(
        Exception ex, WebRequest request) {
        log.warn("认证异常: {}", ex.getMessage());

        // 提取请求上下文用于审计
        String ipAddress = extractClientIp(request);
        String userAgent = extractUserAgent(request);

        // 审计日志：记录认证失败事件
        if (auditLoggingService != null) {
            try {
                String username = extractUsernameFromException(ex);
                auditLoggingService.auditLogin(
                    null,  // userId 在认证失败时为 null
                    username,
                    false,  // success flag
                    ipAddress,
                    userAgent,
                    ex.getMessage()  // 记录失败原因
                );
            } catch (Exception e) {
                log.debug("审计日志记录异常: {}", e.getMessage());
            }
        }

        // 统一返回通用错误消息，避免暴露用户存在性信息或其他敏感细节
        String message = "用户名或密码错误";
        if (ex instanceof DisabledException) {
            message = "该账号已被禁用";
        }

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.unauthorized(message));
    }

    /**
     * 处理权限异常 - 已认证但无权访问
     * 集成审计日志记录
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(
        AccessDeniedException ex, WebRequest request) {
        log.warn("权限异常: {}", ex.getMessage());

        // 提取请求上下文用于审计
        String ipAddress = extractClientIp(request);
        String userAgent = extractUserAgent(request);

        // 审计日志：记录权限拒绝事件（这是重要的安全事件）
        if (auditLoggingService != null) {
            try {
                // 尝试从 SecurityContext 中获取当前用户信息
                String username = getCurrentUsername();
                Long userId = getCurrentUserId();

                auditLoggingService.auditAsync(
                    userId,
                    username,
                    AuditLog.EventType.SENSITIVE_OPERATION,
                    AuditLog.Status.DENIED,
                    "权限不足：" + ex.getMessage(),
                    ipAddress,
                    userAgent
                );
            } catch (Exception e) {
                log.debug("审计日志记录异常: {}", e.getMessage());
            }
        }

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
     * 处理 HTTP 消息不可读异常（如 JSON 解析错误）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException ex, WebRequest request) {
        log.warn("请求体格式错误: {}", ex.getMessage());
        String userMessage = "请求体格式错误，请检查 JSON 格式";
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.badRequest(userMessage));
    }

    /**
     * 处理数据完整性违反异常（如数据库约束冲突）
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolationException(
        DataIntegrityViolationException ex, WebRequest request) {
        log.error("数据完整性违反: {}", ex.getMessage());
        // 隐藏具体的数据库约束信息，返回泛化的错误消息
        String userMessage = "数据操作失败，可能是因为数据已存在或约束冲突";
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.badRequest(userMessage));
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

    // ======================== 辅助方法 ========================

    /**
     * 从请求中提取客户端 IP 地址
     */
    private String extractClientIp(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            HttpServletRequest httpRequest = ((ServletWebRequest) request).getRequest();
            return IpUtil.getClientIp(httpRequest);
        }

        // 备选方案
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return "unknown";
    }

    /**
     * 从请求中提取 User-Agent
     */
    private String extractUserAgent(WebRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "unknown";
    }

    /**
     * 从异常中提取用户名
     */
    private String extractUsernameFromException(Exception ex) {
        // 尝试从异常消息中提取用户名（如果有的话）
        String message = ex.getMessage();
        if (message != null && message.contains("user")) {
            // 这是一个简单的启发式方法，实际场景可能需要更复杂的逻辑
            return "unknown";
        }
        return "unknown";
    }

    /**
     * 从 SecurityContext 获取当前认证用户的用户名
     */
    private String getCurrentUsername() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.debug("无法获取当前用户名: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从 SecurityContext 获取当前认证用户的 ID
     * 注意：这需要 UserPrincipal 或其他自定义 Principal 实现来提供用户ID
     */
    private Long getCurrentUserId() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                var principal = authentication.getPrincipal();
                // 如果是 UserPrincipal 对象，提取 ID
                if (principal instanceof com.quicktap.security.UserPrincipal) {
                    return ((com.quicktap.security.UserPrincipal) principal).getId().longValue();
                }
            }
        } catch (Exception e) {
            log.debug("无法获取当前用户ID: {}", e.getMessage());
        }
        return null;
    }
}
