package com.quicktap.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;

/**
 * Token过期事件
 * 当Token验证失败或过期时发送此事件
 */
@Getter
@Setter
public class TokenExpiredEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    private String traceId;
    private Integer userId;
    private String token;
    private LocalDateTime expiredAt;
    private String reason;      // 过期原因

    public TokenExpiredEvent(Object source, String traceId, Integer userId, String token, LocalDateTime expiredAt) {
        super(source);
        this.traceId = traceId;
        this.userId = userId;
        this.token = token;
        this.expiredAt = expiredAt;
    }
}
