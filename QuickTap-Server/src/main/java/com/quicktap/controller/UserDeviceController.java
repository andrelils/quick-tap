package com.quicktap.controller;

import com.quicktap.dto.UserDeviceDTO;
import com.quicktap.dto.ApiResponse;
import com.quicktap.service.UserDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 用户设备关系Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/user-device")
@RequiredArgsConstructor
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    /**
     * 绑定用户到设备
     */
    @PostMapping("/bind/{userId}/{deviceId}")
    public ApiResponse<UserDeviceDTO> bindUserToDevice(
            @PathVariable Long userId,
            @PathVariable Long deviceId) {
        try {
            UserDeviceDTO result = userDeviceService.bindUserToDevice(userId, deviceId);
            return ApiResponse.success("绑定成功", result);
        } catch (Exception e) {
            log.error("Error binding user to device", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户的所有设备
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<UserDeviceDTO>> getUserDevices(@PathVariable Long userId) {
        try {
            List<UserDeviceDTO> result = userDeviceService.getUserDevices(userId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Error getting user devices", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取设备的所有关联用户
     */
    @GetMapping("/device/{deviceId}")
    public ApiResponse<List<UserDeviceDTO>> getDeviceUsers(@PathVariable Long deviceId) {
        try {
            List<UserDeviceDTO> result = userDeviceService.getDeviceUsers(deviceId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Error getting device users", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 检查用户是否关联设备
     */
    @GetMapping("/check/{userId}/{deviceId}")
    public ApiResponse<Boolean> checkUserDeviceBinding(
            @PathVariable Long userId,
            @PathVariable Long deviceId) {
        try {
            boolean bound = userDeviceService.isUserBoundToDevice(userId, deviceId);
            return ApiResponse.success(bound);
        } catch (Exception e) {
            log.error("Error checking user device binding", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 解绑用户与设备
     */
    @DeleteMapping("/unbind/{userId}/{deviceId}")
    public ApiResponse<Void> unbindUserFromDevice(
            @PathVariable Long userId,
            @PathVariable Long deviceId) {
        try {
            userDeviceService.unbindUserFromDevice(userId, deviceId);
            return ApiResponse.success("解绑成功", null);
        } catch (Exception e) {
            log.error("Error unbinding user from device", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除用户的所有设备
     */
    @DeleteMapping("/user/{userId}/all")
    public ApiResponse<Void> removeUserAllDevices(@PathVariable Long userId) {
        try {
            userDeviceService.removeUserAllDevices(userId);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            log.error("Error removing user all devices", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
