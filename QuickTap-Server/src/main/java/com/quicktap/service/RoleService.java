package com.quicktap.service;

import com.quicktap.common.ErrorCode;
import com.quicktap.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义角色服务 - 角色管理(新增/编辑/删除)落库
 * <p>
 * 数据表: role (id, name=角色标识, description=角色名称, permissions=权限JSON)
 * 与「角色管理」页面契约一致：name 为英文标识，description 为中文名称，permissions 为模块权限数组。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final JdbcTemplate jdbcTemplate;

    /** 系统内置角色标识 */
    public static final List<String> BUILT_IN_ROLES = List.of("super_admin", "admin", "merchant");

    /** 内置角色默认权限（优先从 permissions 表取全量，超管/管理员/商户给出合理默认） */
    public List<String> getDefaultPermissionCodes(String role) {
        List<String> all = listDbPermissionCodes();
        if (all.isEmpty()) {
            return new ArrayList<>();
        }
        switch (role == null ? "" : role) {
            case "super_admin":
                return new ArrayList<>(all);
            case "admin":
                // 除商家权限配置外的全部权限
                List<String> adminPerms = new ArrayList<>();
                for (String code : all) {
                    if (!"system.access".equals(code)) {
                        adminPerms.add(code);
                    }
                }
                return adminPerms;
            case "merchant":
                List<String> merchantPerms = new ArrayList<>();
                for (String code : all) {
                    if (code.startsWith("dashboard.") || code.startsWith("merchant.")
                            || code.startsWith("device.") || "ai.generate".equals(code)
                            || "ai.corpus".equals(code) || code.startsWith("marketing.")
                            || "system.settings".equals(code)) {
                        merchantPerms.add(code);
                    }
                }
                return merchantPerms;
            default:
                return new ArrayList<>();
        }
    }

    /** 从 permissions 表读取全部启用权限 code */
    public List<String> listDbPermissionCodes() {
        try {
            return jdbcTemplate.queryForList(
                    "SELECT code FROM permissions WHERE status = 1 ORDER BY id", String.class);
        } catch (Exception e) {
            log.warn("读取 permissions 表失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /** 从 permissions 表读取全部权限（含 resource/action/description，供前端勾选） */
    public List<Map<String, Object>> listDbPermissions() {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT code, resource, action, description FROM permissions WHERE status = 1 ORDER BY resource, action");
            List<Map<String, Object>> list = new ArrayList<>();
            for (Map<String, Object> r : rows) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("code", r.get("code"));
                item.put("resource", r.get("resource"));
                item.put("action", r.get("action"));
                item.put("description", r.get("description"));
                list.add(item);
            }
            return list;
        } catch (Exception e) {
            log.warn("读取 permissions 表失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /** 获取角色生效的权限 code 列表：role 表有配置则用之，否则用内置默认 */
    public List<String> getEffectivePermissions(String name) {
        Map<String, Object> record = getByName(name);
        if (record != null && record.get("permissions") != null) {
            List<String> configured = parsePermissionJson(toStr(record.get("permissions")));
            if (!configured.isEmpty()) {
                return configured;
            }
        }
        return getDefaultPermissionCodes(name);
    }

    private List<String> parsePermissionJson(String json) {
        if (json == null || json.isBlank() || "[]".equals(json.trim())) {
            return new ArrayList<>();
        }
        try {
            com.fasterxml.jackson.core.type.TypeReference<List<String>> typeRef =
                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {};
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.warn("解析权限JSON失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper =
            new com.fasterxml.jackson.databind.ObjectMapper();

    private String toStr(Object o) {
        return o == null ? null : String.valueOf(o).trim();
    }

    public boolean isBuiltIn(String name) {
        return name != null && BUILT_IN_ROLES.contains(name);
    }

    /** 自定义角色列表（不含内置角色） */
    public List<Map<String, Object>> listCustom() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, name, description, permissions, created_at FROM role " +
                        "ORDER BY created_at ASC");
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.get("id"));
            item.put("name", r.get("name"));
            item.put("description", r.get("description"));
            item.put("permissions", r.get("permissions"));
            item.put("createdAt", r.get("created_at"));
            list.add(item);
        }
        return list;
    }

    public boolean exists(String name) {
        if (name == null) return false;
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM role WHERE name = ?", Integer.class, name);
        return count != null && count > 0;
    }

    public Map<String, Object> getByName(String name) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, name, description, permissions, created_at FROM role WHERE name = ? LIMIT 1", name);
        if (rows.isEmpty()) {
            return null;
        }
        Map<String, Object> r = rows.get(0);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", r.get("id"));
        item.put("name", r.get("name"));
        item.put("description", r.get("description"));
        item.put("permissions", r.get("permissions"));
        item.put("createdAt", r.get("created_at"));
        return item;
    }

    /**
     * 创建自定义角色
     * @param name 角色标识(英文)
     * @param description 角色名称(中文)
     * @param permissionsJson 权限JSON数组字符串
     */
    public Map<String, Object> create(String name, String description, String permissionsJson) {
        if (!StringUtils.hasText(name) || !StringUtils.hasText(description)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "角色标识和角色名称不能为空");
        }
        String code = name.trim().toLowerCase();
        if (isBuiltIn(code)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "不能创建与系统内置角色相同的角色");
        }
        if (!code.matches("^[a-z][a-z0-9_]{1,31}$")) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "角色标识需以小写字母开头，仅含小写字母/数字/下划线，长度2-32");
        }
        if (exists(code)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "角色标识已存在");
        }
        String perms = (permissionsJson == null || permissionsJson.isBlank()) ? "[]" : permissionsJson;
        jdbcTemplate.update(
                "INSERT INTO role (name, description, permissions, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())",
                code, description, perms);
        log.info("创建自定义角色成功: name={}", code);
        return getByName(code);
    }

    /**
     * 更新角色（内置角色也允许更新权限与名称，配置落库 role 表）
     * @param name 角色标识
     */
    public Map<String, Object> update(String name, String description, String permissionsJson) {
        Map<String, Object> existing = getByName(name);
        if (existing == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "角色不存在");
        }
        String perms = (permissionsJson == null || permissionsJson.isBlank()) ? "[]" : permissionsJson;
        jdbcTemplate.update(
                "UPDATE role SET description = ?, permissions = ?, updated_at = NOW() WHERE name = ?",
                description, perms, name);
        log.info("更新角色成功: name={}", name);
        return getByName(name);
    }

    /**
     * 保存内置角色配置（不存在则插入，存在则更新），用于内置角色权限页面配置
     */
    public Map<String, Object> upsertBuiltIn(String name, String description, String permissionsJson) {
        if (!isBuiltIn(name)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "仅支持系统内置角色");
        }
        String perms = (permissionsJson == null || permissionsJson.isBlank()) ? "[]" : permissionsJson;
        if (exists(name)) {
            jdbcTemplate.update(
                    "UPDATE role SET description = ?, permissions = ?, updated_at = NOW() WHERE name = ?",
                    description, perms, name);
        } else {
            jdbcTemplate.update(
                    "INSERT INTO role (name, description, permissions, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())",
                    name, description, perms);
        }
        log.info("保存内置角色配置成功: name={}", name);
        return getByName(name);
    }

    /**
     * 删除自定义角色
     */
    public void delete(String name) {
        Map<String, Object> existing = getByName(name);
        if (existing == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "角色不存在");
        }
        if (isBuiltIn(name)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "系统内置角色不允许删除");
        }
        jdbcTemplate.update("DELETE FROM role WHERE name = ?", name);
        log.info("删除自定义角色成功: name={}", name);
    }
}
