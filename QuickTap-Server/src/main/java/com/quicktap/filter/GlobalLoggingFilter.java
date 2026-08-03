package com.quicktap.filter;

import com.quicktap.utils.SensitiveDataSanitizer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 全局日志过滤器
 * 为每个请求生成唯一的 traceId，用于链路追踪
 * 记录请求和响应信息到日志，并脱敏敏感数据
 *
 * 安全特性:
 * - 自动生成请求追踪ID
 * - 敏感数据脱敏（密码、Token等）
 * - 记录请求/响应体（仅当包含敏感数据时）
 * - 记录请求头（脱敏授权信息）
 * - 性能监控（请求耗时）
 * - 安全日志记录
 */
@Slf4j
@Component
public class GlobalLoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_MDC = "traceId";
    private static final String REQUEST_START_TIME = "requestStartTime";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    // 不记录请求体的路径（静态资源、文件下载等）
    private static final String[] EXCLUDE_PATHS = {
        "/api/*/download",
        "/api/*/export",
        "/static/",
        "/images/",
        "/videos/",
        "/files/"
    };

    // 最大请求/响应体大小（防止日志文件过大）
    private static final int MAX_BODY_SIZE = 10000; // 10KB

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        // 生成或获取 traceId
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }

        // 将 traceId 放入 MDC，自动关联到所有日志输出
        MDC.put(TRACE_ID_MDC, traceId);
        MDC.put(REQUEST_START_TIME, String.valueOf(System.currentTimeMillis()));

        // 添加到响应头
        response.setHeader(TRACE_ID_HEADER, traceId);

        // 包装请求和响应以捕获内容
        ContentCachingRequestWrapper cachedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper(response);

        try {
            // 记录请求信息
            logRequest(cachedRequest);

            // 执行过滤链
            filterChain.doFilter(cachedRequest, cachedResponse);

            // 记录响应信息
            logResponse(cachedResponse);

            // 将缓存的响应内容写回真实响应
            cachedResponse.copyBodyToResponse();

        } catch (Exception e) {
            log.error("请求异常 | 方法: {} | URI: {} | 错误: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    e.getMessage(), e);
            throw e;
        } finally {
            // 清理 MDC
            MDC.clear();
        }
    }

    /**
     * 记录请求信息
     */
    private void logRequest(ContentCachingRequestWrapper request) {
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            String clientIP = getClientIP(request);
            String userAgent = request.getHeader("User-Agent");

            // 基本请求信息（脱敏查询参数）
            String sanitizedQuery = queryString != null ?
                SensitiveDataSanitizer.sanitize(queryString) : "";

            log.info("请求开始 | 方法: {} | URI: {} | 查询: {} | IP: {} | User-Agent: {}",
                    method,
                    uri,
                    sanitizedQuery.isEmpty() ? "无" : sanitizedQuery,
                    clientIP,
                    userAgent);

            // 脱敏并记录认证头
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && !authHeader.isEmpty()) {
                String sanitizedAuth = SensitiveDataSanitizer.sanitizeAuthHeader(authHeader);
                log.debug("认证信息 | Authorization: {}", sanitizedAuth);
            }

            // 记录请求体（如果包含敏感数据）
            if (isRequestBodyLoggable(request)) {
                String requestBody = getRequestBody(request);
                if (requestBody != null && !requestBody.isEmpty()) {
                    if (SensitiveDataSanitizer.containsSensitiveData(requestBody)) {
                        String sanitizedBody = SensitiveDataSanitizer.sanitizeRequestBody(requestBody);
                        log.debug("请求体 | 内容类型: {} | 体: {}",
                                request.getContentType(),
                                sanitizedBody);
                    } else {
                        log.debug("请求体 | 内容类型: {} | 体: {}",
                                request.getContentType(),
                                requestBody);
                    }
                }
            }

        } catch (Exception e) {
            log.warn("记录请求信息异常: {}", e.getMessage());
        }
    }

    /**
     * 记录响应信息
     */
    private void logResponse(ContentCachingResponseWrapper response) {
        try {
            long duration = System.currentTimeMillis() - Long.parseLong(MDC.get(REQUEST_START_TIME));
            int status = response.getStatus();

            // 基本响应信息
            log.info("请求结束 | 状态: {} | 耗时: {}ms",
                    status,
                    duration);

            // 记录响应体（如果包含敏感数据）
            String responseBody = getResponseBody(response);
            if (responseBody != null && !responseBody.isEmpty()) {
                if (SensitiveDataSanitizer.containsSensitiveData(responseBody)) {
                    String sanitizedBody = SensitiveDataSanitizer.sanitize(responseBody);
                    log.debug("响应体 | 内容类型: {} | 体: {}",
                            response.getContentType(),
                            sanitizedBody);
                }
                // 如果不包含敏感数据，不记录响应体（避免日志过大）
            }

            // 记录错误响应的详情
            if (status >= 400) {
                log.warn("响应异常 | 状态码: {} | 响应体: {}",
                        status,
                        responseBody != null && responseBody.length() > 0 ?
                            responseBody.substring(0, Math.min(200, responseBody.length())) : "无");
            }

        } catch (Exception e) {
            log.warn("记录响应信息异常: {}", e.getMessage());
        }
    }

    /**
     * 获取请求体内容
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        try {
            byte[] content = request.getContentAsByteArray();
            if (content.length == 0) {
                return null;
            }

            // 限制日志大小
            if (content.length > MAX_BODY_SIZE) {
                return new String(content, 0, MAX_BODY_SIZE, StandardCharsets.UTF_8) +
                       "... [截断，总大小: " + content.length + " 字节]";
            }

            return new String(content, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("获取请求体异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取响应体内容
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        try {
            byte[] content = response.getContentAsByteArray();
            if (content.length == 0) {
                return null;
            }

            // 限制日志大小
            if (content.length > MAX_BODY_SIZE) {
                return new String(content, 0, MAX_BODY_SIZE, StandardCharsets.UTF_8) +
                       "... [截断，总大小: " + content.length + " 字节]";
            }

            return new String(content, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("获取响应体异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断是否应该记录请求体
     * 排除文件下载、静态资源等
     */
    private boolean isRequestBodyLoggable(HttpServletRequest request) {
        String uri = request.getRequestURI();

        for (String excludePath : EXCLUDE_PATHS) {
            if (uri.matches(excludePath.replace("*", ".*"))) {
                return false;
            }
        }

        // 仅记录 POST、PUT、PATCH 请求的请求体
        String method = request.getMethod();
        return "POST".equalsIgnoreCase(method) ||
               "PUT".equalsIgnoreCase(method) ||
               "PATCH".equalsIgnoreCase(method);
    }

    /**
     * 获取客户端 IP 地址
     */
    private String getClientIP(HttpServletRequest request) {
        // 检查代理头
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For 可能包含多个 IP，取第一个
            return ip.split(",")[0].trim();
        }

        // 检查 X-Real-IP 头（Nginx）
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        // 检查 Cloudflare 头
        ip = request.getHeader("CF-Connecting-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        // 使用远程地址作为最后的选择
        return request.getRemoteAddr();
    }
}
