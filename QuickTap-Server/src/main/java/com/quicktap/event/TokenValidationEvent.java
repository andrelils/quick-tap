package com.quicktap.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * Token验证事件
 * 当需要验证Token时发送此事件
 */
@Getter
@Setter
public class TokenValidationEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    private String traceId;
    private Integer userId;
    private String token;
    private String action;      // LOGIN, VALIDATE, REFRESH等
    private boolean valid;      // 验证结果

    public TokenValidationEvent(Object source, String traceId, Integer userId, String token, String action) {
        super(source);
        this.traceId = traceId;
        this.userId = userId;
        this.token = token;
        this.action = action;
        this.valid = false;
    }
}
