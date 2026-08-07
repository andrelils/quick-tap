package com.quicktap.service;

import com.quicktap.entity.SystemSetting;
import com.quicktap.mapper.SystemSettingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统设置服务
 * <p>
 * 管理 system_setting 表中的域名、URL 等全局配置（key-value 结构）。
 * 抽离自 SystemController，避免 Controller 直接使用 JdbcTemplate。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSettingService {

    private final SystemSettingMapper systemSettingMapper;

    /**
     * 获取全部系统设置，以 key -> value 映射返回（保持数据库顺序）。
     */
    public Map<String, Object> getAllSettings() {
        List<SystemSetting> list = systemSettingMapper.selectAll();
        Map<String, Object> settings = new LinkedHashMap<>();
        for (SystemSetting s : list) {
            settings.put(s.getKeyName(), s.getValue());
        }
        return settings;
    }

    /**
     * 批量更新（或新增）系统设置。
     * @param settings key -> value 映射
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSettings(Map<String, Object> settings) {
        if (settings == null || settings.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
            upsertSetting(key, value);
        }
    }

    /**
     * upsert 单条配置：存在则更新，不存在则新增。
     */
    public void upsertSetting(String key, String value) {
        SystemSetting existing = systemSettingMapper.selectByKey(key);
        if (existing == null) {
            systemSettingMapper.insert(key, value);
        } else {
            systemSettingMapper.updateByKey(key, value);
        }
    }

    /**
     * 按 key 读取配置值，不存在返回 null。
     */
    public String getValue(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        SystemSetting s = systemSettingMapper.selectByKey(key);
        return s != null ? s.getValue() : null;
    }
}
