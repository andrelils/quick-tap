package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.dto.PageResponse;
import com.quicktap.dto.DeviceCreateRequest;
import com.quicktap.dto.DeviceUpdateRequest;
import com.quicktap.entity.Device;
import com.quicktap.service.DeviceService;
import com.quicktap.service.MerchantService;
import com.quicktap.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备管理控制器
 * IMPORTANT: Static routes must be defined BEFORE dynamic routes to prevent
 * dynamic routes from matching static paths. Route matching order in Spring:
 * 1. GET /api/device/list (static)
 * 2. GET /api/device/merchant/{merchantId} (dynamic with specific pattern)
 * 3. POST /api/device/batch (static)
 * 4. PUT /api/device/batch/enable (static)
 * 5. PUT /api/device/batch/disable (static)
 * 6. DELETE /api/device/batch (static)
 * 7. GET /api/device/{id} (dynamic - must be LAST to avoid matching static routes)
 * 8. PUT /api/device/{id}/disable (dynamic)
 * 9. PUT /api/device/{id}/enable (dynamic)
 * 10. DELETE /api/device/{id} (dynamic)
 * 11. PUT /api/device/{id} (dynamic)
 */
@Slf4j
@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
@Validated
public class DeviceController {
    private final DeviceService deviceService;
    private final MerchantService merchantService;
    private final SecurityUtil securityUtil;

    // ============================================================================
    // STATIC ROUTES - All static routes must come before dynamic routes
    // ============================================================================

    /**
     * 获取设备列表
     * STATIC ROUTE: Must appear before /{id}
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    public ApiResponse<PageResponse<Map<String, Object>>> listDevices(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<Device> data;
        if (securityUtil.isMerchant()) {
            Long merchantId = securityUtil.getCurrentMerchantId();
            data = merchantId != null
                    ? deviceService.getMerchantDeviceList(merchantId.intValue(), pageNum, pageSize)
                    : PageResponse.of(List.of(), pageNum, pageSize, 0L);
        } else {
            data = deviceService.getDeviceList(pageNum, pageSize);
        }
        // 将扁平设备列表按名称+systemCode 分组为设备套
        List<Map<String, Object>> sets = groupDevicesToSets(data.getList());
        long total = securityUtil.isMerchant() ? data.getTotal() : deviceService.countDeviceSets();
        PageResponse<Map<String, Object>> result = PageResponse.of(sets, pageNum, pageSize, total);
        return ApiResponse.success("获取成功", result);
    }

    /**
     * 将扁平设备列表按 name + systemCode 分组，一套设备 = QR + NFC
     */
    private List<Map<String, Object>> groupDevicesToSets(List<Device> devices) {
        if (devices == null || devices.isEmpty()) return java.util.Collections.emptyList();

        // 收集所有非空 merchantId
        List<Integer> merchantIds = devices.stream()
                .map(Device::getMerchantId)
                .filter(mid -> mid != null)
                .distinct()
                .collect(Collectors.toList());

        // 通过 Service 批量查询商家名称（避免 Controller 直接使用 JdbcTemplate）
        Map<Integer, String> merchantNameMap = merchantService.getMerchantNameMap(merchantIds);

        Map<String, Map<String, Object>> setMap = new java.util.LinkedHashMap<>();
        for (Device d : devices) {
            String key = (d.getName() != null ? d.getName() : "") + "|" + (d.getSystemCode() != null ? d.getSystemCode() : "");
            if (!setMap.containsKey(key)) {
                Map<String, Object> set = new java.util.LinkedHashMap<>();
                set.put("setId", key);
                set.put("setName", d.getName());
                set.put("systemCode", d.getSystemCode());
                set.put("merchantId", d.getMerchantId());
                set.put("merchantName", d.getMerchantId() != null ? merchantNameMap.getOrDefault(d.getMerchantId(), null) : null);
                set.put("bindStatus", d.getMerchantId() != null ? 1 : 0);
                set.put("createdAt", d.getCreatedAt());
                set.put("qrcode", null);
                set.put("nfc", null);
                setMap.put(key, set);
            }
            Map<String, Object> set = setMap.get(key);
            if ("qrcode".equals(d.getType())) {
                set.put("qrcode", d);
            } else if ("nfc".equals(d.getType())) {
                set.put("nfc", d);
            }
            if (d.getMerchantId() != null) {
                set.put("bindStatus", 1);
                set.put("merchantId", d.getMerchantId());
                set.put("merchantName", merchantNameMap.getOrDefault(d.getMerchantId(), null));
            }
        }
        return new java.util.ArrayList<>(setMap.values());
    }

    /**
     * 批量创建设备
     * 匹配 Node.js: POST /api/device/batch
     * STATIC ROUTE: Must appear before /{id}
     */
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    public ApiResponse<Map<String, Object>> batchCreateDevices(
            @RequestBody List<DeviceCreateRequest> requests) {
        log.info("批量创建设备: count={}", requests.size());

        if (requests == null || requests.isEmpty()) {
            return ApiResponse.badRequest("设备列表不能为空");
        }

        int successCount = 0;
        int failCount = 0;

        for (DeviceCreateRequest request : requests) {
            try {
                Device device = new Device();
                device.setDeviceNo(request.getDeviceNo());
                device.setName(request.getName());
                device.setMerchantId(request.getMerchantId());
                device.setType(request.getType());
                device.setSystemCode(request.getSystemCode());
                device.setUrl(request.getUrl());
                device.setQrcode(request.getQrcode());
                device.setStatus(1);

                deviceService.createDevice(device);
                successCount++;
            } catch (Exception e) {
                log.error("批量创建设备失败: deviceNo={}, error={}", request.getDeviceNo(), e.getMessage());
                failCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", requests.size());
        result.put("success", successCount);
        result.put("fail", failCount);

        return ApiResponse.success("批量创建完成", result);
    }

    /**
     * 批量启用设备
     * 匹配 Node.js: PUT /api/device/batch/enable
     * STATIC ROUTE: Must appear before /{id}/*
     */
    @PutMapping("/batch/enable")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Map<String, Object>> batchEnableDevices(@RequestBody List<Integer> deviceIds) {
        log.info("批量启用设备: count={}", deviceIds.size());

        if (deviceIds == null || deviceIds.isEmpty()) {
            return ApiResponse.badRequest("设备ID列表不能为空");
        }

        int successCount = 0;
        int failCount = 0;

        for (Integer deviceId : deviceIds) {
            try {
                deviceService.enableDevice(deviceId);
                successCount++;
            } catch (Exception e) {
                log.error("启用设备失败: deviceId={}, error={}", deviceId, e.getMessage());
                failCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", deviceIds.size());
        result.put("success", successCount);
        result.put("fail", failCount);

        return ApiResponse.success("批量启用完成", result);
    }

    /**
     * 批量禁用设备
     * 匹配 Node.js: PUT /api/device/batch/disable
     * STATIC ROUTE: Must appear before /{id}/*
     */
    @PutMapping("/batch/disable")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Map<String, Object>> batchDisableDevices(@RequestBody List<Integer> deviceIds) {
        log.info("批量禁用设备: count={}", deviceIds.size());

        if (deviceIds == null || deviceIds.isEmpty()) {
            return ApiResponse.badRequest("设备ID列表不能为空");
        }

        int successCount = 0;
        int failCount = 0;

        for (Integer deviceId : deviceIds) {
            try {
                deviceService.disableDevice(deviceId);
                successCount++;
            } catch (Exception e) {
                log.error("禁用设备失败: deviceId={}, error={}", deviceId, e.getMessage());
                failCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", deviceIds.size());
        result.put("success", successCount);
        result.put("fail", failCount);

        return ApiResponse.success("批量禁用完成", result);
    }

    /**
     * 批量删除设备
     * 匹配 Node.js: DELETE /api/device/batch
     * STATIC ROUTE: Must appear before /{id}
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Map<String, Object>> batchDeleteDevices(@RequestBody List<Integer> deviceIds) {
        log.info("批量删除设备: count={}", deviceIds.size());

        if (deviceIds == null || deviceIds.isEmpty()) {
            return ApiResponse.badRequest("设备ID列表不能为空");
        }

        int successCount = 0;
        int failCount = 0;

        for (Integer deviceId : deviceIds) {
            try {
                deviceService.deleteDevice(deviceId);
                successCount++;
            } catch (Exception e) {
                log.error("删除设备失败: deviceId={}, error={}", deviceId, e.getMessage());
                failCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", deviceIds.size());
        result.put("success", successCount);
        result.put("fail", failCount);

        return ApiResponse.success("批量删除完成", result);
    }

    // ============================================================================
    // DYNAMIC ROUTES - All dynamic routes must come AFTER static routes
    // ============================================================================

    /**
     * 获取设备详情
     * DYNAMIC ROUTE: Must appear AFTER all static routes like /list, /batch, etc.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    public ApiResponse<Device> getDevice(@PathVariable @NotNull Integer id) {
        Device device = deviceService.getDeviceById(id);
        return ApiResponse.success("获取成功", device);
    }

    /**
     * 获取商户设备列表
     * DYNAMIC ROUTE: Specific pattern /merchant/{merchantId}
     */
    @GetMapping("/merchant/{merchantId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    public ApiResponse<PageResponse<Device>> getMerchantDevices(
            @PathVariable @NotNull Integer merchantId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<Device> data = deviceService.getMerchantDeviceList(merchantId, pageNum, pageSize);
        return ApiResponse.success("获取成功", data);
    }

    /**
     * 创建设备
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    public ApiResponse<Device> createDevice(@Valid @RequestBody DeviceCreateRequest request) {
        Device device = new Device();
        device.setDeviceNo(request.getDeviceNo());
        device.setName(request.getName());
        device.setMerchantId(request.getMerchantId());
        device.setType(request.getType());
        device.setQrcode(request.getQrcode());
        device.setSystemCode(request.getSystemCode());
        device.setUrl(request.getUrl());
        device.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        Device created = deviceService.createDevice(device);
        return ApiResponse.success("创建成功", created);
    }

    /**
     * 更新设备
     * DYNAMIC ROUTE: Must appear AFTER all static routes
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    public ApiResponse<Device> updateDevice(
            @PathVariable @NotNull Integer id,
            @Valid @RequestBody DeviceUpdateRequest request) {
        Device device = new Device();
        device.setDeviceNo(request.getDeviceNo());
        device.setName(request.getName());
        device.setType(request.getType());
        device.setQrcode(request.getQrcode());
        device.setStatus(request.getStatus());

        Device updated = deviceService.updateDevice(id, device);
        return ApiResponse.success("更新成功", updated);
    }

    /**
     * 禁用设备
     * DYNAMIC ROUTE: Must appear AFTER all static routes
     */
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Device> disableDevice(@PathVariable @NotNull Integer id) {
        Device device = deviceService.disableDevice(id);
        return ApiResponse.success("禁用成功", device);
    }

    /**
     * 启用设备
     * DYNAMIC ROUTE: Must appear AFTER all static routes
     */
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Device> enableDevice(@PathVariable @NotNull Integer id) {
        Device device = deviceService.enableDevice(id);
        return ApiResponse.success("启用成功", device);
    }

    /**
     * 删除设备
     * DYNAMIC ROUTE: Must appear AFTER all static routes
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> deleteDevice(@PathVariable @NotNull Integer id) {
        deviceService.deleteDevice(id);
        return ApiResponse.success("删除成功");
    }
}
