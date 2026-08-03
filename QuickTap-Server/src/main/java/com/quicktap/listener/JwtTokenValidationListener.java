package com.quicktap.listener;

import com.quicktap.event.*;
import com.quicktap.security.JwtTokenProvider;
import com.quicktap.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * JWT Token验证监听器
 * 处理所有与JWT Token相关的事件
 * 包括：Token验证、刷新、过期、登出、权限变更
 *
 * 架构说明：
 * - TokenValidationEvent 保持同步执行，便于发布方读取验证结果
 * - 其余事件使用 @Async 异步执行，不阻塞 HTTP 请求线程
 * - 集成Token黑名单管理
 * - 支持分布式环境（通过Redis）
 * - 提供完整的JWT生命周期管理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenValidationListener {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Token验证事件处理（同步执行）
     * 保持同步是为了让 AuthService.validateToken 能读取 event.isValid() 作为最终验证结果。
     * 验证逻辑：先检查黑名单，再校验签名与过期，任一失败则置 valid=false。
     *
     * @param event TokenValidationEvent
     */
    @EventListener
    public void onTokenValidationEvent(TokenValidationEvent event) {
        log.info("[JWT-Listener] Token验证事件 - traceId: {}, userId: {}, action: {}",
                event.getTraceId(), event.getUserId(), event.getAction());

        try {
            // 首先检查token是否在黑名单中
            if (tokenBlacklistService.isBlacklisted(event.getToken())) {
                log.warn("[JWT-Listener] Token已在黑名单中 - traceId: {}", event.getTraceId());
                event.setValid(false);
                return;
            }

            // 检查用户是否已被撤销（权限变更后强制重登）
            if (event.getUserId() != null && tokenBlacklistService.isUserRevoked(event.getUserId())) {
                log.warn("[JWT-Listener] 用户已被撤销 - userId: {}", event.getUserId());
                event.setValid(false);
                return;
            }

            // 验证Token有效性
            if (jwtTokenProvider.validateToken(event.getToken())) {
                event.setValid(true);
                log.debug("[JWT-Listener] Token验证通过 - userId: {}", event.getUserId());
            } else {
                event.setValid(false);
                log.warn("[JWT-Listener] Token验证失败 - userId: {}", event.getUserId());
            }
        } catch (Exception e) {
            log.error("[JWT-Listener] Token验证异常 - userId: {}, error: {}", event.getUserId(), e.getMessage(), e);
            event.setValid(false);
        }
    }

    /**
     * Token刷新事件处理（异步执行）
     * 将旧 token 加入黑名单，防止旧 token 在过期前继续使用。
     *
     * @param event TokenRefreshEvent
     */
    @Async
    @EventListener
    public void onTokenRefreshEvent(TokenRefreshEvent event) {
        log.info("[JWT-Listener] Token刷新事件 - traceId: {}, userId: {}, failed: {}",
                event.getTraceId(), event.getUserId(), event.isRefreshFailed());

        try {
            if (event.isRefreshFailed()) {
                log.warn("[JWT-Listener] Token刷新失败 - traceId: {}", event.getTraceId());
                return;
            }

            // 将旧token加入黑名单，防止使用已过期的token
            if (event.getOldToken() != null) {
                tokenBlacklistService.addToBlacklist(event.getOldToken());
                log.info("[JWT-Listener] 旧token已加入黑名单 - userId: {}", event.getUserId());
            }

            log.info("[JWT-Listener] Token刷新成功 - userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("[JWT-Listener] Token刷新事件处理失败 - userId: {}, error: {}",
                    event.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * Token过期事件处理（异步执行）
     * 将过期 token 加入黑名单，防止重放。
     *
     * @param event TokenExpiredEvent
     */
    @Async
    @EventListener
    public void onTokenExpiredEvent(TokenExpiredEvent event) {
        log.info("[JWT-Listener] Token过期事件 - traceId: {}, userId: {}, reason: {}",
                event.getTraceId(), event.getUserId(), event.getReason());

        try {
            // 将过期token加入黑名单
            if (event.getToken() != null) {
                tokenBlacklistService.addToBlacklist(event.getToken());
                log.info("[JWT-Listener] 过期token已加入黑名单 - userId: {}", event.getUserId());
            }

            log.info("[JWT-Listener] Token过期处理完成 - userId: {}, expiredAt: {}",
                    event.getUserId(), event.getExpiredAt());
        } catch (Exception e) {
            log.error("[JWT-Listener] Token过期事件处理失败 - userId: {}, error: {}",
                    event.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * 用户登出事件处理（异步执行）
     * 将登出用户的 token 加入黑名单。
     *
     * @param event UserLogoutEvent
     */
    @Async
    @EventListener
    public void onUserLogoutEvent(UserLogoutEvent event) {
        log.info("[JWT-Listener] 用户登出事件 - traceId: {}, userId: {}, username: {}",
                event.getTraceId(), event.getUserId(), event.getUsername());

        try {
            // 将登出用户的token加入黑名单
            if (event.getToken() != null) {
                tokenBlacklistService.addToBlacklist(event.getToken());
                log.info("[JWT-Listener] 用户token已加入黑名单 - userId: {}", event.getUserId());
            }

            log.info("[JWT-Listener] 用户登出处理完成 - userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("[JWT-Listener] 用户登出事件处理失败 - userId: {}, error: {}",
                    event.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * 用户权限变更事件处理（异步执行）
     * 撤销该用户的所有 Token，强制其重新登录获取新权限。
     * 这是重要的安全操作，防止用户权限变更后仍使用旧token进行越权操作。
     *
     * @param event UserPermissionChangedEvent
     */
    @Async
    @EventListener
    public void onUserPermissionChangedEvent(UserPermissionChangedEvent event) {
        log.info("[JWT-Listener] 用户权限变更事件 - traceId: {}, userId: {}, changedBy: {}",
                event.getTraceId(), event.getUserId(), event.getChangedBy());

        try {
            // 撤销该用户的所有现有 Token，强制重新登录
            if (event.getUserId() != null) {
                tokenBlacklistService.revokeUser(event.getUserId());
                log.info("[JWT-Listener] 用户 {} 的所有Token已撤销，需重新登录", event.getUserId());
            }

            log.info("[JWT-Listener] 用户权限变更处理完成 - userId: {}, oldRoles: {}, newRoles: {}",
                    event.getUserId(), event.getOldRoles(), event.getNewRoles());
        } catch (Exception e) {
            log.error("[JWT-Listener] 用户权限变更事件处理失败 - userId: {}, error: {}",
                    event.getUserId(), e.getMessage(), e);
        }
    }
}

