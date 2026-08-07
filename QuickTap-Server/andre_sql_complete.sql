-- ======================================
-- QuickTap 数据库初始化脚本
-- 脚本功能：
-- 1. 表存在则跳过，不存在则新建
-- 2. 字段存在则跳过，不存在则新增
-- 3. 索引存在则跳过，不存在则新建
-- ======================================

-- ======================================
-- 1. Plan（套餐表）
-- ======================================
CREATE TABLE IF NOT EXISTS `plan` (
                                      `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                      `name` VARCHAR(255) COMMENT '套餐名称',
    `level` VARCHAR(255) COMMENT '套餐等级: basic/pro/enterprise',
    `price` DECIMAL(10, 2) COMMENT '套餐价格',
    `duration_months` INT COMMENT '购买时长(月)',
    `device_count` INT COMMENT '设备数量限制',
    `text_quota` INT COMMENT '文字生成额度',
    `image_quota` INT COMMENT '图片生成额度',
    `video_quota` INT COMMENT '视频生成额度',
    `storage_limit` BIGINT COMMENT '存储空间限制(MB)',
    `recommend` INT DEFAULT 0 COMMENT '是否推荐: 0否/1是',
    `status` INT DEFAULT 1 COMMENT '状态：1启用/0停用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='套餐表';

-- ======================================
-- 2. Merchant（商户表）
-- ======================================
CREATE TABLE IF NOT EXISTS `merchant` (
                                          `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                          `name` VARCHAR(255) COMMENT '商户名称',
    `logo` VARCHAR(255) COMMENT '商户logo',
    `contact_name` VARCHAR(255) COMMENT '联系人名称',
    `contact_phone` VARCHAR(255) COMMENT '联系人电话',
    `contact_email` VARCHAR(255) COMMENT '联系人Email',
    `address` VARCHAR(255) COMMENT '商户地址',
    `wifi_name` VARCHAR(255) COMMENT '门店WiFi名称',
    `wifi_password` VARCHAR(255) COMMENT 'WiFi密码',
    `audit_status` INT DEFAULT 0 COMMENT '审核状态：0待审核/1通过/2拒绝',
    `status` INT DEFAULT 1 COMMENT '状态：1启用/0停用',
    `plan_id` INT COMMENT '套餐ID',
    `storage_used` BIGINT DEFAULT 0 COMMENT '已使用存储(MB)',
    `storage_limit` BIGINT COMMENT '存储限制(MB)',
    `banner_images` VARCHAR(255) COMMENT '横幅图片(JSON数组)',
    `boss_wechat` VARCHAR(255) COMMENT '老板微信',
    `business_hours` VARCHAR(255) COMMENT '营业时间',
    `shop_images` VARCHAR(255) COMMENT '店铺图片(JSON数组)',
    `referrer_code` VARCHAR(255) COMMENT '推荐人代码',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_plan_id` (`plan_id`),
    CONSTRAINT `fk_merchant_plan` FOREIGN KEY (`plan_id`) REFERENCES `plan` (`id`) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户表';

-- ======================================
-- 3. Admin（管理员表）
-- ======================================
CREATE TABLE IF NOT EXISTS `admin` (
                                       `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                       `username` VARCHAR(255) COMMENT '登录账号',
    `user_code` VARCHAR(255) COMMENT '用户编码(如AD001)',
    `password` VARCHAR(255) COMMENT '密码（bcrypt加密）',
    `nickname` VARCHAR(255) COMMENT '昵称',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `email` VARCHAR(255) COMMENT '邮箱',
    `phone` VARCHAR(255) COMMENT '电话',
    `role` VARCHAR(255) DEFAULT 'admin' COMMENT '角色：super_admin/admin/merchant',
    `merchant_id` INT COMMENT '商户ID',
    `status` INT DEFAULT 1 COMMENT '状态：1启用/0禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_merchant_id` (`merchant_id`),
    CONSTRAINT `fk_admin_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- ======================================
-- 4. User（C端用户表）
-- ======================================
CREATE TABLE IF NOT EXISTS `user` (
                                      `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                      `username` VARCHAR(255) COMMENT '用户名（用于登录）',
    `password` VARCHAR(255) COMMENT '密码（BCrypt加密）',
    `openid` VARCHAR(255) COMMENT '微信openid',
    `unionid` VARCHAR(255) COMMENT '微信unionid',
    `nickname` VARCHAR(255) COMMENT '昵称',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `phone` VARCHAR(255) COMMENT '绑定手机号',
    `status` INT DEFAULT 1 COMMENT '状态：1启用/0禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_openid` (`openid`),
    UNIQUE KEY `uk_phone` (`phone`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='C端用户表';

-- ======================================
-- 5. UserMerchant（用户-商户关系表）
-- ======================================
CREATE TABLE IF NOT EXISTS `user_merchant` (
                                               `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                               `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                               `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
                                               `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                               `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                               UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_merchant_id` (`merchant_id`),
    CONSTRAINT `fk_user_merchant_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_merchant_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-商户关系表';

-- ======================================
-- 6. Device（设备表）
-- ======================================
CREATE TABLE IF NOT EXISTS `device` (
                                        `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                        `device_no` VARCHAR(255) UNIQUE COMMENT '设备编号（唯一）',
    `name` VARCHAR(255) COMMENT '设备名称',
    `merchant_id` INT COMMENT '关联商户ID',
    `type` VARCHAR(255) COMMENT '设备类型: nfc/qrcode',
    `system_code` VARCHAR(255) COMMENT '系统编码',
    `url` VARCHAR(255) COMMENT '设备URL',
    `qrcode` VARCHAR(255) COMMENT '二维码数据',
    `location` VARCHAR(255) COMMENT '设备位置',
    `mac_address` VARCHAR(255) COMMENT 'MAC地址',
    `ip_address` VARCHAR(255) COMMENT 'IP地址',
    `status` INT DEFAULT 1 COMMENT '状态：1启用/0停用/2故障',
    `bind_qr_code_id` BIGINT COMMENT '绑定的二维码ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_device_no` (`device_no`),
    CONSTRAINT `fk_device_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备表';

-- ======================================
-- 7. QrCode（二维码表）
-- ======================================
-- IF EXISTS：表存在才删除，不存在什么都不做，不会报1051错误
DROP TABLE IF EXISTS andre_sql.qr_code;

CREATE TABLE IF NOT EXISTS `qr_code` (
                                         `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                         `code` VARCHAR(255) UNIQUE COMMENT '二维码编码（唯一）',
    -- 和device.id保持完全一致：INT UNSIGNED NULL
    `device_id` INT UNSIGNED NULL COMMENT '设备ID',
    -- merchant表id也需要确认是 int unsigned，按你之前业务推断也是int unsigned
    `merchant_id` INT UNSIGNED NULL COMMENT '商户ID',
    `qr_data` VARCHAR(255) COMMENT '二维码包含的数据',
    `qr_image_url` VARCHAR(255) COMMENT '二维码图片URL',
    `type` VARCHAR(255) COMMENT '二维码类型（NFC或STANDARD）',
    `status` VARCHAR(255) COMMENT '状态（ACTIVE、INACTIVE、EXPIRED）',
    `bind_status` INT DEFAULT 0 COMMENT '绑定状态：0未绑定/1已绑定',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_device_id` (`device_id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_code` (`code`),
    KEY `idx_bind_status` (`bind_status`),
    CONSTRAINT `fk_qr_code_device` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_qr_code_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='二维码表';

-- ======================================
-- 8. UserDevice（用户-设备关系表）
-- ======================================
CREATE TABLE IF NOT EXISTS `user_device` (
                                             `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                             `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                             `device_id` BIGINT NOT NULL COMMENT '设备ID',
                                             `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             UNIQUE KEY `uk_user_device` (`user_id`, `device_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_device_id` (`device_id`),
    CONSTRAINT `fk_user_device_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_device_device` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-设备关系表';

-- ======================================
-- 9. ScanLog（扫码日志表）
-- ======================================
CREATE TABLE IF NOT EXISTS `scan_log` (
                                          `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                          `user_id` BIGINT COMMENT '用户ID',
                                          `device_id` BIGINT NOT NULL COMMENT '设备ID',
                                          `merchant_id` BIGINT COMMENT '商户ID',
                                          `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          KEY `idx_user_id` (`user_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_scan_log_device_created` (`device_id`, `created_at`),
    CONSTRAINT `fk_scan_log_device` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_scan_log_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_scan_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='扫码日志表';

-- ======================================
-- 10. Coupon（卡券表）
-- ======================================
CREATE TABLE IF NOT EXISTS `coupon` (
                                        `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                        `merchant_id` INT COMMENT '商户ID',
                                        `title` VARCHAR(255) COMMENT '卡券标题',
    `type` VARCHAR(255) COMMENT '卡券类型: cash(现金)/discount(折扣)',
    `amount` DECIMAL(10, 2) COMMENT '金额/比例',
    `min_amount` DECIMAL(10, 2) COMMENT '最低消费金额',
    `total_count` INT COMMENT '总数',
    `remain_count` INT COMMENT '剩余数',
    `start_time` DATETIME COMMENT '有效期开始',
    `end_time` DATETIME COMMENT '有效期结束',
    `status` INT DEFAULT 1 COMMENT '状态：1启用/0停用',
    `link` VARCHAR(255) COMMENT '第三方平台跳转链接',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_merchant_id` (`merchant_id`),
    CONSTRAINT `fk_coupon_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卡券表';

-- ======================================
-- 11. UserCoupon（用户卡券关系表）
-- ======================================
CREATE TABLE IF NOT EXISTS `user_coupon` (
                                             `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                             `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                             `coupon_id` BIGINT NOT NULL COMMENT '卡券ID',
                                             `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
                                             `status` INT DEFAULT 1 COMMENT '状态：1=UNUSED, 2=USED, 3=EXPIRED',
                                             `used_at` DATETIME COMMENT '使用时间',
                                             `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                             UNIQUE KEY `uk_user_coupon` (`user_id`, `coupon_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_coupon_id` (`coupon_id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_status` (`status`),
    KEY `idx_user_coupon_status` (`user_id`, `status`),
    CONSTRAINT `fk_user_coupon_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_coupon_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_coupon_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户卡券关系表';

-- ======================================
-- 12. PromotionPlatform（推广平台表）
-- ======================================
CREATE TABLE IF NOT EXISTS `promotion_platform` (
                                                    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                                    `code` VARCHAR(255) COMMENT '平台代码：douyin/xiaohongshu等',
    `name` VARCHAR(255) COMMENT '平台名称',
    `icon` VARCHAR(255) COMMENT '平台图标URL',
    `color` VARCHAR(255) COMMENT '平台颜色(十六进制)',
    `description` VARCHAR(255) COMMENT '平台描述',
    `jump_mode` VARCHAR(255) COMMENT '跳转方式：scheme/webview/miniprogram/copy',
    `scheme_template` VARCHAR(255) COMMENT 'URL scheme模板',
    `web_url_template` VARCHAR(255) COMMENT 'H5链接模板',
    `miniprogram_appid` VARCHAR(255) COMMENT '小程序AppID',
    `miniprogram_path_template` VARCHAR(255) COMMENT '小程序路径模板',
    `required_params` VARCHAR(255) COMMENT '必填参数（JSON格式）',
    `optional_params` VARCHAR(255) COMMENT '可选参数（JSON格式）',
    `sort` INT COMMENT '排序号',
    `status` INT DEFAULT 1 COMMENT '状态：1启用/0停用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推广平台表';

-- ======================================
-- 13. MerchantPromotionConfig（商户推广配置表）
-- ======================================
CREATE TABLE IF NOT EXISTS `merchant_promotion_config` (
                                                           `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                                           `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
                                                           `type` VARCHAR(32) NOT NULL COMMENT '类型: platform或coupon',
    `platform_id` BIGINT COMMENT '平台ID',
    `coupon_id` BIGINT COMMENT '卡券ID',
    `params` JSON COMMENT 'JSON格式参数',
    `custom_name` VARCHAR(128) COMMENT '自定义名称',
    `custom_icon` VARCHAR(255) COMMENT '自定义图标',
    `sort` INT COMMENT '排序',
    `status` INT DEFAULT 1 COMMENT '状态：0禁用/1启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_merchant_config` (`merchant_id`, `type`, `platform_id`, `coupon_id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_type` (`type`),
    KEY `idx_platform_id` (`platform_id`),
    KEY `idx_coupon_id` (`coupon_id`),
    KEY `idx_status` (`status`),
    KEY `idx_merchant_config_status` (`merchant_id`, `status`),
    CONSTRAINT `fk_merchant_config_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_merchant_config_platform` FOREIGN KEY (`platform_id`) REFERENCES `promotion_platform` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_merchant_config_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户推广配置表';

-- ======================================
-- 14. PromotionClickLog（推广点击日志表）
-- ======================================
CREATE TABLE IF NOT EXISTS `promotion_click_log` (
                                                     `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                                     `user_id` BIGINT COMMENT '用户ID',
                                                     `platform_id` BIGINT NOT NULL COMMENT '平台ID',
                                                     `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
                                                     `device_id` BIGINT COMMENT '设备ID',
                                                     `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                     KEY `idx_user_id` (`user_id`),
    KEY `idx_platform_id` (`platform_id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_promotion_click_merchant_created` (`merchant_id`, `created_at`),
    CONSTRAINT `fk_promotion_click_platform` FOREIGN KEY (`platform_id`) REFERENCES `promotion_platform` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_promotion_click_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_promotion_click_device` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_promotion_click_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推广点击日志表';

-- ======================================
-- 15. Order（订单表）
-- ======================================
CREATE TABLE IF NOT EXISTS `order` (
                                       `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                       `order_no` VARCHAR(255) UNIQUE COMMENT '订单号（唯一）',
    -- 改为 UNSIGNED，与主表一致
    `merchant_id` INT UNSIGNED COMMENT '商户ID',
    `plan_id` INT UNSIGNED COMMENT '套餐ID',
    `amount` DECIMAL(10, 2) COMMENT '订单金额',
    `status` VARCHAR(255) DEFAULT 'pending' COMMENT '订单状态: pending/paid/expired',
    `expire_at` DATETIME COMMENT '订单过期时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_plan_id` (`plan_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_order_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_order_plan` FOREIGN KEY (`plan_id`) REFERENCES `plan` (`id`) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ======================================
-- 16. OrderRecord（订单记录表）
-- ======================================
CREATE TABLE IF NOT EXISTS `order_record` (
                                              `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                              `order_no` VARCHAR(255) UNIQUE COMMENT '订单号（唯一）',
    `merchant_id` INT COMMENT '商户ID',
    `plan_id` INT COMMENT '套餐ID',
    `amount` DECIMAL(10, 2) COMMENT '订单金额',
    `status` VARCHAR(255) COMMENT '订单状态：pending/paid/expired等',
    `expire_at` DATETIME COMMENT '订单过期时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_plan_id` (`plan_id`),
    CONSTRAINT `fk_order_record_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_order_record_plan` FOREIGN KEY (`plan_id`) REFERENCES `plan` (`id`) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单记录表';

-- ======================================
-- 17. Corpus（知识库内容表）
-- ======================================
CREATE TABLE IF NOT EXISTS `corpus` (
                                        `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                        `corpus_id` VARCHAR(255) COMMENT '内容ID（UUID）',
    `merchant_id` INT COMMENT '商户ID',
    `title` VARCHAR(255) COMMENT '标题',
    `content` LONGTEXT COMMENT '内容',
    `category` VARCHAR(255) COMMENT '分类',
    `tags` VARCHAR(255) COMMENT '标签（JSON数组）',
    `status` INT DEFAULT 1 COMMENT '状态：1正常/0删除',
    `image_url` VARCHAR(255) COMMENT '缩略图URL',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `created_by` VARCHAR(255) COMMENT '创建人',
    `updated_by` VARCHAR(255) COMMENT '更新人',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_category` (`category`),
    CONSTRAINT `fk_corpus_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库内容表';

-- ======================================
-- 18. CorpusCategory（语料库分类表）
-- ======================================
CREATE TABLE IF NOT EXISTS `corpus_category` (
                                                 `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                                 `name` VARCHAR(255) COMMENT '分类名称',
    `merchant_id` INT COMMENT '商户ID（null表示全局分类）',
    `sort_order` INT COMMENT '排序顺序（升序）',
    `description` VARCHAR(255) COMMENT '分类描述',
    `corpus_count` INT DEFAULT 0 COMMENT '该分类下的语料数量',
    `enabled` BOOLEAN DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_merchant_id` (`merchant_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='语料库分类表';

-- ======================================
-- 19. AiConfig（AI配置表）
-- ======================================
CREATE TABLE IF NOT EXISTS `ai_config` (
                                           `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                           `merchant_id` BIGINT COMMENT '商户ID（NULL表示全局配置）',
                                           `text_model` VARCHAR(255) COMMENT '文本生成模型（如gpt-3.5-turbo）',
    `image_model` VARCHAR(255) COMMENT '图片生成模型（如dall-e-3）',
    `video_model` VARCHAR(255) COMMENT '视频生成模型',
    `api_key` VARCHAR(255) COMMENT 'API Key（加密存储）',
    `api_secret` VARCHAR(255) COMMENT 'API Secret（加密存储）',
    `enabled` BOOLEAN DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_merchant_id` (`merchant_id`),
    CONSTRAINT `fk_ai_config_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI配置表';

-- ======================================
-- 20. AiGenerateRecord（AI生成记录表）
-- ======================================
CREATE TABLE IF NOT EXISTS `ai_generate_record` (
                                                    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                                    `record_id` VARCHAR(255) COMMENT '记录ID（UUID）',
    `merchant_id` INT COMMENT '商户ID（可选）',
    `type` VARCHAR(255) COMMENT '生成类型：text/image/video',
    `mode` VARCHAR(255) COMMENT '生成模式: new(全新创作)/secondary(二次创作)',
    `corpus_id` BIGINT COMMENT '关联语料库ID',
    `prompt` TEXT COMMENT '用户输入的提示词',
    `result` TEXT COMMENT '生成结果',
    `status` INT DEFAULT 1 COMMENT '状态：1成功/0失败',
    `token_usage` INT COMMENT 'Token使用数量（仅用于文本生成）',
    `cost` DOUBLE COMMENT '生成成本（美元）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_corpus_id` (`corpus_id`),
    KEY `idx_type` (`type`),
    CONSTRAINT `fk_ai_generate_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_ai_generate_corpus` FOREIGN KEY (`corpus_id`) REFERENCES `corpus` (`id`) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI生成记录表';

-- ======================================
-- 21. SystemSetting（系统设置表）
-- ======================================
CREATE TABLE IF NOT EXISTS `system_setting` (
                                                `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                                `key_name` VARCHAR(255) UNIQUE COMMENT '配置键名',
    `value` VARCHAR(255) COMMENT '配置值',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统设置表';

-- ======================================
-- 22. AuditLog（审计日志表）
-- ======================================
CREATE TABLE IF NOT EXISTS `audit_log` (
                                           `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                           `user_id` BIGINT COMMENT '用户ID（谁操作）',
                                           `username` VARCHAR(255) COMMENT '用户名（谁操作）',
    `event_type` VARCHAR(50) COMMENT '审计事件类型',
    `status` VARCHAR(50) COMMENT '操作结果状态',
    `description` VARCHAR(255) COMMENT '操作描述',
    `object_type` VARCHAR(100) COMMENT '操作对象类型（如：User、Role等）',
    `object_id` BIGINT COMMENT '操作对象ID',
    `before_data` LONGTEXT COMMENT '操作前的数据（JSON格式）',
    `after_data` LONGTEXT COMMENT '操作后的数据（JSON格式）',
    `ip_address` VARCHAR(50) COMMENT '客户端IP地址',
    `user_agent` VARCHAR(255) COMMENT '用户代理（User-Agent）',
    `request_id` VARCHAR(100) COMMENT '请求ID',
    `failure_reason` VARCHAR(255) COMMENT '失败原因',
    `duration_ms` BIGINT COMMENT '操作耗时（毫秒）',
    `affected_records` INT COMMENT '受影响的记录数',
    `created_time` DATETIME COMMENT '操作时间',
    `archived` BOOLEAN DEFAULT 0 COMMENT '是否已归档',
    `remarks` VARCHAR(255) COMMENT '备注',
    KEY `idx_user_id` (`user_id`),
    KEY `idx_event_type` (`event_type`),
    KEY `idx_object_type` (`object_type`),
    KEY `idx_created_time` (`created_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- ======================================
-- 23. Permission（权限表）
-- ======================================
CREATE TABLE IF NOT EXISTS `permissions` (
                                             `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                             `code` VARCHAR(100) NOT NULL UNIQUE COMMENT '权限代码(resource.action格式)',
    `resource` VARCHAR(50) NOT NULL COMMENT '资源名称',
    `action` VARCHAR(50) NOT NULL COMMENT '操作名称',
    `description` VARCHAR(200) COMMENT '描述',
    `category` VARCHAR(50) COMMENT '分类',
    `status` INT DEFAULT 1 COMMENT '状态：1=活跃/0=停用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_code` (`code`),
    KEY `idx_resource` (`resource`),
    KEY `idx_status` (`status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- ======================================
-- 24. Role（角色表）
-- ======================================
CREATE TABLE IF NOT EXISTS `roles` (
                                       `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
                                       `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色代码：super_admin/admin/merchant',
    `name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(255) COMMENT '角色描述',
    `is_system` BOOLEAN DEFAULT 0 COMMENT '是否是系统内置角色',
    `status` INT DEFAULT 1 COMMENT '状态：1=活跃/0=停用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_role_code` (`code`),
    KEY `idx_role_status` (`status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ======================================
-- 25. RolePermissions（角色-权限关联表）
-- ======================================
CREATE TABLE IF NOT EXISTS `role_permissions` (
                                                  `role_id` BIGINT NOT NULL COMMENT '角色ID',
                                                  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
                                                  PRIMARY KEY (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`),
    CONSTRAINT `fk_role_permissions_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_role_permissions_permission` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-权限关联表';

-- ======================================
-- 初始化内置角色数据
-- ======================================
INSERT IGNORE INTO `roles` (`code`, `name`, `description`, `is_system`, `status`) VALUES
('super_admin', '超级管理员', '拥有系统所有权限的超级管理员', 1, 1),
('admin', '系统管理员', '系统管理员，拥有除用户权限外的所有权限', 1, 1),
('merchant', '商户', '商户用户，只能管理自己的相关资源', 1, 1);

-- ======================================
-- 脚本执行完成
-- ======================================
COMMIT;
