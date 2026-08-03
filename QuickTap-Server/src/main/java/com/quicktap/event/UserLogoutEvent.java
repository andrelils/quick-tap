package com.quicktap.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户登出事件
 * 当用户登出时发送此事件
 */
@Getter
@Setter
public class UserLogoutEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    private String traceId;
    private Integer userId;
    private String token;
    private String username;

    public UserLogoutEvent(Object source, String traceId, Integer userId, String token, String username) {
        super(source);
        this.traceId = traceId;
        this.userId = userId;
        this.token = token;
        this.username = username;
    }
}
