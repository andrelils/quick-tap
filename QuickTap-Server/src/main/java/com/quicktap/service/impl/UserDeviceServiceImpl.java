package com.quicktap.service.impl;

import com.quicktap.entity.UserDevice;
import com.quicktap.dto.UserDeviceDTO;
import com.quicktap.repository.UserDeviceRepository;
import com.quicktap.service.UserDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户设备关系Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserDeviceServiceImpl implements UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;

    @Override
    public UserDeviceDTO bindUserToDevice(Long userId, Long deviceId) {
        // 检查是否已绑定
        if (userDeviceRepository.existsByUserIdAndDeviceId(userId, deviceId)) {
            log.warn("User {} already bound to device {}", userId, deviceId);
            throw new RuntimeException("用户已绑定该设备");
        }

        UserDevice userDevice = UserDevice.builder()
                .userId(userId)
                .deviceId(deviceId)
                .build();

        UserDevice saved = userDeviceRepository.save(userDevice);
        log.info("User {} bound to device {}", userId, deviceId);
        return convertToDTO(saved);
    }

    @Override
    public List<UserDeviceDTO> getUserDevices(Long userId) {
        List<UserDevice> devices = userDeviceRepository.findByUserId(userId);
        return devices.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<UserDeviceDTO> getDeviceUsers(Long deviceId) {
        List<UserDevice> users = userDeviceRepository.findByDeviceId(deviceId);
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public void unbindUserFromDevice(Long userId, Long deviceId) {
        var userDevice = userDeviceRepository.findByUserIdAndDeviceId(userId, deviceId);
        if (userDevice.isPresent()) {
            userDeviceRepository.delete(userDevice.get());
            log.info("User {} unbound from device {}", userId, deviceId);
        } else {
            log.warn("User {} not bound to device {}", userId, deviceId);
        }
    }

    @Override
    public boolean isUserBoundToDevice(Long userId, Long deviceId) {
        return userDeviceRepository.existsByUserIdAndDeviceId(userId, deviceId);
    }

    @Override
    public void removeUserAllDevices(Long userId) {
        List<UserDevice> devices = userDeviceRepository.findByUserId(userId);
        if (!devices.isEmpty()) {
            userDeviceRepository.deleteAll(devices);
            log.info("Removed all devices for user {}", userId);
        }
    }

    @Override
    public void removeDeviceAllUsers(Long deviceId) {
        List<UserDevice> users = userDeviceRepository.findByDeviceId(deviceId);
        if (!users.isEmpty()) {
            userDeviceRepository.deleteAll(users);
            log.info("Removed all users for device {}", deviceId);
        }
    }

    private UserDeviceDTO convertToDTO(UserDevice userDevice) {
        return UserDeviceDTO.builder()
                .id(userDevice.getId())
                .userId(userDevice.getUserId())
                .deviceId(userDevice.getDeviceId())
                .build();
    }
}
