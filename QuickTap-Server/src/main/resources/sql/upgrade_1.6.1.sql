-- =====================================================
-- 升级脚本 v1.6.1
-- 功能：修复图片 URL 缺少 context-path 前缀的问题
-- 原因：UploadController 之前返回的 URL 格式为 /uploads/xxx，
--       但服务端配置了 server.servlet.context-path=/api，
--       导致 Tomcat 无法识别路径，图片无法访问。
--       新格式应为 /api/uploads/xxx
-- 日期：2026-08-03
-- =====================================================

-- 1. admin 表的 avatar 字段
UPDATE admin 
SET avatar = CONCAT('/api', avatar) 
WHERE avatar IS NOT NULL 
  AND avatar != '' 
  AND avatar LIKE '/uploads%' 
  AND avatar NOT LIKE '/api/uploads%';

-- 2. merchant 表的 logo 字段
UPDATE merchant 
SET logo = CONCAT('/api', logo) 
WHERE logo IS NOT NULL 
  AND logo != '' 
  AND logo LIKE '/uploads%' 
  AND logo NOT LIKE '/api/uploads%';

-- 3. merchant 表的 banner_images 字段（JSON 数组中的 URL）
--    JSON 数组格式: ["uploads/images/xxx.jpg", ...]
UPDATE merchant 
SET banner_images = REPLACE(banner_images, '"/uploads/', '"/api/uploads/')
WHERE banner_images IS NOT NULL 
  AND banner_images != '' 
  AND banner_images LIKE '%/uploads%';

-- 4. merchant 表的 shop_photos 字段（JSON 数组中的 URL）
UPDATE merchant 
SET shop_photos = REPLACE(shop_photos, '"/uploads/', '"/api/uploads/')
WHERE shop_photos IS NOT NULL 
  AND shop_photos != '' 
  AND shop_photos LIKE '%/uploads%';

-- 5. ai_generate_record 表的 result_content 字段（文本中包含的 URL）
UPDATE ai_generate_record 
SET result_content = REPLACE(result_content, '/uploads/', '/api/uploads/')
WHERE result_content IS NOT NULL 
  AND result_content != '' 
  AND result_content LIKE '%/uploads%';

-- 6. ai_generate_record 表的 input_content 字段
UPDATE ai_generate_record 
SET input_content = REPLACE(input_content, '/uploads/', '/api/uploads/')
WHERE input_content IS NOT NULL 
  AND input_content != '' 
  AND input_content LIKE '%/uploads%';

-- 7. coupon 表的 image 字段
UPDATE coupon 
SET image = CONCAT('/api', image) 
WHERE image IS NOT NULL 
  AND image != '' 
  AND image LIKE '/uploads%' 
  AND image NOT LIKE '/api/uploads%';

-- 8. promotion_platform 表的 icon 字段
UPDATE promotion_platform 
SET icon = CONCAT('/api', icon) 
WHERE icon IS NOT NULL 
  AND icon != '' 
  AND icon LIKE '/uploads%' 
  AND icon NOT LIKE '/api/uploads%';
