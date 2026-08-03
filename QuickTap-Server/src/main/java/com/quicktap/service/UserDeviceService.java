package com.quicktap.service;

import com.quicktap.dto.UserDeviceDTO;
import java.util.List;

/**
 * 用户设备关系Service
 */
public interface UserDeviceService {

    /**
     * 绑定用户到设备
     */
    UserDeviceDTO bindUserToDevice(Long userId, Long deviceId);

    /**
     * 获取用户关联的所有设备
     */
    List<UserDeviceDTO> getUserDevices(Long userId);

    /**
     * 获取设备关联的所有用户
     */
    List<UserDeviceDTO> getDeviceUsers(Long deviceId);

    /**
     * 解绑用户与设备关系
     */
    void unbindUserFromDevice(Long userId, Long deviceId);

    /**
     * 检查用户是否关联设备
     */
    boolean isUserBoundToDevice(Long userId, Long deviceId);

    /**
     * 删除用户的所有设备绑定
     */
    void removeUserAllDevices(Long userId);

    /**
     * 删除设备的所有用户绑定
     */
    void removeDeviceAllUsers(Long deviceId);
}
