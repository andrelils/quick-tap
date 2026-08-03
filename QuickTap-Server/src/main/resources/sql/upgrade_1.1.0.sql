-- ============================================
-- QuickTap Server 数据库升级脚本
-- 版本: 1.1.0
-- 功能: 补充缺失的功能模块（语料库分类、AI配置等）
-- ============================================

-- 创建语料库分类表
CREATE TABLE IF NOT EXISTS `corpus_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `description` VARCHAR(500) COMMENT '分类描述',
  `corpus_count` INT NOT NULL DEFAULT 0 COMMENT '该分类下的语料数量',
  `enabled` BOOLEAN NOT NULL DEFAULT true COMMENT '是否启用',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  UNIQUE KEY `uk_merchant_name` (`merchant_id`, `name`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='语料库分类表';

-- 创建 AI 配置表
CREATE TABLE IF NOT EXISTS `ai_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'AI配置ID',
  `merchant_id` BIGINT COMMENT '商户ID（NULL表示全局配置）',
  `text_model` VARCHAR(100) DEFAULT 'gpt-3.5-turbo' COMMENT '文本生成模型',
  `image_model` VARCHAR(100) DEFAULT 'dall-e-3' COMMENT '图片生成模型',
  `video_model` VARCHAR(100) DEFAULT 'custom-video-model' COMMENT '视频生成模型',
  `api_key` VARCHAR(500) COMMENT '第三方 API Key（加密存储）',
  `api_secret` VARCHAR(500) COMMENT '第三方 API Secret（加密存储）',
  `enabled` BOOLEAN NOT NULL DEFAULT true COMMENT '是否启用',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  UNIQUE KEY `uk_merchant_id` (`merchant_id`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI配置表';

-- 创建二维码管理表
CREATE TABLE IF NOT EXISTS `qr_code` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '二维码ID',
  `code` VARCHAR(200) NOT NULL UNIQUE COMMENT '二维码编码（唯一）',
  `device_id` BIGINT NOT NULL COMMENT '设备ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
  `qr_data` LONGTEXT COMMENT '二维码包含的数据',
  `qr_image_url` VARCHAR(500) COMMENT '二维码图片URL',
  `type` ENUM('NFC', 'STANDARD') NOT NULL DEFAULT 'STANDARD' COMMENT '二维码类型',
  `status` ENUM('ACTIVE', 'INACTIVE', 'EXPIRED') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='二维码管理表';

-- 创建语料库分类关联表（在 corpus 表中添加 category_id 字段）
ALTER TABLE `corpus` ADD COLUMN `category_id` BIGINT COMMENT '分类ID' AFTER `merchant_id`;
ALTER TABLE `corpus` ADD KEY `idx_category_id` (`category_id`);

-- 创建推广平台高级配置表
CREATE TABLE IF NOT EXISTS `promotion_platform_advanced` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '高级配置ID',
  `platform_id` BIGINT NOT NULL COMMENT '推广平台ID',
  `param_name` VARCHAR(100) NOT NULL COMMENT '参数名称',
  `param_type` ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') NOT NULL DEFAULT 'STRING' COMMENT '参数类型',
  `is_required` BOOLEAN NOT NULL DEFAULT false COMMENT '是否必填',
  `is_visible` BOOLEAN NOT NULL DEFAULT true COMMENT '是否对商家可见',
  `description` VARCHAR(500) COMMENT '参数描述',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  UNIQUE KEY `uk_platform_param` (`platform_id`, `param_name`),
  KEY `idx_platform_id` (`platform_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推广平台高级配置表';

-- 创建定时任务执行日志表
CREATE TABLE IF NOT EXISTS `scheduled_task_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
  `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
  `status` ENUM('SUCCESS', 'FAILED') NOT NULL COMMENT '执行状态',
  `message` VARCHAR(500) COMMENT '执行信息',
  `executed_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',

  KEY `idx_task_name` (`task_name`),
  KEY `idx_status` (`status`),
  KEY `idx_executed_at` (`executed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时任务执行日志表';

-- 为 order_record 表添加过期检查字段
ALTER TABLE `order_record` ADD COLUMN `expired_at` TIMESTAMP COMMENT '订单过期时间' AFTER `pay_time`;
ALTER TABLE `order_record` ADD KEY `idx_expired_at` (`expired_at`);

-- 为 coupon 表添加过期检查字段
ALTER TABLE `coupon` ADD COLUMN `expired_at` TIMESTAMP COMMENT '卡券过期时间' AFTER `created_at`;
ALTER TABLE `coupon` ADD KEY `idx_expired_at` (`expired_at`);

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS `idx_merchant_expired` ON `order_record` (`merchant_id`, `expired_at`);
CREATE INDEX IF NOT EXISTS `idx_merchant_expired_coupon` ON `coupon` (`merchant_id`, `expired_at`);

-- 初始化全局 AI 配置（如果不存在）
INSERT IGNORE INTO `ai_config`
  (`merchant_id`, `text_model`, `image_model`, `video_model`, `enabled`, `created_at`)
VALUES
  (NULL, 'gpt-3.5-turbo', 'dall-e-3', 'custom-video-model', true, NOW());

COMMIT;
