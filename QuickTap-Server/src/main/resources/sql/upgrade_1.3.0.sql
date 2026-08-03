-- ============================================
-- QuickTap Server 数据库最终升级脚本
-- 版本: 1.3.0
-- 包含: 推广平台、用户卡券、设备扩展等功能
-- ============================================

-- ============================================
-- 推广平台高级配置表（已在upgrade_1.2.0中创建）
-- ============================================

-- ============================================
-- 用户卡券关联表
-- ============================================
CREATE TABLE IF NOT EXISTS `user_coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '用户卡券ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `coupon_id` BIGINT NOT NULL COMMENT '卡券ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1未使用/2已使用/3已过期',
  `used_at` TIMESTAMP COMMENT '使用时间',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  UNIQUE KEY `uk_user_coupon` (`user_id`, `coupon_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_used_at` (`used_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户卡券表';

-- ============================================
-- 设备表扩展（添加新字段）
-- ============================================
ALTER TABLE `device` ADD COLUMN IF NOT EXISTS `location` VARCHAR(255) COMMENT '设备位置' AFTER `type`;
ALTER TABLE `device` ADD COLUMN IF NOT EXISTS `mac_address` VARCHAR(100) COMMENT 'MAC地址' AFTER `location`;
ALTER TABLE `device` ADD COLUMN IF NOT EXISTS `ip_address` VARCHAR(100) COMMENT 'IP地址' AFTER `mac_address`;
ALTER TABLE `device` ADD COLUMN IF NOT EXISTS `bind_qr_code_id` BIGINT COMMENT '绑定的二维码ID' AFTER `ip_address`;

-- 添加索引
ALTER TABLE `device` ADD KEY `idx_bind_qr_code_id` (`bind_qr_code_id`);
ALTER TABLE `device` ADD KEY `idx_location` (`location`);

-- ============================================
-- 订单表扩展（已在upgrade_1.2.0中添加expired_at）
-- ============================================
-- 如果尚未添加，运行以下命令：
-- ALTER TABLE `order_record` ADD COLUMN IF NOT EXISTS `expired_at` TIMESTAMP COMMENT '订单过期时间' AFTER `pay_time`;
-- ALTER TABLE `order_record` ADD KEY IF NOT EXISTS `idx_expired_at` (`expired_at`);

-- ============================================
-- 卡券表扩展（已在upgrade_1.2.0中添加expired_at）
-- ============================================
-- 如果尚未添加，运行以下命令：
-- ALTER TABLE `coupon` ADD COLUMN IF NOT EXISTS `expired_at` TIMESTAMP COMMENT '卡券过期时间' AFTER `created_at`;
-- ALTER TABLE `coupon` ADD KEY IF NOT EXISTS `idx_expired_at` (`expired_at`);

-- ============================================
-- 创建复合索引用于查询优化
-- ============================================
CREATE INDEX IF NOT EXISTS `idx_user_coupon_status` ON `user_coupon` (`user_id`, `status`);
CREATE INDEX IF NOT EXISTS `idx_merchant_coupon_status` ON `coupon` (`merchant_id`, `status`);
CREATE INDEX IF NOT EXISTS `idx_device_merchant` ON `device` (`merchant_id`, `status`);
CREATE INDEX IF NOT EXISTS `idx_promotion_merchant` ON `merchant_promotion_config` (`merchant_id`, `status`);

-- ============================================
-- 定时任务执行日志表（已在upgrade_1.2.0中创建）
-- ============================================

-- ============================================
-- 用户登录日志表（新增）
-- ============================================
CREATE TABLE IF NOT EXISTS `user_login_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `login_type` VARCHAR(50) COMMENT '登录方式: password/wechat/etc',
  `ip_address` VARCHAR(100) COMMENT '登录IP',
  `device_info` VARCHAR(255) COMMENT '设备信息',
  `login_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `logout_at` TIMESTAMP COMMENT '登出时间',

  KEY `idx_user_id` (`user_id`),
  KEY `idx_login_type` (`login_type`),
  KEY `idx_login_at` (`login_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录日志表';

-- ============================================
-- 操作审计日志表（新增）
-- ============================================
CREATE TABLE IF NOT EXISTS `audit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
  `user_id` BIGINT COMMENT '操作用户ID',
  `merchant_id` BIGINT COMMENT '商户ID',
  `operation` VARCHAR(100) COMMENT '操作类型',
  `resource_type` VARCHAR(100) COMMENT '资源类型: user/coupon/device/etc',
  `resource_id` BIGINT COMMENT '资源ID',
  `change_detail` LONGTEXT COMMENT '变更详情(JSON)',
  `ip_address` VARCHAR(100) COMMENT '操作IP',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_operation` (`operation`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志表';

-- ============================================
-- 初始化数据
-- ============================================

-- 如果内置推广平台不存在，初始化7个内置平台
INSERT IGNORE INTO `promotion_platform` (`code`, `name`, `description`, `sort`, `status`, `created_at`, `updated_at`)
VALUES
  ('douyin', '抖音', '抖音平台配置', 1, 1, NOW(), NOW()),
  ('xiaohongshu', '小红书', '小红书平台配置', 2, 1, NOW(), NOW()),
  ('meituan', '美团', '美团平台配置', 3, 1, NOW(), NOW()),
  ('dianping', '大众点评', '大众点评平台配置', 4, 1, NOW(), NOW()),
  ('weibo', '微博', '微博平台配置', 5, 1, NOW(), NOW()),
  ('kuaishou', '快手', '快手平台配置', 6, 1, NOW(), NOW()),
  ('bilibili', 'B站', 'B站平台配置', 7, 1, NOW(), NOW());

-- ============================================
-- 数据迁移助手SQL（用于Mapper方法实现）
-- ============================================

-- 更新过期订单（需在OrderRecordMapper实现deleteExpiredOrders方法）
-- UPDATE order_record
-- SET status = 'expired'
-- WHERE expired_at IS NOT NULL AND expired_at < NOW() AND status IN ('pending', 'confirmed');

-- 更新过期卡券（需在CouponMapper实现updateExpiredCoupons方法）
-- UPDATE coupon
-- SET status = 0
-- WHERE end_time < NOW() AND status = 1;

-- 标记过期用户卡券（需在UserCouponMapper实现markExpiredCoupons方法）
-- UPDATE user_coupon uc
-- INNER JOIN coupon c ON uc.coupon_id = c.id
-- SET uc.status = 3
-- WHERE c.end_time < NOW() AND uc.status = 1;

-- 清理回收站中的语料库（需在CorpusMapper实现deletePermanentlyTrashedCorpus方法）
-- DELETE FROM corpus
-- WHERE is_deleted = true AND updated_at < DATE_SUB(NOW(), INTERVAL 30 DAY);

COMMIT;
