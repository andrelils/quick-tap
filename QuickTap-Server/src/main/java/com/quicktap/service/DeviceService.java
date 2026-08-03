package com.quicktap.service;

import com.quicktap.entity.Device;
import com.quicktap.mapper.DeviceMapper;
import com.quicktap.dto.PageResponse;
import com.quicktap.dto.DeviceDTO;
import com.quicktap.exception.BusinessException;
import com.quicktap.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备管理业务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceMapper deviceMapper;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 获取设备列表（分页）
     */
    public PageResponse<Device> getDeviceList(Integer pageNum, Integer pageSize) {
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.max(Math.min(pageSize, 100), 1);
        int offset = (pageNum - 1) * pageSize;

        List<Device> list = deviceMapper.selectPage(offset, pageSize);
        long total = deviceMapper.countAll();

        return PageResponse.of(list, pageNum, pageSize, total);
    }

    /**
     * 获取设备详情
     */
    public Device getDeviceById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("设备ID不能为空");
        }
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new IllegalArgumentException("设备不存在");
        }
        return device;
    }

    /**
     * 获取商户设备列表（分页）
     */
    public PageResponse<Device> getMerchantDeviceList(Integer merchantId, Integer pageNum, Integer pageSize) {
        if (merchantId == null || merchantId <= 0) {
            throw new IllegalArgumentException("商户ID不能为空");
        }
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.max(Math.min(pageSize, 100), 1);
        int offset = (pageNum - 1) * pageSize;

        List<Device> list = deviceMapper.selectByMerchantIdAndPage(merchantId, offset, pageSize);
        long total = deviceMapper.countByMerchantId(merchantId);

        return PageResponse.of(list, pageNum, pageSize, total);
    }

    /**
     * 创建设备
     */
    public Device createDevice(Device device) {
        if (device == null) {
            throw new IllegalArgumentException("设备信息不能为空");
        }
        if (device.getDeviceNo() == null || device.getDeviceNo().trim().isEmpty()) {
            throw new IllegalArgumentException("设备编号不能为空");
        }
        if (device.getName() == null || device.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("设备名称不能为空");
        }
        if (device.getType() == null || device.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("设备类型不能为空");
        }

        // 检查设备编号是否已存在
        Device existing = deviceMapper.selectByDeviceNo(device.getDeviceNo());
        if (existing != null) {
            throw new IllegalArgumentException("设备编号已存在");
        }

        if (device.getStatus() == null) {
            device.setStatus(1);
        }

        deviceMapper.insert(device);
        log.info("创建设备成功, deviceNo: {}, merchantId: {}", device.getDeviceNo(), device.getMerchantId());
        return device;
    }

    /**
     * 更新设备
     */
    public Device updateDevice(Integer id, Device device) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("设备ID不能为空");
        }
        Device existing = deviceMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("设备不存在");
        }

        if (device.getDeviceNo() != null && !device.getDeviceNo().isEmpty()) {
            existing.setDeviceNo(device.getDeviceNo());
        }
        if (device.getName() != null && !device.getName().isEmpty()) {
            existing.setName(device.getName());
        }
        if (device.getType() != null && !device.getType().isEmpty()) {
            existing.setType(device.getType());
        }
        if (device.getQrcode() != null) {
            existing.setQrcode(device.getQrcode());
        }
        if (device.getStatus() != null) {
            existing.setStatus(device.getStatus());
        }

        deviceMapper.update(existing);
        log.info("更新设备成功, id: {}", id);
        return existing;
    }

    /**
     * 删除设备
     */
    public void deleteDevice(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("设备ID不能为空");
        }
        Device existing = deviceMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("设备不存在");
        }
        deviceMapper.deleteById(id);
        log.info("删除设备成功, id: {}", id);
    }

    /**
     * 禁用设备
     */
    public Device disableDevice(Integer id) {
        Device device = getDeviceById(id);
        device.setStatus(0);
        deviceMapper.update(device);
        log.info("禁用设备成功, id: {}", id);
        return device;
    }

    /**
     * 启用设备
     */
    public Device enableDevice(Integer id) {
        Device device = getDeviceById(id);
        device.setStatus(1);
        deviceMapper.update(device);
        log.info("启用设备成功, id: {}", id);
        return device;
    }

    /**
     * 批量创建设备
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "merchant_devices", allEntries = true)
    public List<DeviceDTO> batchCreateDevices(Long merchantId, DeviceDTO.BatchCreateDeviceRequest request) {
        log.info("批量创建设备 | merchantId: {} | count: {}", merchantId, request.getDevices().size());

        List<Device> devices = request.getDevices().stream()
                .map(req -> {
                    Device device = deviceMapper.selectByDeviceNo(req.getDeviceNo());
                    if (device != null) {
                        throw new BusinessException("设备编号已存在: " + req.getDeviceNo());
                    }
                    return Device.builder()
                            .deviceNo(req.getDeviceNo())
                            .name(req.getName())
                            .merchantId(merchantId.intValue())
                            .type(req.getType())
                            .location(req.getLocation())
                            .macAddress(req.getMacAddress())
                            .ipAddress(req.getIpAddress())
                            .status(1)
                            .build();
                })
                .collect(Collectors.toList());

        deviceMapper.insertBatch(devices);
        log.info("设备批量创建成功 | count: {}", devices.size());
        return devices.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取商户启用设备
     */
    @Cacheable(value = "merchant_devices_enabled", key = "#merchantId")
    public List<DeviceDTO> getMerchantEnabledDevices(Long merchantId) {
        log.info("获取商户启用设备 | merchantId: {}", merchantId);
        List<Device> devices = deviceMapper.selectByMerchantIdAndStatus(merchantId.intValue(), 1);
        return devices.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 绑定二维码
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "merchant_devices", allEntries = true)
    public DeviceDTO bindQrCode(Long deviceId, Long merchantId, Long qrCodeId) {
        log.info("设备绑定二维码 | deviceId: {} | qrCodeId: {}", deviceId, qrCodeId);

        Device device = deviceMapper.selectById(deviceId.intValue());
        if (device == null) {
            throw new BusinessException("设备不存在");
        }

        if (!device.getMerchantId().equals(merchantId.intValue())) {
            throw new BusinessException("无权操作该设备");
        }

        // 检查二维码是否被其他设备绑定
        Device boundDevice = deviceMapper.selectByBindQrCodeId(qrCodeId);
        if (boundDevice != null && !boundDevice.getId().equals(device.getId())) {
            throw new BusinessException("该二维码已被其他设备绑定");
        }

        device.setBindQrCodeId(qrCodeId);
        deviceMapper.update(device);
        log.info("设备二维码绑定成功 | deviceId: {}", deviceId);
        return convertToDTO(device);
    }

    /**
     * 解绑二维码
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "merchant_devices", allEntries = true)
    public DeviceDTO unbindQrCode(Long deviceId, Long merchantId) {
        log.info("设备解绑二维码 | deviceId: {}", deviceId);

        Device device = deviceMapper.selectById(deviceId.intValue());
        if (device == null) {
            throw new BusinessException("设备不存在");
        }

        if (!device.getMerchantId().equals(merchantId.intValue())) {
            throw new BusinessException("无权操作该设备");
        }

        device.setBindQrCodeId(null);
        deviceMapper.update(device);
        log.info("设备二维码解绑成功 | deviceId: {}", deviceId);
        return convertToDTO(device);
    }

    /**
     * 转换为DTO
     */
    private DeviceDTO convertToDTO(Device device) {
        return DeviceDTO.builder()
                .id(device.getId().longValue())
                .deviceNo(device.getDeviceNo())
                .name(device.getName())
                .merchantId(device.getMerchantId())
                .type(device.getType())
                .location(device.getLocation())
                .macAddress(device.getMacAddress())
                .ipAddress(device.getIpAddress())
                .status(device.getStatus())
                .bindQrCodeId(device.getBindQrCodeId())
                .build();
    }
}
