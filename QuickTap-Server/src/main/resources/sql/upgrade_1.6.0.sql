-- ============================================================
-- QuickTap-Server v1.6.0 升级脚本
-- 功能变更：
--   1. 补充高频查询的复合索引，优化分页与统计查询性能
--      依据：CorpusMapper / AiGenerateRecordMapper / OrderMapper 实际查询模式
--   2. system_setting 表结构初始化（SystemController/Service 使用）
--   3. admin 表新增 avatar 字段，支持管理员/商家头像上传
--   4. user 表 avatar 字段已存在，无需变更
-- ============================================================

-- ============================================
-- 0. admin 表新增 avatar 字段（支持头像上传）
-- ============================================
ALTER TABLE `admin` ADD COLUMN IF NOT EXISTS `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL' AFTER `nickname`;

-- ============================================
-- 1. system_setting 表（key-value 全局配置）
--    若已存在则跳过；SystemController 依赖此表
-- ============================================
CREATE TABLE IF NOT EXISTS `system_setting` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `key_name` VARCHAR(128) NOT NULL COMMENT '配置键(唯一)',
  `value` TEXT COMMENT '配置值',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key_name` (`key_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统设置表';

-- ============================================
-- 2. ai_generate_record 复合索引
--    查询模式：按 merchant_id + type 分页，按 created_at 倒序
--    现有 idx_merchant_id 为单列，补充复合索引避免回表 + filesort
-- ============================================
CREATE INDEX IF NOT EXISTS `idx_merchant_type_created`
  ON `ai_generate_record` (`merchant_id`, `type`, `created_at`);

-- ============================================
-- 3. corpus 表索引补充
--    查询模式：
--      a) WHERE merchant_id=? AND status=1 ORDER BY created_at DESC
--      b) WHERE merchant_id=? AND status=0 ORDER BY updated_at DESC (回收站)
--      c) WHERE category=? AND status=1 ORDER BY created_at DESC
-- ============================================
CREATE INDEX IF NOT EXISTS `idx_corpus_merchant_status_created`
  ON `corpus` (`merchant_id`, `status`, `created_at`);
CREATE INDEX IF NOT EXISTS `idx_corpus_category_status`
  ON `corpus` (`category`, `status`);
CREATE INDEX IF NOT EXISTS `idx_corpus_status_updated`
  ON `corpus` (`status`, `updated_at`);

-- ============================================
-- 4. order_record 复合索引
--    查询模式：按 status 分页 + created_at 排序
--    统计模式：按 status + created_at 范围统计
-- ============================================
CREATE INDEX IF NOT EXISTS `idx_order_status_created`
  ON `order_record` (`status`, `created_at`);

-- ============================================
-- 5. coupon 复合索引
--    小程序列表查询：WHERE status=1 AND remain_count>0
--                    AND start_time<=NOW() AND end_time>=NOW()
--    已有 idx_status，补充 (status, remain_count) 提升过滤效率
-- ============================================
CREATE INDEX IF NOT EXISTS `idx_coupon_status_remain`
  ON `coupon` (`status`, `remain_count`);

-- ============================================
-- 6. device.system_code 单列索引
--    MiniappController 频繁按 system_code 查询设备
--    system_code 虽有 UNIQUE 约束自带索引，但部分环境升级脚本可能未执行
--    若已存在同名索引，CREATE INDEX IF NOT EXISTS 会自动跳过
-- ============================================
-- system_code 已是 UNIQUE，无需重复创建

COMMIT;
