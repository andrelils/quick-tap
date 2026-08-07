package com.quicktap.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库结构自动迁移
 * 启动时执行幂等 DDL（MySQL 8 支持 ADD COLUMN IF NOT EXISTS），
 * 保证新功能所需的列在任意环境部署时自动补齐，无需手动执行 SQL。
 */
@Slf4j
@Component
public class DatabaseSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        migrateMerchantQuotaColumns();
        migrateAiGenerateRecordColumns();
        migrateUserColumns();
        migrateAiConfigColumns();
    }

    private void migrateMerchantQuotaColumns() {
        // MySQL 8 不支持 ADD COLUMN IF NOT EXISTS，改为先查 INFORMATION_SCHEMA 再补列（幂等且兼容）
        String[] columns = {"text_quota_limit", "image_quota_limit", "video_quota_limit"};
        addColumnsIfMissing("merchant", columns);
    }

    private void migrateAiGenerateRecordColumns() {
        String[] columns = {"record_id", "token_usage", "cost", "updated_at"};
        addColumnsIfMissing("ai_generate_record", columns);
    }

    private void migrateUserColumns() {
        String[] columns = {"username", "password"};
        addColumnsIfMissing("user", columns);
    }

    private void migrateAiConfigColumns() {
        String[] columns = {"api_secret"};
        addColumnsIfMissing("ai_config", columns);
    }

    private void addColumnsIfMissing(String table, String[] columns) {
        for (String col : columns) {
            try {
                Integer exists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                    Integer.class, table, col);
                if (exists != null && exists > 0) {
                    log.info("数据库列已存在，跳过迁移: {}.{}", table, col);
                    continue;
                }
                String ddl = "ALTER TABLE " + table + " ADD COLUMN " + col + " " + columnType(table, col);
                jdbcTemplate.execute(ddl);
                log.info("数据库迁移成功: {}", ddl);
            } catch (Exception e) {
                log.warn("数据库迁移失败: {}.{} - {}", table, col, e.getMessage());
            }
        }
    }

    private String columnType(String table, String col) {
        if (table.equals("merchant")) {
            return "BIGINT NULL COMMENT '额度覆盖(NULL使用套餐默认)'";
        }
        switch (col) {
            case "record_id": return "VARCHAR(64) NULL COMMENT '记录ID(UUID)'";
            case "token_usage": return "INT NULL COMMENT 'Token用量'";
            case "cost": return "DECIMAL(10,6) NULL COMMENT '生成成本'";
            case "updated_at": return "DATETIME NULL COMMENT '更新时间'";
            case "username": return "VARCHAR(50) NULL COMMENT '用户名(电话注册)'";
            case "password": return "VARCHAR(255) NULL COMMENT '密码(BCrypt)'";
            case "api_secret": return "VARCHAR(255) NULL COMMENT 'API密钥'";
            default: return "VARCHAR(255) NULL";
        }
    }
}
