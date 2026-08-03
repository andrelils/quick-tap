package com.quicktap.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import java.util.List;

/**
 * 用户权限变更事件
 * 当用户权限/角色发生变更时发送此事件
 *
 * 使用场景：
 * 1. 管理员修改用户角色
 * 2. 权限规则更新
 * 3. 用户账号被禁用/启用
 */
@Getter
@Setter
public class UserPermissionChangedEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    private String traceId;
    private Integer userId;
    private List<String> oldRoles;
    private List<String> newRoles;
    private String changeReason;     // 变更原因
    private Integer changedBy;           // 谁进行的变更

    public UserPermissionChangedEvent(Object source, String traceId, Integer userId,
                                      List<String> oldRoles, List<String> newRoles) {
        super(source);
        this.traceId = traceId;
        this.userId = userId;
        this.oldRoles = oldRoles;
        this.newRoles = newRoles;
    }
}
