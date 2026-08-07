-- ============================================
-- QuickTap Server 数据库升级脚本
-- 版本: 2.1.0
-- 说明: 自定义角色表 role 已存在（name=角色标识, description=角色名称, permissions=权限JSON）
--      本脚本仅作为 schema 参照，不做变更（避免与已有表结构冲突）
-- ============================================

-- 已有 role 表结构（供参考，勿重复执行）：
-- CREATE TABLE IF NOT EXISTS `role` (
--   `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
--   `name` VARCHAR(64) NOT NULL UNIQUE COMMENT '角色标识(如 editor)',
--   `description` VARCHAR(64) COMMENT '角色名称(如 编辑)',
--   `permissions` JSON COMMENT '权限列表(JSON数组)',
--   `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
--   `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

COMMIT;
