package com.quicktap.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * Token刷新事件
 * 当需要刷新Token时发送此事件
 */
@Getter
@Setter
public class TokenRefreshEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    private String traceId;
    private Integer userId;
    private String oldToken;
    private String newToken;
    private boolean refreshFailed;

    public TokenRefreshEvent(Object source, String traceId, Integer userId, String oldToken) {
        super(source);
        this.traceId = traceId;
        this.userId = userId;
        this.oldToken = oldToken;
        this.refreshFailed = false;
    }
}
