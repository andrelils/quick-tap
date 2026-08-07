-- QuickTap 数据库初始化脚本
-- 数据库：MySQL 5.7.44+
-- 字符集：utf8mb4

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS quick_tap DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE quick_tap;

-- =====================================================
-- 管理员表
-- =====================================================
CREATE TABLE IF NOT EXISTS `admin` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` VARCHAR(64) NOT NULL UNIQUE COMMENT '登录账号',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(bcrypt加密)',
  `nickname` VARCHAR(64) COMMENT '昵称',
  `email` VARCHAR(64) COMMENT '邮箱',
  `phone` VARCHAR(32) COMMENT '电话号码',
  `role` VARCHAR(32) NOT NULL DEFAULT 'admin' COMMENT '角色: super_admin/admin/merchant',
  `merchant_id` INT UNSIGNED COMMENT '关联商户ID(merchant角色时)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用/0禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- =====================================================
-- C端用户表
-- =====================================================
CREATE TABLE IF NOT EXISTS `user` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(64) UNIQUE COMMENT '登录账号',
  `password` VARCHAR(255) COMMENT '密码(bcrypt加密)',
  `openid` VARCHAR(128) UNIQUE COMMENT '微信openid',
  `unionid` VARCHAR(128) UNIQUE COMMENT '微信unionid',
  `nickname` VARCHAR(64) COMMENT '昵称',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `phone` VARCHAR(32) COMMENT '绑定手机号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用/0禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_openid` (`openid`),
  KEY `idx_unionid` (`unionid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 商户表
-- =====================================================
CREATE TABLE IF NOT EXISTS `merchant` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商户ID',
  `name` VARCHAR(128) NOT NULL COMMENT '商户名称',
  `logo` VARCHAR(255) COMMENT '商户logo URL',
  `contact_name` VARCHAR(64) COMMENT '联系人名称',
  `contact_phone` VARCHAR(32) COMMENT '联系人电话',
  `address` VARCHAR(255) COMMENT '商户地址',
  `wifi_name` VARCHAR(128) COMMENT '门店WiFi名称',
  `wifi_password` VARCHAR(128) COMMENT '门店WiFi密码',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态: 0待审核/1通过/2拒绝',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用/0停用',
  `storage_used` BIGINT DEFAULT 0 COMMENT '已使用存储(MB)',
  `storage_limit` BIGINT DEFAULT 10240 COMMENT '存储限制(MB, 默认10GB)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户表';

-- =====================================================
-- 设备表
-- =====================================================
CREATE TABLE IF NOT EXISTS `device` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `device_no` VARCHAR(64) NOT NULL UNIQUE COMMENT '设备编号(唯一)',
  `name` VARCHAR(128) COMMENT '设备名称',
  `merchant_id` INT UNSIGNED NOT NULL COMMENT '关联商户ID',
  `type` VARCHAR(32) NOT NULL COMMENT '设备类型: nfc/qrcode',
  `qrcode` VARCHAR(255) COMMENT '二维码数据',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用/0停用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_no` (`device_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备表';

-- =====================================================
-- 推广平台表（超管配置）
-- =====================================================
CREATE TABLE IF NOT EXISTS `promotion_platform` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '推广平台ID',
  `code` VARCHAR(64) NOT NULL UNIQUE COMMENT '平台代码(douyin/xiaohongshu等)',
  `name` VARCHAR(128) NOT NULL COMMENT '平台名称',
  `icon` VARCHAR(255) COMMENT '平台图标URL',
  `jump_mode` VARCHAR(32) COMMENT '跳转方式: scheme/webview/miniprogram/copy',
  `scheme_template` VARCHAR(500) COMMENT 'URL scheme模板',
  `web_url_template` VARCHAR(500) COMMENT 'H5链接模板',
  `required_params` JSON COMMENT '必填参数定义(JSON)',
  `optional_params` JSON COMMENT '可选参数定义(JSON)',
  `sort` INT DEFAULT 0 COMMENT '排序号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用/0停用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推广平台表(超管维护)';

-- =====================================================
-- 商户推广配置表
-- =====================================================
CREATE TABLE IF NOT EXISTS `merchant_promotion_config` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `merchant_id` INT UNSIGNED NOT NULL COMMENT '商户ID',
  `platform_id` INT UNSIGNED NOT NULL COMMENT '推广平台ID',
  `params` JSON COMMENT '参数值(JSON格式)',
  `custom_name` VARCHAR(128) COMMENT '自定义名称',
  `custom_icon` VARCHAR(255) COMMENT '自定义图标URL',
  `sort` INT DEFAULT 0 COMMENT '排序号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用/0禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_platform` (`merchant_id`, `platform_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_platform_id` (`platform_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户推广配置表';

-- =====================================================
-- 卡券表
-- =====================================================
CREATE TABLE IF NOT EXISTS `coupon` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '卡券ID',
  `merchant_id` INT UNSIGNED NOT NULL COMMENT '商户ID',
  `title` VARCHAR(128) NOT NULL COMMENT '卡券标题',
  `type` VARCHAR(32) NOT NULL COMMENT '卡券类型: cash(现金)/discount(折扣)',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '金额/比例',
  `min_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '最低消费金额',
  `total_count` INT NOT NULL COMMENT '总数',
  `remain_count` INT NOT NULL COMMENT '剩余数',
  `start_time` DATETIME COMMENT '有效期开始',
  `end_time` DATETIME COMMENT '有效期结束',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用/0停用',
  `link` VARCHAR(500) COMMENT '第三方平台跳转链接',
  `description` VARCHAR(500) COMMENT '使用说明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卡券表';

-- =====================================================
-- 套餐表
-- =====================================================
CREATE TABLE IF NOT EXISTS `plan` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
  `name` VARCHAR(128) NOT NULL COMMENT '套餐名称',
  `level` VARCHAR(32) NOT NULL COMMENT '套餐等级: basic/pro/enterprise',
  `price` DECIMAL(10,2) NOT NULL COMMENT '套餐价格',
  `duration_months` INT NOT NULL COMMENT '购买时长(月)',
  `device_count` INT NOT NULL COMMENT '设备数量限制',
  `text_quota` INT DEFAULT 0 COMMENT '文字生成额度',
  `image_quota` INT DEFAULT 0 COMMENT '图片生成额度',
  `video_quota` INT DEFAULT 0 COMMENT '视频生成额度',
  `storage_limit` BIGINT DEFAULT 1024 COMMENT '存储空间限制(MB)',
  `recommend` TINYINT DEFAULT 0 COMMENT '是否推荐: 0否/1是',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用/0停用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_level` (`level`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='套餐表';

-- =====================================================
-- 二维码表
-- =====================================================
CREATE TABLE IF NOT EXISTS `qrcode` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '二维码ID',
  `code` VARCHAR(255) NOT NULL UNIQUE COMMENT '二维码内容(唯一)',
  `qrcode_url` VARCHAR(255) COMMENT '二维码图片URL',
  `merchant_id` INT UNSIGNED COMMENT '绑定的商户ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用/0停用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='二维码表';

-- =====================================================
-- 订单表
-- =====================================================
CREATE TABLE IF NOT EXISTS `order_record` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号(唯一)',
  `merchant_id` INT UNSIGNED NOT NULL COMMENT '商户ID',
  `plan_id` INT UNSIGNED NOT NULL COMMENT '套餐ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  `status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '订单状态: pending/paid/expired',
  `expire_at` DATETIME COMMENT '订单过期时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =====================================================
-- AI生成记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS `ai_generate_record` (
  `id` VARCHAR(36) NOT NULL COMMENT '记录ID(UUID)',
  `record_id` VARCHAR(36) NOT NULL COMMENT '记录ID(UUID)',
  `merchant_id` INT UNSIGNED COMMENT '商户ID(可选)',
  `type` VARCHAR(32) NOT NULL COMMENT '生成类型: text/image/video',
  `prompt` LONGTEXT COMMENT '用户输入的提示词',
  `result` LONGTEXT COMMENT '生成结果',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1成功/0失败',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI生成记录表';

-- =====================================================
-- 权限表
-- =====================================================
CREATE TABLE IF NOT EXISTS `permissions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `code` VARCHAR(100) NOT NULL UNIQUE COMMENT '权限代码(resource.action格式)',
  `resource` VARCHAR(50) NOT NULL COMMENT '资源名称(merchant/device/ai/marketing/system/dashboard)',
  `action` VARCHAR(50) NOT NULL COMMENT '操作名称(view/create/update/delete等)',
  `description` VARCHAR(200) COMMENT '权限描述',
  `category` VARCHAR(50) COMMENT '权限分类(UI中的分组)',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1启用/0停用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_resource` (`resource`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- =====================================================
-- 创建索引
-- =====================================================
ALTER TABLE `device` ADD CONSTRAINT `fk_device_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE;
ALTER TABLE `coupon` ADD CONSTRAINT `fk_coupon_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE;
ALTER TABLE `merchant_promotion_config` ADD CONSTRAINT `fk_mpc_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE;
ALTER TABLE `merchant_promotion_config` ADD CONSTRAINT `fk_mpc_platform` FOREIGN KEY (`platform_id`) REFERENCES `promotion_platform` (`id`) ON DELETE CASCADE;
ALTER TABLE `order_record` ADD CONSTRAINT `fk_order_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE;
ALTER TABLE `qrcode` ADD CONSTRAINT `fk_qrcode_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE SET NULL;

-- =====================================================
-- 初始化超级管理员账号
-- =====================================================
INSERT INTO `admin` (`username`, `password`, `nickname`, `role`, `status`, `created_at`, `updated_at`) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/SLm', '超级管理员', 'super_admin', 1, NOW(), NOW());

-- 初始化套餐数据
INSERT INTO `plan` (`name`, `level`, `price`, `duration_months`, `device_count`, `text_quota`, `image_quota`, `video_quota`, `storage_limit`, `recommend`, `status`) VALUES
('基础版', 'basic', 99.00, 1, 10, 100, 50, 10, 1024, 0, 1),
('专业版', 'pro', 299.00, 1, 50, 500, 200, 50, 5120, 1, 1),
('企业版', 'enterprise', 999.00, 1, 200, 2000, 1000, 200, 20480, 0, 1);

-- 初始化推广平台
INSERT INTO `promotion_platform` (`code`, `name`, `icon`, `jump_mode`, `sort`, `status`) VALUES
('douyin', '抖音', 'https://via.placeholder.com/128', 'scheme', 1, 1),
('xiaohongshu', '小红书', 'https://via.placeholder.com/128', 'scheme', 2, 1),
('meituan', '美团', 'https://via.placeholder.com/128', 'webview', 3, 1),
('dianping', '点评', 'https://via.placeholder.com/128', 'webview', 4, 1);

-- 设置表注释
ALTER TABLE `admin` COMMENT='管理员表';
ALTER TABLE `user` COMMENT='用户表';
ALTER TABLE `merchant` COMMENT='商户表';
ALTER TABLE `device` COMMENT='设备表';
ALTER TABLE `promotion_platform` COMMENT='推广平台表';
ALTER TABLE `merchant_promotion_config` COMMENT='商户推广配置表';
ALTER TABLE `coupon` COMMENT='卡券表';
ALTER TABLE `plan` COMMENT='套餐表';
ALTER TABLE `qrcode` COMMENT='二维码表';
ALTER TABLE `order_record` COMMENT='订单表';
ALTER TABLE `ai_generate_record` COMMENT='AI生成记录表';
