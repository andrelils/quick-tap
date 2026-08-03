package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.service.AdminService;
import com.quicktap.service.MerchantService;
import com.quicktap.entity.Admin;
import com.quicktap.entity.Merchant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统设置控制器
 * <p>
 * 路径前缀：/api/admin/system
 * 管理 system_setting 表中的域名、URL 等全局配置（key-value 结构）。
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/system")
@CrossOrigin(origins = "*")
public class SystemController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AdminService adminService;

    @Autowired
    private MerchantService merchantService;

    /**
     * 获取系统设置
     * 读取 system_setting 表全部 key-value，以对象形式返回。
     */
    @GetMapping("/settings")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Map<String, Object>> getSettings() {
        log.info("获取系统设置");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT key_name, value FROM system_setting");
        Map<String, Object> settings = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String key = (String) row.get("key_name");
            Object value = row.get("value");
            settings.put(key, value);
        }
        return ApiResponse.success("获取成功", settings);
    }

    /**
     * 更新系统设置
     * 接收 { domain: { ... } } 或 { key: value, ... }，逐个 upsert。
     */
    @PutMapping("/settings")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> updateSettings(@RequestBody Map<String, Object> body) {
        log.info("更新系统设置: {}", body.keySet());

        // 前端可能包裹在 domain 字段里
        Map<String, Object> settings;
        Object domainObj = body.get("domain");
        if (domainObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> casted = (Map<String, Object>) domainObj;
            settings = casted;
        } else {
            settings = body;
        }

        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
            upsertSetting(key, value);
        }
        return ApiResponse.success("保存成功", null);
    }

    /**
     * upsert 单条系统设置：存在则更新，不存在则插入。
     */
    private void upsertSetting(String key, String value) {
        List<Map<String, Object>> exist = jdbcTemplate.queryForList(
                "SELECT id FROM system_setting WHERE key_name = ? LIMIT 1", key);
        if (exist.isEmpty()) {
            jdbcTemplate.update(
                    "INSERT INTO system_setting (key_name, value, created_at, updated_at) VALUES (?, ?, NOW(), NOW())",
                    key, value);
        } else {
            jdbcTemplate.update(
                    "UPDATE system_setting SET value = ?, updated_at = NOW() WHERE key_name = ?",
                    value, key);
        }
    }

    // ============================================================================
    // 管理员 <-> 商户 访问权限绑定
    // 策略：
    //   SUPER_ADMIN -> 可访问所有商户（返回全部merchantIds）
    //   MERCHANT    -> 仅可访问自己绑定的商户
    //   ADMIN       -> 可访问通过本接口配置的商户列表（用 admin.merchantId 作为单个绑定，
    //                  同时本接口额外支持"多商户"列表，以 JSON 字符串形式存在 system_setting 中，
    //                  key = "admin_merchant_access_{adminId}"，value = "[1,2,3]"）
    // ============================================================================

    /**
     * 获取所有管理员的商户访问绑定列表
     */
    @GetMapping("/admin-merchant-access/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<List<Map<String, Object>>> getAdminMerchantAccessList() {
        log.info("获取所有管理员的商户访问权限列表");
        List<Admin> allAdmins = adminService.getAllAdmins();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Admin admin : allAdmins) {
            Map<String, Object> row = new HashMap<>();
            row.put("adminId", admin.getId());
            row.put("username", admin.getUsername());
            row.put("nickname", admin.getNickname());
            row.put("role", admin.getRole());
            row.put("merchantIds", resolveMerchantIds(admin));
            result.add(row);
        }
        return ApiResponse.success(result);
    }

    /**
     * 获取单个管理员可访问的商户ID列表
     */
    @GetMapping("/admin-merchant-access/{adminId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Map<String, Object>> getAdminMerchantAccess(@PathVariable Integer adminId) {
        log.info("获取管理员 {} 的商户访问权限", adminId);
        Admin admin = adminService.getAdminById(adminId);
        Map<String, Object> result = new HashMap<>();
        result.put("adminId", adminId);
        result.put("username", admin.getUsername());
        result.put("role", admin.getRole());
        result.put("merchantIds", resolveMerchantIds(admin));
        return ApiResponse.success(result);
    }

    /**
     * 更新管理员可访问的商户列表
     * 入参：{ "merchantIds": [1,2,3] }
     */
    @PostMapping("/admin-merchant-access/{adminId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> updateAdminMerchantAccess(
            @PathVariable Integer adminId,
            @RequestBody Map<String, Object> body) {
        Object raw = body.get("merchantIds");
        List<Integer> merchantIds = new ArrayList<>();
        if (raw instanceof List) {
            for (Object o : (List<?>) raw) {
                if (o != null) {
                    try {
                        merchantIds.add(Integer.valueOf(o.toString()));
                    } catch (Exception ignore) {
                    }
                }
            }
        }
        log.info("更新管理员 {} 的商户访问权限: merchantIds={}", adminId, merchantIds);

        // 如果只选了一个，直接写到 admin.merchantId；其它写入 system_setting 做扩展存储
        Admin admin = adminService.getAdminById(adminId);
        Integer firstMerchant = merchantIds.isEmpty() ? null : merchantIds.get(0);
        try {
            com.quicktap.dto.AdminUpdateRequest upd = new com.quicktap.dto.AdminUpdateRequest();
            upd.setMerchantId(firstMerchant);
            adminService.updateAdmin(adminId, upd);
        } catch (Exception e) {
            log.warn("更新 admin.merchantId 失败（但继续存储多商户配置）: {}", e.getMessage());
        }
        // 存储多商户列表到 system_setting（即使只有一个也写，保证一致）
        String json = merchantIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]"));
        upsertSetting("admin_merchant_access_" + adminId, json);
        return ApiResponse.success("保存成功", null);
    }

    /**
     * 根据管理员角色/配置解析可访问的商户ID集合
     */
    private List<Integer> resolveMerchantIds(Admin admin) {
        String role = admin.getRole() == null ? "" : admin.getRole().toUpperCase();
        // SUPER_ADMIN 可访问所有商户
        if ("SUPER_ADMIN".equals(role)) {
            try {
                List<Merchant> all = merchantService.getMerchantList(1, 1000);
                return all.stream().map(Merchant::getId).collect(Collectors.toList());
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }
        // MERCHANT 仅自己绑定的那个
        if ("MERCHANT".equals(role)) {
            List<Integer> list = new ArrayList<>();
            if (admin.getMerchantId() != null) list.add(admin.getMerchantId());
            return list;
        }
        // ADMIN 角色：从 system_setting 读多商户配置，fallback 到 admin.merchantId
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT value FROM system_setting WHERE key_name = ? LIMIT 1",
                    "admin_merchant_access_" + admin.getId());
            if (!rows.isEmpty()) {
                String value = String.valueOf(rows.get(0).get("value"));
                // 解析 "[1,2,3]" 形式
                String inside = value.replaceAll("[\\[\\]\\s]", "");
                if (!inside.isEmpty()) {
                    List<Integer> list = new ArrayList<>();
                    for (String s : inside.split(",")) {
                        if (!s.isEmpty()) {
                            try {
                                list.add(Integer.parseInt(s));
                            } catch (Exception ignore) {
                            }
                        }
                    }
                    if (!list.isEmpty()) return list;
                }
            }
        } catch (Exception ignore) {
        }
        List<Integer> fallback = new ArrayList<>();
        if (admin.getMerchantId() != null) fallback.add(admin.getMerchantId());
        return fallback;
    }
}
