-- MySQL 5.7+ 兼容版本 - 分开执行每个ADD COLUMN（避免IF NOT EXISTS语法）
-- 如果列已存在会报错，但可以继续执行后续语句

-- 添加 username 列
ALTER TABLE `audit_log` ADD COLUMN `username` VARCHAR(128) COMMENT '操作用户名' AFTER `user_id`;

-- 添加 event_type 列（如果原来没有，operation列需要保留）
ALTER TABLE `audit_log` ADD COLUMN `event_type` VARCHAR(64) NOT NULL DEFAULT 'SENSITIVE_OPERATION' COMMENT '事件类型' AFTER `operation`;

-- 添加 status 列
ALTER TABLE `audit_log` ADD COLUMN `status` VARCHAR(32) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果状态' AFTER `event_type`;

-- 添加 object_type 列（替代 resource_type 的新名称）
ALTER TABLE `audit_log` ADD COLUMN `object_type` VARCHAR(100) COMMENT '操作对象类型' AFTER `resource_type`;

-- 添加 object_id 列
ALTER TABLE `audit_log` ADD COLUMN `object_id` BIGINT COMMENT '操作对象ID' AFTER `object_type`;

-- 添加 before_data 列
ALTER TABLE `audit_log` ADD COLUMN `before_data` LONGTEXT COMMENT '操作前的数据' AFTER `change_detail`;

-- 添加 after_data 列
ALTER TABLE `audit_log` ADD COLUMN `after_data` LONGTEXT COMMENT '操作后的数据' AFTER `before_data`;

-- 添加 user_agent 列
ALTER TABLE `audit_log` ADD COLUMN `user_agent` VARCHAR(500) COMMENT '用户代理' AFTER `ip_address`;

-- 添加 request_id 列
ALTER TABLE `audit_log` ADD COLUMN `request_id` VARCHAR(128) COMMENT '请求ID' AFTER `user_agent`;

-- 添加 failure_reason 列
ALTER TABLE `audit_log` ADD COLUMN `failure_reason` TEXT COMMENT '失败原因' AFTER `request_id`;

-- 添加 duration_ms 列
ALTER TABLE `audit_log` ADD COLUMN `duration_ms` BIGINT COMMENT '操作耗时(毫秒)' AFTER `failure_reason`;

-- 添加 affected_records 列
ALTER TABLE `audit_log` ADD COLUMN `affected_records` INT COMMENT '受影响的记录数' AFTER `duration_ms`;

-- 添加 created_time 列（MyBatis映射用）
ALTER TABLE `audit_log` ADD COLUMN `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `created_at`;

-- 添加 archived 列
ALTER TABLE `audit_log` ADD COLUMN `archived` TINYINT DEFAULT 0 COMMENT '是否已归档' AFTER `created_time`;

-- 添加 remarks 列
ALTER TABLE `audit_log` ADD COLUMN `remarks` VARCHAR(500) COMMENT '备注信息' AFTER `archived`;

-- 添加索引优化查询
CREATE INDEX `idx_audit_user_event_time` ON `audit_log` (`user_id`, `event_type`, `created_at`);
CREATE INDEX `idx_audit_event_status_time` ON `audit_log` (`event_type`, `status`, `created_at`);
CREATE INDEX `idx_audit_object_type_id` ON `audit_log` (`object_type`, `object_id`);
CREATE INDEX `idx_audit_username` ON `audit_log` (`username`);

-- 完成
COMMIT;
