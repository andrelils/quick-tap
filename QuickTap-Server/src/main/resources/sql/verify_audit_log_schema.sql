-- ============================================
-- 审计日志表结构验证脚本
-- 用于检查所有必需的列是否存在
-- ============================================

-- 查看当前 audit_log 表的完整结构
DESCRIBE audit_log;

-- 详细的列信息查询（显示列名、数据类型、是否为NULL、注释等）
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_KEY,
    EXTRA,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'audit_log'
ORDER BY ORDINAL_POSITION;

-- 查看表的索引信息
SHOW INDEX FROM audit_log;

-- ============================================
-- 以下是应该存在的所有列（共18个）：
-- ============================================
-- 1. id (主键，自增)
-- 2. user_id (操作用户ID)
-- 3. username (操作用户名) ← 之前缺失
-- 4. operation (原始操作字段，可能需要保留兼容性)
-- 5. event_type (审计事件类型：LOGIN/LOGOUT/USER_CREATE等) ← 之前缺失
-- 6. status (操作结果状态：SUCCESS/FAILURE/DENIED/WARNING) ← 之前缺失
-- 7. description (操作详描述) ← 关键缺失！导致logout失败
-- 8. resource_type (原始资源类型字段，可能需要保留兼容性)
-- 9. object_type (操作对象类型：User/Role/Document等) ← 之前缺失
-- 10. object_id (操作对象ID) ← 之前缺失
-- 11. resource_id (原始资源ID字段，可能需要保留兼容性)
-- 12. change_detail (原始变更详情字段，可能需要保留兼容性)
-- 13. before_data (操作前的数据JSON) ← 之前缺失
-- 14. after_data (操作后的数据JSON) ← 之前缺失
-- 15. ip_address (客户端IP地址)
-- 16. user_agent (用户代理User-Agent) ← 之前缺失
-- 17. request_id (请求ID) ← 之前缺失
-- 18. failure_reason (失败原因) ← 之前缺失
-- 19. duration_ms (操作耗时毫秒) ← 之前缺失
-- 20. affected_records (受影响的记录数) ← 之前缺失
-- 21. created_at (操作时间，原始字段)
-- 22. created_time (创建时间，MyBatis映射用) ← 之前缺失
-- 23. archived (是否已归档) ← 之前缺失
-- 24. remarks (备注信息) ← 之前缺失
-- 25. merchant_id (商户ID，原始字段，可能需要保留兼容性)
-- ============================================
