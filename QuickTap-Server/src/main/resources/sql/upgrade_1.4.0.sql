-- ============================================
-- QuickTap Server 数据库升级脚本
-- 版本: 1.4.0
-- 包含: 用户-商户关系、用户-设备关系、推广日志等新表
--      以及对Merchant、Device、Admin等表的扩展
-- ============================================

-- ============================================
-- Task 1: 创建user_merchant表 (用户-商户关系)
-- ============================================
CREATE TABLE IF NOT EXISTS `user_merchant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '关系ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  UNIQUE KEY `uk_user_merchant` (`user_id`, `merchant_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-商户关系表';

-- ============================================
-- Task 2: 创建user_device表 (用户-设备关系)
-- ============================================
CREATE TABLE IF NOT EXISTS `user_device` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '关系ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `device_id` BIGINT NOT NULL COMMENT '设备ID',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  UNIQUE KEY `uk_user_device` (`user_id`, `device_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-设备关系表';

-- ============================================
-- Task 3: 创建user_coupon表 (已在1.3.0创建，确保存在)
-- ============================================
-- CREATE TABLE IF NOT EXISTS `user_coupon` ... (已存在)

-- ============================================
-- Task 4: 创建promotion_click_log表 (推广点击日志)
-- ============================================
CREATE TABLE IF NOT EXISTS `promotion_click_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
  `user_id` BIGINT COMMENT '用户ID',
  `platform_id` BIGINT NOT NULL COMMENT '推广平台ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
  `device_id` BIGINT COMMENT '设备ID',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  KEY `idx_user_id` (`user_id`),
  KEY `idx_platform_id` (`platform_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推广点击日志表';

-- ============================================
-- Task 5: 创建scan_log表 (扫码日志)
-- ============================================
CREATE TABLE IF NOT EXISTS `scan_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
  `user_id` BIGINT COMMENT '用户ID',
  `device_id` BIGINT NOT NULL COMMENT '设备ID',
  `merchant_id` BIGINT COMMENT '商户ID',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  KEY `idx_user_id` (`user_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='扫码日志表';

-- ============================================
-- Task 6: 创建merchant_promotion_config表 (商户推广配置)
-- ============================================
CREATE TABLE IF NOT EXISTS `merchant_promotion_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
  `type` VARCHAR(32) NOT NULL COMMENT '配置类型: platform/coupon',
  `platform_id` BIGINT COMMENT '推广平台ID (当type=platform时)',
  `coupon_id` BIGINT COMMENT '卡券ID (当type=coupon时)',
  `params` JSON COMMENT '配置参数',
  `custom_name` VARCHAR(128) COMMENT '自定义名称',
  `custom_icon` VARCHAR(255) COMMENT '自定义图标',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用/1启用',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  UNIQUE KEY `uk_config` (`merchant_id`, `type`, `platform_id`, `coupon_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_type` (`type`),
  KEY `idx_platform_id` (`platform_id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户推广配置表';

-- ============================================
-- Task 7: 修改merchant表 - 添加5个新字段
-- ============================================
ALTER TABLE `merchant` ADD COLUMN IF NOT EXISTS `banner_images` JSON COMMENT '横幅图片(JSON数组)' AFTER `description`;
ALTER TABLE `merchant` ADD COLUMN IF NOT EXISTS `boss_wechat` VARCHAR(255) COMMENT '老板微信' AFTER `banner_images`;
ALTER TABLE `merchant` ADD COLUMN IF NOT EXISTS `business_hours` VARCHAR(128) COMMENT '营业时间' AFTER `boss_wechat`;
ALTER TABLE `merchant` ADD COLUMN IF NOT EXISTS `shop_images` JSON COMMENT '店铺图片(JSON数组)' AFTER `business_hours`;
ALTER TABLE `merchant` ADD COLUMN IF NOT EXISTS `referrer_code` VARCHAR(64) UNIQUE COMMENT '推荐人代码' AFTER `shop_images`;

-- ============================================
-- Task 8: 修改device表 - 添加2个新字段
-- ============================================
ALTER TABLE `device` ADD COLUMN IF NOT EXISTS `system_code` VARCHAR(64) UNIQUE COMMENT '系统编码' AFTER `type`;
ALTER TABLE `device` ADD COLUMN IF NOT EXISTS `url` VARCHAR(500) COMMENT '设备URL' AFTER `system_code`;

-- ============================================
-- Task 9: 修改admin表 - 添加1个新字段
-- ============================================
ALTER TABLE `admin` ADD COLUMN IF NOT EXISTS `user_code` VARCHAR(32) UNIQUE COMMENT '用户编码(如AD001)' AFTER `username`;

-- ============================================
-- Task 10: 修改ai_generate_record表 - 添加2个新字段
-- ============================================
ALTER TABLE `ai_generate_record` ADD COLUMN IF NOT EXISTS `mode` VARCHAR(16) COMMENT '生成模式: new(全新创作)/secondary(二次创作)' AFTER `type`;
ALTER TABLE `ai_generate_record` ADD COLUMN IF NOT EXISTS `corpus_id` BIGINT COMMENT '关联语料库ID' AFTER `mode`;

-- ============================================
-- Task 11: 修改promotion_platform表 - 添加10个新字段
-- ============================================
ALTER TABLE `promotion_platform` ADD COLUMN IF NOT EXISTS `code` VARCHAR(64) UNIQUE COMMENT '平台代码' AFTER `id`;
ALTER TABLE `promotion_platform` ADD COLUMN IF NOT EXISTS `color` VARCHAR(32) COMMENT '平台颜色(十六进制)' AFTER `code`;
ALTER TABLE `promotion_platform` ADD COLUMN IF NOT EXISTS `description` VARCHAR(255) COMMENT '平台描述' AFTER `color`;
ALTER TABLE `promotion_platform` ADD COLUMN IF NOT EXISTS `jump_mode` VARCHAR(32) COMMENT '跳转模式: scheme/webview/miniprogram/copy' AFTER `description`;
ALTER TABLE `promotion_platform` ADD COLUMN IF NOT EXISTS `scheme_template` VARCHAR(500) COMMENT 'Scheme跳转模板' AFTER `jump_mode`;
ALTER TABLE `promotion_platform` ADD COLUMN IF NOT EXISTS `web_url_template` VARCHAR(500) COMMENT 'H5网址模板' AFTER `scheme_template`;
ALTER TABLE `promotion_platform` ADD COLUMN IF NOT EXISTS `miniprogram_appid` VARCHAR(128) COMMENT '小程序AppID' AFTER `web_url_template`;
ALTER TABLE `promotion_platform` ADD COLUMN IF NOT EXISTS `miniprogram_path_template` VARCHAR(500) COMMENT '小程序路径模板' AFTER `miniprogram_appid`;
ALTER TABLE `promotion_platform` ADD COLUMN IF NOT EXISTS `required_params` JSON COMMENT '必需参数(JSON数组)' AFTER `miniprogram_path_template`;
ALTER TABLE `promotion_platform` ADD COLUMN IF NOT EXISTS `optional_params` JSON COMMENT '可选参数(JSON数组)' AFTER `required_params`;

-- ============================================
-- 创建复合索引用于优化查询
-- ============================================
CREATE INDEX IF NOT EXISTS `idx_promotion_click_merchant_created` ON `promotion_click_log` (`merchant_id`, `created_at`);
CREATE INDEX IF NOT EXISTS `idx_scan_log_device_created` ON `scan_log` (`device_id`, `created_at`);
CREATE INDEX IF NOT EXISTS `idx_merchant_config_status` ON `merchant_promotion_config` (`merchant_id`, `status`);

COMMIT;
