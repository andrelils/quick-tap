-- =====================================================
-- 升级脚本 v1.6.0
-- 功能：admin 表添加 avatar（头像URL）字段
-- 日期：2026-08-03
-- =====================================================

-- admin 表添加 avatar 列
ALTER TABLE `admin` ADD COLUMN IF NOT EXISTS `avatar` VARCHAR(255) COMMENT '头像URL' AFTER `nickname`;
