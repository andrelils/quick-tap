-- ============================================
-- QuickTap Server 数据库升级脚本
-- 版本: 2.0.0
-- 功能变更：
--   1. 扩展 audit_log 表，支持完整的安全审计功能
--      - 添加事件类型、状态、失败原因等字段
--      - 添加 User-Agent、请求ID 等细节信息
--      - 支持数据变更追踪（变更前后数据）
-- ============================================

-- ============================================
-- 重建 audit_log 表（扩展安全审计功能）
-- ============================================

-- 检查是否需要迁移现有数据
-- 如果 audit_log 表已存在旧结构，备份现有数据后重建

-- 方案A: 如果表不存在，直接创建完整表结构
CREATE TABLE IF NOT EXISTS `audit_log_new` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',

  -- 用户信息
  `user_id` BIGINT COMMENT '操作用户ID',
  `username` VARCHAR(128) COMMENT '操作用户名',

  -- 审计事件信息
  `event_type` VARCHAR(64) NOT NULL COMMENT '事件类型: LOGIN/LOGOUT/LOGIN_FAILED/USER_CREATE/USER_UPDATE/USER_DELETE/ROLE_GRANT/ROLE_REVOKE/SENSITIVE_OPERATION/等',
  `status` VARCHAR(32) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果状态: SUCCESS/FAILURE/DENIED/WARNING',
  `description` TEXT COMMENT '操作描述',

  -- 操作对象信息
  `object_type` VARCHAR(100) COMMENT '操作对象类型: User/Role/Permission/Document/等',
  `object_id` BIGINT COMMENT '操作对象ID',

  -- 数据变更追踪
  `before_data` LONGTEXT COMMENT '操作前的数据(JSON格式)',
  `after_data` LONGTEXT COMMENT '操作后的数据(JSON格式)',

  -- 请求上下文
  `ip_address` VARCHAR(100) COMMENT '客户端IP地址',
  `user_agent` VARCHAR(500) COMMENT '用户代理(User-Agent)',
  `request_id` VARCHAR(128) COMMENT '请求ID(用于关联日志)',

  -- 故障信息
  `failure_reason` TEXT COMMENT '失败原因(仅当status为FAILURE时)',

  -- 性能指标
  `duration_ms` BIGINT COMMENT '操作耗时(毫秒)',
  `affected_records` INT COMMENT '受影响的记录数',

  -- 时间戳
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间(兼容MyBatis映射)',

  -- 管理字段
  `archived` TINYINT DEFAULT 0 COMMENT '是否已归档: 0未归档/1已归档',
  `remarks` VARCHAR(500) COMMENT '备注信息',

  -- 索引
  KEY `idx_user_id` (`user_id`),
  KEY `idx_username` (`username`),
  KEY `idx_event_type` (`event_type`),
  KEY `idx_status` (`status`),
  KEY `idx_ip_address` (`ip_address`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_object_type_id` (`object_type`, `object_id`),
  KEY `idx_user_event_time` (`user_id`, `event_type`, `created_at`),
  KEY `idx_event_status_time` (`event_type`, `status`, `created_at`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志表(V2.0完整版)';

-- 如果旧表存在，迁移数据然后删除
-- 注意：这只会迁移共同的列，新列会为NULL
-- 只有在确认升级时才执行以下语句：
-- BEGIN;
-- INSERT INTO `audit_log_new` (
--   `id`, `user_id`, `ip_address`, `created_at`, `event_type`, `status`
-- ) SELECT
--   `id`, `user_id`, `ip_address`, `created_at`,
--   COALESCE(`operation`, 'SENSITIVE_OPERATION') as event_type,
--   'SUCCESS' as status
-- FROM `audit_log`
-- WHERE NOT EXISTS (SELECT 1 FROM `audit_log_new` WHERE `id` = `audit_log`.`id`);
-- DROP TABLE IF EXISTS `audit_log`;
-- RENAME TABLE `audit_log_new` TO `audit_log`;
-- COMMIT;

-- 方案B: 直接修改已存在的 audit_log 表（推荐，可保留现有数据）
-- 如果 audit_log 表已存在，需要添加缺失的列
ALTER TABLE `audit_log`
  ADD COLUMN IF NOT EXISTS `username` VARCHAR(128) COMMENT '操作用户名' AFTER `user_id`,
  ADD COLUMN IF NOT EXISTS `event_type` VARCHAR(64) NOT NULL DEFAULT 'SENSITIVE_OPERATION' COMMENT '事件类型' AFTER `operation`,
  ADD COLUMN IF NOT EXISTS `status` VARCHAR(32) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果状态' AFTER `event_type`,
  ADD COLUMN IF NOT EXISTS `object_type` VARCHAR(100) COMMENT '操作对象类型' AFTER `resource_type`,
  ADD COLUMN IF NOT EXISTS `object_id` BIGINT COMMENT '操作对象ID' AFTER `object_type`,
  ADD COLUMN IF NOT EXISTS `before_data` LONGTEXT COMMENT '操作前的数据' AFTER `change_detail`,
  ADD COLUMN IF NOT EXISTS `after_data` LONGTEXT COMMENT '操作后的数据' AFTER `before_data`,
  ADD COLUMN IF NOT EXISTS `user_agent` VARCHAR(500) COMMENT '用户代理' AFTER `ip_address`,
  ADD COLUMN IF NOT EXISTS `request_id` VARCHAR(128) COMMENT '请求ID' AFTER `user_agent`,
  ADD COLUMN IF NOT EXISTS `failure_reason` TEXT COMMENT '失败原因' AFTER `request_id`,
  ADD COLUMN IF NOT EXISTS `duration_ms` BIGINT COMMENT '操作耗时(毫秒)' AFTER `failure_reason`,
  ADD COLUMN IF NOT EXISTS `affected_records` INT COMMENT '受影响的记录数' AFTER `duration_ms`,
  ADD COLUMN IF NOT EXISTS `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `created_at`,
  ADD COLUMN IF NOT EXISTS `archived` TINYINT DEFAULT 0 COMMENT '是否已归档' AFTER `created_time`,
  ADD COLUMN IF NOT EXISTS `remarks` VARCHAR(500) COMMENT '备注信息' AFTER `archived`;

-- 添加新的复合索引（优化查询性能）
CREATE INDEX IF NOT EXISTS `idx_audit_user_event_time` ON `audit_log` (`user_id`, `event_type`, `created_at`);
CREATE INDEX IF NOT EXISTS `idx_audit_event_status_time` ON `audit_log` (`event_type`, `status`, `created_at`);
CREATE INDEX IF NOT EXISTS `idx_audit_object_type_id` ON `audit_log` (`object_type`, `object_id`);
CREATE INDEX IF NOT EXISTS `idx_audit_username` ON `audit_log` (`username`);

COMMIT;
