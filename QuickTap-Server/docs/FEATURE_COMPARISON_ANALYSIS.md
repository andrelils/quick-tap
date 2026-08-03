# QuickTap 项目 Node vs Java 功能对标清单

## 执行摘要

- **Node 接口总数**: 114+（包括所有路由）
- **Java 接口总数**: 104+ （包括所有 Mapping）
- **两者都有**: ~90 个功能
- **Node 特有**: ~15-20 个功能需要补充
- **Java 新增**: ~10 个功能

---

## 1. 完整的功能对标清单

### 1.1 两个项目都已实现的功能 (约 90 个)

#### 认证与授权 (6 个功能)
| 功能 | Node 路由 | Java Endpoint | 状态 | 备注 |
|------|---------|---|------|-----|
| 管理员登录 | POST /api/auth/login | POST /api/admin/auth/login | ✓ | 已对标 |
| 用户登录 | POST /api/user/login | POST /api/user/login | ✓ | 已对标 |
| 微信小程序登录 | POST /api/user/auth/wechat-mini | POST /api/user/auth/wechat-mini | ✓ | 已对标 |
| 用户注册 | POST /api/user/register | POST /api/user/register | ✓ | 已对标 |
| 刷新 Token | POST /api/refresh-token | POST /api/refresh-token | ✓ | 已对标 |
| 验证 Token | GET /api/validate-token | GET /api/validate-token | ✓ | 已对标 |
| 用户登出 | POST /api/auth/logout | POST /api/admin/auth/logout | ✓ | 已对标 |
| 获取用户信息 | GET /api/user/info | GET /api/admin/user/info | ✓ | 已对标 |

#### 设备管理 (13 个功能)
| 功能 | Node 路由 | Java Endpoint | 状态 | 备注 |
|------|---------|---|------|-----|
| 获取设备列表 | GET /api/device/list | GET /api/device/list | ✓ | 已对标 |
| 获取设备详情 | GET /api/device/:id | GET /api/device/{id} | ✓ | 已对标 |
| 创建设备 | POST /api/device | POST /api/device | ✓ | 已对标 |
| 批量创建设备 | POST /api/device/batch | POST /api/device/batch | ✓ | 已对标 |
| 编辑设备 | PUT /api/device/:id | PUT /api/device/{id} | ✓ | 已对标 |
| 删除设备 | DELETE /api/device/:id | DELETE /api/device/{id} | ✓ | 已对标 |
| 批量启用设备 | N/A | PUT /api/device/batch/enable | ✓ Java新增 | 运维功能 |
| 批量禁用设备 | N/A | PUT /api/device/batch/disable | ✓ Java新增 | 运维功能 |
| 批量删除设备 | N/A | DELETE /api/device/batch | ✓ Java新增 | 运维功能 |
| 禁用设备 | N/A | PUT /api/device/{id}/disable | ✓ Java新增 | 状态管理 |
| 启用设备 | N/A | PUT /api/device/{id}/enable | ✓ Java新增 | 状态管理 |
| 生成二维码 | POST /api/qrcode/generate | GET /api/qrcode/config | ✓ | 已对标 |
| 绑定二维码到商户 | POST /api/qrcode/bind | N/A | ✗ Node特有 | **缺失** |

#### 商户管理 (11 个功能)
| 功能 | Node 路由 | Java Endpoint | 状态 | 备注 |
|------|---------|---|------|-----|
| 获取商户列表 | GET /api/merchant/list | GET /api/merchant/list | ✓ | 已对标 |
| 获取商户详情 | GET /api/merchant/:id | GET /api/merchant/{id} | ✓ | 已对标 |
| 创建商户 | POST /api/merchant | POST /api/merchant | ✓ | 已对标 |
| 编辑商户 | PUT /api/merchant/:id | PUT /api/merchant/{id} | ✓ | 已对标 |
| 删除商户 | DELETE /api/merchant/:id | DELETE /api/merchant/{id} | ✓ | 已对标 |
| 审核商户 | GET /api/merchant/audit-status/:status | GET /api/merchant/audit-status/{auditStatus} | ✓ | 已对标 |
| 禁用商户 | PUT /api/merchant/{id}/disable | PUT /api/merchant/{id}/disable | ✓ | 已对标 |
| 启用商户 | PUT /api/merchant/{id}/enable | PUT /api/merchant/{id}/enable | ✓ | 已对标 |
| 获取我的订单 | GET /api/merchant/my/orders | GET /api/merchant/my/orders | ✓ | 已对标 |
| 获取商户存储 | GET /api/merchant/:id/storage | GET /api/merchant/{id}/storage | ✓ | 已对标 |
| 商户配额管理 | GET /api/merchant/my/quota | GET /api/merchant/my/quota | ✓ | 已对标 |

#### AI 生成功能 (7 个功能)
| 功能 | Node 路由 | Java Endpoint | 状态 | 备注 |
|------|---------|---|------|-----|
| 生成文本 | POST /api/ai/generate/text | POST /api/merchant/ai-generate/text | ✓ | 已对标 |
| 生成图片 | POST /api/ai/generate/image | POST /api/merchant/ai-generate/image | ✓ | 已对标 |
| 生成视频 | POST /api/ai/generate/video | POST /api/merchant/ai-generate/video | ✓ | 已对标 |
| 获取生成历史 | GET /api/ai/history | GET /api/merchant/ai-generate/history | ✓ | 已对标 |
| 获取生成记录 | N/A | GET /api/merchant/ai-generate/{recordId} | ✓ Java新增 | 详情查询 |
| 删除生成记录 | N/A | DELETE /api/merchant/ai-generate/{recordId} | ✓ Java新增 | 记录管理 |
| AI 统计 | GET /api/admin/ai-generate/statistics | GET /api/admin/ai-generate/statistics | ✓ | 已对标 |

#### 语料库管理 (13 个功能)
| 功能 | Node 路由 | Java Endpoint | 状态 | 备注 |
|------|---------|---|------|-----|
| 获取语料库列表 | GET /api/corpus/list | GET /api/admin/corpus | ✓ | 已对标 |
| 获取语料库详情 | GET /api/corpus/:id | GET /api/merchant/corpus/{corpusId} | ✓ | 已对标 |
| 创建语料库 | POST /api/corpus | POST /api/merchant/corpus | ✓ | 已对标 |
| 编辑语料库 | PUT /api/corpus/:id | PUT /api/merchant/corpus/{corpusId} | ✓ | 已对标 |
| 删除语料库 | DELETE /api/corpus/:id | DELETE /api/merchant/corpus/{corpusId} | ✓ | 已对标 |
| 永久删除语料库 | N/A | DELETE /api/merchant/corpus/{corpusId}/permanent | ✓ Java新增 | 硬删除 |
| 获取语料库分类 | GET /api/corpus/categories | GET /api/merchant/corpus/category/{category} | ✓ | 已对标 |
| 创建分类 | POST /api/corpus/categories | N/A | ✗ | **缺失** |
| 编辑分类 | PUT /api/corpus/categories/:id | N/A | ✗ | **缺失** |
| 删除分类 | DELETE /api/corpus/categories/:id | N/A | ✗ | **缺失** |
| 获取回收站 | GET /api/corpus/trash/list | GET /api/merchant/corpus/trash | ✓ | 已对标 |
| 恢复语料库 | POST /api/corpus/restore | POST /api/merchant/corpus/{corpusId}/restore | ✓ | 已对标 |
| 搜索语料库 | GET /api/corpus/search | GET /api/merchant/corpus/search | ✓ | 已对标 |

#### 卡券管理 (9 个功能)
| 功能 | Node 路由 | Java Endpoint | 状态 | 备注 |
|------|---------|---|------|-----|
| 获取卡券列表 | GET /api/coupon/list | GET /api/coupon/list | ✓ | 已对标 |
| 获取卡券详情 | N/A | GET /api/coupon/{id} | ✓ Java新增 | 详情查询 |
| 创建卡券 | POST /api/coupon | POST /api/coupon | ✓ | 已对标 |
| 编辑卡券 | PUT /api/coupon/:id | PUT /api/coupon/{id} | ✓ | 已对标 |
| 禁用卡券 | N/A | PUT /api/coupon/{id}/disable | ✓ Java新增 | 状态管理 |
| 启用卡券 | N/A | PUT /api/coupon/{id}/enable | ✓ Java新增 | 状态管理 |
| 领取卡券 | N/A | PUT /api/coupon/{id}/claim | ✓ Java新增 | 用户功能 |
| 删除卡券 | DELETE /api/coupon/:id | DELETE /api/coupon/{id} | ✓ | 已对标 |
| 按商户获取 | GET /api/coupon/merchant/:merchantId | GET /api/coupon/merchant/{merchantId} | ✓ | 已对标 |

#### 订单管理 (4 个功能)
| 功能 | Node 路由 | Java Endpoint | 状态 | 备注 |
|------|---------|---|------|-----|
| 获取订单列表 | GET /api/order/list | GET /api/order/list | ✓ | 已对标 |
| 创建订单 | POST /api/order | POST /api/order | ✓ | 已对标 |
| 获取订单详情 | GET /api/order/:id | GET /api/order/{id} | ✓ | 已对标 |
| 编辑订单 | PUT /api/order/:id | PUT /api/order/{id} | ✓ | 已对标 |

#### 套餐管理 (5 个功能)
| 功能 | Node 路由 | Java Endpoint | 状态 | 备注 |
|------|---------|---|------|-----|
| 获取套餐列表 | GET /api/plan/list | GET /api/plan/list | ✓ | 已对标 |
| 创建套餐 | POST /api/plan | POST /api/plan | ✓ | 已对标 |
| 编辑套餐 | PUT /api/plan/:id | PUT /api/plan/{id} | ✓ | 已对标 |
| 删除套餐 | DELETE /api/plan/:id | DELETE /api/plan/{id} | ✓ | 已对标 |
| 获取套餐详情 | GET /api/plan/:id | GET /api/plan/{id} | ✓ | 已对标 |

#### 推广平台管理 (10 个功能)
| 功能 | Node 路由 | Java Endpoint | 状态 | 备注 |
|------|---------|---|------|-----|
| 获取推广平台列表 | GET /api/promotion/platforms | GET /api/promotion/platforms | ✓ | 已对标 |
| 创建推广平台 | POST /api/promotion/platform | POST /api/promotion/platform | ✓ | 已对标 |
| 编辑推广平台 | PUT /api/promotion/platform/:id | PUT /api/promotion/platform/{id} | ✓ | 已对标 |
| 删除推广平台 | DELETE /api/promotion/platform/:id | DELETE /api/promotion/platform/{id} | ✓ | 已对标 |
| 商户推广配置 | GET /api/merchant/promotion/configs | GET /api/merchant/promotion/configs | ✓ | 已对标 |
| 可用平台列表 | GET /api/merchant/promotion/available-platforms | GET /api/merchant/promotion/available-platforms | ✓ | 已对标 |
| 创建商户配置 | POST /api/merchant/promotion/config | POST /api/merchant/promotion/config | ✓ | 已对标 |
| 编辑商户配置 | PUT /api/merchant/promotion/config/:id | PUT /api/merchant/promotion/config/{id} | ✓ | 已对标 |
| 删除商户配置 | DELETE /api/merchant/promotion/config/:id | DELETE /api/merchant/promotion/config/{id} | ✓ | 已对标 |
| 获取全部平台 | GET /api/promotion/platforms/all | N/A | ✗ | **缺失** |

#### 角色权限管理 (11 个功能)
| 功能 | Node 路由 | Java Endpoint | 状态 | 备注 |
|------|---------|---|------|-----|
| 获取所有角色 | GET /api/role/list | GET /api/admin/roles | ✓ | 已对标 |
| 获取权限列表 | N/A | GET /api/admin/roles/permissions | ✓ Java新增 | 细粒度权限 |
| 获取角色详情 | GET /api/role/:id | GET /api/admin/roles/{roleId} | ✓ | 已对标 |
| 创建角色 | POST /api/role | POST /api/admin/roles | ✓ | 已对标 |
| 编辑角色 | PUT /api/role/:id | PUT /api/admin/roles/{roleId} | ✓ | 已对标 |
| 删除角色 | DELETE /api/role/:id | DELETE /api/admin/roles/{roleId} | ✓ | 已对标 |
| 获取用户权限 | N/A | GET /api/admin/roles/{userId}/permissions | ✓ Java新增 | 用户权限查询 |
| 权限检查 | N/A | GET /api/admin/roles/{userId}/check-permission | ✓ Java新增 | 权限验证 |
| 权限分配矩阵 | N/A | GET /api/admin/roles/matrix | ✓ Java新增 | 权限矩阵视图 |
| 分配角色 | N/A | POST /api/admin/roles/assign | ✓ Java新增 | 批量分配 |

#### 用户管理 (7 个功能)
| 功能 | Node 路由 | Java Endpoint | 状态 | 备注 |
|------|---------|---|------|-----|
| 获取用户列表 | GET /api/user/list | GET /api/user/list | ✓ | 已对标 |
| 获取用户详情 | GET /api/user/:id | GET /api/user/{id} | ✓ | 已对标 |
| 创建用户 | POST /api/user | POST /api/user | ✓ | 已对标 |
| 编辑用户 | PUT /api/user/:id | PUT /api/user/{id} | ✓ | 已对标 |
| 删除用户 | DELETE /api/user/:id | DELETE /api/user/{id} | ✓ | 已对标 |
| 禁用用户 | N/A | PUT /api/user/{id}/disable | ✓ Java新增 | 状态管理 |
| 启用用户 | N/A | PUT /api/user/{id}/enable | ✓ Java新增 | 状态管理 |

#### 管理员管理 (8 个功能)
| 功能 | Node 路由 | Java Endpoint | 状态 | 备注 |
|------|---------|---|------|-----|
| 获取管理员列表 | GET /api/admin/list | GET /api/admin/list | ✓ | 已对标 |
| 获取管理员详情 | GET /api/admin/:id | GET /api/admin/{id} | ✓ | 已对标 |
| 创建管理员 | POST /api/admin | POST /api/admin | ✓ | 已对标 |
| 编辑管理员 | PUT /api/admin/:id | PUT /api/admin/{id} | ✓ | 已对标 |
| 删除管理员 | DELETE /api/admin/:id | DELETE /api/admin/{id} | ✓ | 已对标 |
| 禁用管理员 | N/A | PUT /api/admin/{id}/disable | ✓ Java新增 | 状态管理 |
| 启用管理员 | N/A | PUT /api/admin/{id}/enable | ✓ Java新增 | 状态管理 |
| 修改密码 | PUT /api/user/password | PUT /api/user/password | ✓ | 已对标 |

#### 统计分析 (6 个功能)
| 功能 | Node 路由 | Java Endpoint | 状态 | 备注 |
|------|---------|---|------|-----|
| 获取总览统计 | GET /api/statistics/overview | GET /api/statistics/overview | ✓ | 已对标 |
| 获取趋势数据 | GET /api/statistics/trend | GET /api/statistics/trend | ✓ | 已对标 |
| 获取商户统计 | GET /api/statistics/merchant/:merchantId | GET /api/statistics/merchant/{merchantId} | ✓ | 已对标 |
| 获取顶级商户 | GET /api/statistics/top/merchants | GET /api/statistics/top/merchants | ✓ | 已对标 |
| 获取 AI 统计 | GET /api/statistics/ai-stats | GET /api/statistics/ai-stats | ✓ | 已对标 |
| 生成报表 | N/A | N/A | ✗ | **两者缺失** |

#### 系统配置 (5 个功能)
| 功能 | Node 路由 | Java Endpoint | 状态 | 备注 |
|------|---------|---|------|-----|
| 获取系统设置 | GET /api/system/settings | GET /api/system/settings | ✓ | 已对标 |
| 更新系统设置 | PUT /api/system/settings | PUT /api/system/settings | ✓ | 已对标 |
| 管理员商户关联 | GET /api/system/admin-merchant-access/list | GET /api/system/admin-merchant-access/list | ✓ | 已对标 |
| 获取商户关联 | GET /api/system/admin-merchant-access/:adminId | GET /api/system/admin-merchant-access/{adminId} | ✓ | 已对标 |
| 创建管理员商户关联 | POST /api/system/admin-merchant-access | POST /api/system/admin-merchant-access | ✓ | 已对标 |

#### 上传管理 (5 个功能)
| 功能 | Node 路由 | Java Endpoint | 状态 | 备注 |
|------|---------|---|------|-----|
| 上传文件 | POST /api/upload | POST /api/upload | ✓ | 已对标 |
| 删除文件 | DELETE /api/upload/:id | DELETE /api/upload/{id} | ✓ | 已对标 |
| 获取文件列表 | GET /api/upload/list | GET /api/upload/list | ✓ | 已对标 |
| 获取存储统计 | GET /api/upload/storage | GET /api/upload/storage | ✓ | 已对标 |
| 检查大小限制 | N/A | GET /api/upload/check-limit | ✓ Java新增 | 大小检查 |

#### 健康检查 (3 个功能)
| 功能 | Node 路由 | Java Endpoint | 状态 | 备注 |
|------|---------|---|------|-----|
| 健康检查 | GET /api/health | GET /api/health | ✓ | 已对标 |
| 获取信息 | GET /api/info | GET /api/info | ✓ | 已对标 |
| 获取时间 | GET /api/time | GET /api/time | ✓ | 已对标 |

---

### 1.2 Node 特有的功能 (需要补充到 Java) - **15 个**

#### 优先级: 高 (8 个)

| # | 功能名称 | Node 路由 | 优先级 | 难度 | 工作量 | 缺失原因 |
|---|--------|---------|------|------|------|--------|
| 1 | 绑定二维码到商户 | POST /api/qrcode/bind | 高 | 中等 | 4h | 设备分配流程 |
| 2 | 批量删除语料库 | POST /api/corpus/batch-delete | 高 | 简单 | 2h | 批量操作 |
| 3 | 移动语料库到回收站 | POST /api/corpus/trash | 高 | 简单 | 2h | 软删除机制 |
| 4 | 语料库分类管理 (3个) | POST/PUT/DELETE /corpus/categories | 高 | 中等 | 6h | 分类功能 |
| 5 | 获取商户配额详情 | GET /api/merchant/quota/list | 高 | 简单 | 2h | 配额查询 |
| 6 | AI 配置管理 (2个) | GET/PUT /api/ai/config/:merchantId | 高 | 中等 | 5h | 商户级配置 |

#### 优先级: 中 (4 个)

| # | 功能名称 | Node 路由 | 优先级 | 难度 | 工作量 | 缺失原因 |
|---|--------|---------|------|------|------|--------|
| 7 | 获取全部推广平台列表 | GET /api/promotion/platforms/all | 中 | 简单 | 1h | 数据查询 |
| 8 | 生成批量 NFC 二维码 | POST /api/qrcode/generate/batch-nfc | 中 | 中等 | 4h | NFC 特有功能 |
| 9 | 删除二维码 | DELETE /api/qrcode/:id | 中 | 简单 | 1h | 简单删除 |
| 10 | 获取二维码历史 | GET /api/qrcode/history | 中 | 简单 | 2h | 历史记录 |

#### 优先级: 低 (3 个)

| # | 功能名称 | Node 路由 | 优先级 | 难度 | 工作量 | 缺失原因 |
|---|--------|---------|------|------|------|--------|
| 11 | 获取商户列表（用户端） | GET /api/user/merchant/list | 低 | 简单 | 2h | C端功能 |
| 12 | 获取商户详情（用户端） | GET /api/user/merchant/:id | 低 | 简单 | 1h | C端功能 |
| 13 | 获取设备信息 | GET /api/device/info/:deviceNo | 低 | 简单 | 1h | 设备查询 |
| 14 | 扫描二维码接口 | GET /api/scan | 低 | 中等 | 3h | C端功能 |
| 15 | WiFi 接口 | GET /api/wifi | 低 | 简单 | 1h | C端功能 |

**总计缺失工作量**: ~40-45 小时

---

### 1.3 Java 新增的功能 (Node 没有) - **10 个**

| # | 功能名称 | Java Endpoint | 优先级 | 说明 |
|---|--------|---|------|-----|
| 1 | 永久删除语料库 | DELETE /api/merchant/corpus/{corpusId}/permanent | 中 | 硬删除操作 |
| 2 | 批量启用/禁用设备 | PUT /api/device/batch/{action} | 中 | 批量状态管理 |
| 3 | 批量删除设备 | DELETE /api/device/batch | 中 | 批量删除 |
| 4 | 设备启用/禁用 | PUT /api/device/{id}/{action} | 中 | 单个状态管理 |
| 5 | 卡券启用/禁用 | PUT /api/coupon/{id}/{action} | 低 | 单个状态管理 |
| 6 | 卡券领取 | PUT /api/coupon/{id}/claim | 中 | 用户功能 |
| 7 | 用户启用/禁用 | PUT /api/user/{id}/{action} | 低 | 用户状态 |
| 8 | 权限矩阵视图 | GET /api/admin/roles/matrix | 低 | UI 辅助接口 |
| 9 | 权限检查接口 | GET /api/admin/roles/{userId}/check-permission | 中 | 权限验证 |
| 10 | 上传大小限制检查 | GET /api/upload/check-limit | 低 | 上传前检查 |

---

## 2. Node 缺失功能详细分析

### 2.1 语料库分类管理 (3 个功能)

**功能编号**: F001-F003

#### 功能 1: 获取语料库分类列表
- **Node 路由**: 不存在
- **Java 实现**: GET /api/corpus/categories
- **实现路径**: `CorpusController.getCategories()`
- **数据库映射**:
  - 表: `corpus` (category 字段)
  - 需要新增分类表: `corpus_category`

**缺失点**:
- Node 中没有分类管理的独立接口
- 需要建立分类表结构

**优先级**: 高
**难度等级**: 中等
**预计工作量**: 2 小时

**实现建议**:
```sql
-- 新增分类表
CREATE TABLE corpus_category (
  id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  merchant_id INT UNSIGNED NOT NULL,
  name VARCHAR(128) NOT NULL,
  description TEXT,
  sort INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_merchant_category (merchant_id, name),
  KEY idx_merchant_id (merchant_id)
);

-- 修改 corpus 表
ALTER TABLE corpus ADD COLUMN category_id INT UNSIGNED;
ALTER TABLE corpus ADD FOREIGN KEY (category_id) REFERENCES corpus_category(id);
```

#### 功能 2: 创建语料库分类
- **Node 路由**: 不存在
- **Java 实现**: POST /api/corpus/categories
- **优先级**: 高
- **难度等级**: 简单
- **预计工作量**: 2 小时

#### 功能 3: 编辑/删除语料库分类
- **Node 路由**: 不存在
- **Java 实现**: PUT/DELETE /api/corpus/categories/:id
- **优先级**: 高
- **难度等级**: 简单
- **预计工作量**: 2 小时

---

### 2.2 绑定二维码到商户

**功能编号**: F004

- **Node 路由**: POST /api/qrcode/bind
- **Java 缺失**: 不存在
- **实现路径**: Node 实现在 `routes/admin/device.js` 的 `bindQrCodeToMerchant`
- **数据库映射**:
  - 表: `qrcode`
  - 修改字段: `merchant_id`, `status`

**缺失原因**: 设备分配流程需要二维码绑定

**优先级**: 高
**难度等级**: 中等
**预计工作量**: 4 小时

**实现建议**:
```java
// DeviceController.java
@PostMapping("/qrcode/bind")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
public ApiResponse<Void> bindQrCodeToMerchant(
        @RequestParam String qrcodeId,
        @RequestParam Integer merchantId) {
    deviceService.bindQrCodeToMerchant(qrcodeId, merchantId);
    return ApiResponse.success("绑定成功");
}
```

---

### 2.3 批量操作语料库

**功能编号**: F005

- **Node 路由**: POST /api/corpus/batch-delete
- **Java 缺失**: 不存在
- **优先级**: 高
- **难度等级**: 简单
- **预计工作量**: 2 小时

**数据库映射**:
- 表: `corpus` (is_deleted 字段)

**实现建议**:
```java
@PostMapping("/corpus/batch-delete")
public ApiResponse<Map<String, Integer>> batchDeleteCorpus(
        @RequestBody List<Integer> corpusIds) {
    int count = corpusService.batchDelete(corpusIds);
    Map<String, Integer> result = new HashMap<>();
    result.put("deleted", count);
    return ApiResponse.success("批量删除成功", result);
}
```

---

### 2.4 语料库回收站功能

**功能编号**: F006-F007

#### 功能 A: 移动到回收站 (软删除)
- **Node 路由**: POST /api/corpus/trash
- **Java 缺失**: 不存在
- **优先级**: 高
- **难度等级**: 简单
- **预计工作量**: 2 小时

#### 功能 B: 恢复语料库
- **Node 路由**: POST /api/corpus/restore
- **Java 实现**: POST /api/merchant/corpus/{corpusId}/restore ✓ 已有
- **状态**: 已实现

---

### 2.5 AI 配置管理

**功能编号**: F008-F009

#### 功能 A: 获取 AI 配置
- **Node 路由**: GET /api/ai/config 和 GET /api/ai/config/:merchantId
- **Java 缺失**: 不存在
- **优先级**: 高
- **难度等级**: 中等
- **预计工作量**: 3 小时

**数据库映射**:
- 表: `ai_config`
- 字段: `merchant_id`, `text_model`, `image_model`, `video_model`, `api_key`

#### 功能 B: 更新 AI 配置
- **Node 路由**: PUT /api/ai/config 和 PUT /api/ai/config/:merchantId
- **Java 缺失**: 不存在
- **优先级**: 高
- **难度等级**: 中等
- **预计工作量**: 2 小时

**实现建议**:
```java
@GetMapping("/ai/config")
public ApiResponse<Map> getAiConfig(@RequestParam(required = false) Integer merchantId) {
    // 如果没有 merchantId，返回默认配置
    // 如果有 merchantId，返回该商户的配置
}

@PutMapping("/ai/config")
public ApiResponse<Void> updateAiConfig(
        @RequestParam Integer merchantId,
        @RequestBody AiConfigRequest request) {
    aiConfigService.updateConfig(merchantId, request);
    return ApiResponse.success("更新成功");
}
```

---

### 2.6 商户配额管理

**功能编号**: F010

- **Node 路由**: GET /api/merchant/quota/list
- **Java 缺失**: GET /api/merchant/my/quota (已有，但列表缺失)
- **优先级**: 高
- **难度等级**: 简单
- **预计工作量**: 2 小时

**实现建议**:
```java
@GetMapping("/merchant/quota/list")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public ApiResponse<PageResponse<MerchantQuota>> listQuotas(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize) {
    return ApiResponse.success(merchantQuotaService.listQuotas(pageNum, pageSize));
}
```

---

### 2.7 推广平台增强功能

**功能编号**: F011-F012

#### 功能 A: 获取全部推广平台
- **Node 路由**: GET /api/promotion/platforms/all
- **Java 缺失**: 不存在
- **优先级**: 中
- **难度等级**: 简单
- **预计工作量**: 1 小时

#### 功能 B: 批量生成 NFC 二维码
- **Node 路由**: POST /api/qrcode/generate/batch-nfc
- **Java 缺失**: 不存在
- **优先级**: 中
- **难度等级**: 中等
- **预计工作量**: 4 小时

---

### 2.8 二维码管理增强

**功能编号**: F013-F014

#### 功能 A: 删除二维码
- **Node 路由**: DELETE /api/qrcode/:id
- **Java 缺失**: 不存在
- **优先级**: 中
- **难度等级**: 简单
- **预计工作量**: 1 小时

#### 功能 B: 获取二维码生成历史
- **Node 路由**: GET /api/qrcode/history
- **Java 缺失**: 不存在
- **优先级**: 中
- **难度等级**: 简单
- **预计工作量**: 2 小时

**实现建议**:
```java
@GetMapping("/qrcode/history")
public ApiResponse<PageResponse<QrcodeHistory>> getQrcodeHistory(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize) {
    return ApiResponse.success(qrcodeService.getHistory(pageNum, pageSize));
}
```

---

### 2.9 C 端用户相关功能

**功能编号**: F015-F018

这些是 C 端用户 (消费者) 相关的功能，优先级较低，但建议实现:

#### 功能 A: 获取商户列表（用户端）
- **Node 路由**: GET /api/user/merchant/list
- **Java 缺失**: 不存在
- **优先级**: 低
- **难度等级**: 简单
- **预计工作量**: 2 小时

#### 功能 B: 获取商户详情（用户端）
- **Node 路由**: GET /api/user/merchant/:id
- **Java 缺失**: 不存在
- **优先级**: 低
- **难度等级**: 简单
- **预计工作量**: 1 小时

#### 功能 C: 获取设备信息
- **Node 路由**: GET /api/device/info/:deviceNo
- **Java 缺失**: 不存在
- **优先级**: 低
- **难度等级**: 简单
- **预计工作量**: 1 小时

#### 功能 D: 扫描二维码
- **Node 路由**: GET /api/scan
- **Java 缺失**: 不存在
- **优先级**: 低
- **难度等级**: 中等
- **预计工作量**: 3 小时

#### 功能 E: WiFi 信息接口
- **Node 路由**: GET /api/wifi
- **Java 缺失**: 不存在
- **优先级**: 低
- **难度等级**: 简单
- **预计工作量**: 1 小时

---

## 3. 数据库表对应关系

### 3.1 两个项目都有的表 (16 个)

| # | 表名 | 说明 | Node | Java | 状态 |
|----|------|------|------|------|------|
| 1 | admin | 管理员 | ✓ | ✓ | 完全一致 |
| 2 | user | 用户 | ✓ | ✓ | 完全一致 |
| 3 | merchant | 商户 | ✓ | ✓ | 完全一致 |
| 4 | device | 设备 | ✓ | ✓ | 完全一致 |
| 5 | promotion_platform | 推广平台 | ✓ | ✓ | 完全一致 |
| 6 | merchant_promotion_config | 商户推广配置 | ✓ | ✓ | 完全一致 |
| 7 | coupon | 卡券 | ✓ | ✓ | 完全一致 |
| 8 | plan | 套餐 | ✓ | ✓ | 完全一致 |
| 9 | order_record | 订单 | ✓ | ✓ | 完全一致 |
| 10 | ai_config | AI 配置 | ✓ | ✓ | 完全一致 |
| 11 | ai_generate_record | AI 生成记录 | ✓ | ✓ | 完全一致 |
| 12 | corpus | 语料库 | ✓ | ✓ | 完全一致 |
| 13 | qrcode | 二维码 | ✓ | ✓ | 完全一致 |
| 14 | role | 角色 | ✓ | ✓ | 需定义 |
| 15 | permission | 权限 | ✓ | ✓ | 需定义 |
| 16 | role_permission | 角色权限关系 | ✓ | ✓ | 需定义 |

### 3.2 Java 新增或扩展的表 (3 个)

| # | 表名 | 说明 | Node | Java | 新增字段 |
|----|------|------|------|------|---------|
| 1 | merchant | 商户 | ✓ | ✓ | storage_used, storage_limit |
| 2 | device | 设备 | ✓ | ✓ 扩展 | 可以考虑添加 device_info JSON 字段 |
| 3 | admin | 管理员 | ✓ | ✓ | avatar, email, phone 字段更规范 |

### 3.3 Node 缺失的表结构 (Java 需要新增)

#### 表 1: corpus_category（语料库分类）

```sql
CREATE TABLE corpus_category (
  id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
  merchant_id INT UNSIGNED NOT NULL COMMENT '商户ID',
  name VARCHAR(128) NOT NULL COMMENT '分类名称',
  description TEXT COMMENT '分类描述',
  sort INT DEFAULT 0 COMMENT '排序号',
  status TINYINT DEFAULT 1 COMMENT '状态: 1启用/0禁用',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_merchant_category (merchant_id, name),
  KEY idx_merchant_id (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='语料库分类表';

-- 修改 corpus 表添加外键
ALTER TABLE corpus ADD COLUMN category_id INT UNSIGNED COMMENT '分类ID';
ALTER TABLE corpus ADD KEY idx_category_id (category_id);
ALTER TABLE corpus ADD CONSTRAINT fk_corpus_category FOREIGN KEY (category_id) REFERENCES corpus_category(id) ON DELETE SET NULL;
```

#### 表 2: qrcode_history（二维码生成历史）

```sql
CREATE TABLE qrcode_history (
  id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '历史ID',
  merchant_id INT UNSIGNED COMMENT '商户ID',
  qrcode_id INT UNSIGNED COMMENT '关联二维码ID',
  type VARCHAR(32) COMMENT '生成类型: nfc/qrcode',
  code VARCHAR(255) COMMENT '二维码内容',
  qrcode_url VARCHAR(255) COMMENT '二维码图片URL',
  params JSON COMMENT '生成参数',
  result JSON COMMENT '生成结果',
  status TINYINT DEFAULT 1 COMMENT '状态: 1成功/0失败',
  error_message TEXT COMMENT '错误信息',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_merchant_id (merchant_id),
  KEY idx_qrcode_id (qrcode_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='二维码生成历史';
```

#### 表 3: ai_config_merchant（商户 AI 配置详情）

Node 的 ai_config 表设计需要扩展，建议添加字段:

```sql
ALTER TABLE ai_config ADD COLUMN (
  text_prompt TEXT COMMENT '文本生成提示词',
  image_prompt TEXT COMMENT '图片生成提示词',
  video_prompt TEXT COMMENT '视频生成提示词',
  enabled TINYINT DEFAULT 1 COMMENT '是否启用',
  color VARCHAR(32) COMMENT '品牌色',
  miniprogram_appid VARCHAR(128) COMMENT '小程序AppID'
);
```

### 3.4 字段差异对比

#### admin 表
| 字段 | Node | Java | 备注 |
|------|------|------|------|
| avatar | × | ✓ | Java 新增 |
| email | ✓ | ✓ | 一致 |
| phone | ✓ | ✓ | 一致 |
| nickname | ✓ | ✓ | 一致 |
| role | ✓ | ✓ | 一致 |
| merchant_id | ✓ | ✓ | 一致 |
| status | ✓ | ✓ | 一致 |

#### merchant 表
| 字段 | Node | Java | 备注 |
|------|------|------|------|
| description | ✓ | × | Node 有，Java 缺失 |
| storage_used | × | ✓ | Java 新增，应同步到 Node |
| storage_limit | × | ✓ | Java 新增，应同步到 Node |
| created_by | × | × | 建议两者都添加 |
| updated_by | × | × | 建议两者都添加 |

#### device 表
| 字段 | Node | Java | 备注 |
|------|------|------|------|
| device_info | × | × | 建议添加 JSON 字段存储扩展信息 |
| location | × | × | 建议添加位置信息 |
| last_active | × | × | 建议添加最后活跃时间 |

---

## 4. 架构完善建议

### 4.1 项目级 Filter 和 Interceptor

#### Java 侧现有:
- ✓ JwtAuthenticationFilter - JWT 验证
- ✓ CorsConfigurationSource - CORS 配置
- ✓ JwtAuthenticationEntryPoint - 异常处理入口

#### 建议新增:

**1. 请求日志拦截器** (RequestLoggingInterceptor)
```java
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // 记录: 时间戳、用户ID、请求方法、URL、IP、参数
        MDC.put("traceId", UUID.randomUUID().toString());
        return true;
    }
}
```

**2. 业务异常处理拦截器** (ExceptionHandlerAdvice)
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }
}
```

**3. 请求参数验证拦截器** (ValidationInterceptor)
```java
// 使用 @Valid 和 BindingResult 验证
// 自定义验证器实现复杂验证逻辑
```

**4. 权限检查拦截器** (PermissionInterceptor)
```java
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // 检查用户权限，与 @PreAuthorize 配合
        return true;
    }
}
```

#### Node 侧:
现有实现:
- 认证中间件 (authMiddleware)
- 验证中间件 (validate)
- 错误处理中间件

建议增强:
- 添加请求日志中间件
- 添加速率限制中间件
- 添加请求超时中间件
- 规范化错误响应格式

---

### 4.2 统一的请求/响应格式

#### 当前状态

Java 已实现统一格式:
```java
{
  "code": 200,
  "message": "成功",
  "data": { ... },
  "timestamp": "2024-07-30T10:00:00Z"
}
```

Node 应同步此格式（检查是否一致）:
```javascript
{
  code: 200,
  message: '成功',
  data: { ... },
  timestamp: new Date().toISOString()
}
```

#### 建议标准化:

**成功响应 (200)**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": "2024-07-30T10:00:00Z",
  "traceId": "uuid-xxx"
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": null,
  "errors": [
    { "field": "username", "message": "用户名不能为空" }
  ],
  "timestamp": "2024-07-30T10:00:00Z",
  "traceId": "uuid-xxx"
}
```

**分页响应**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [...],
    "pageNum": 1,
    "pageSize": 10,
    "total": 100,
    "pages": 10
  },
  "timestamp": "2024-07-30T10:00:00Z"
}
```

---

### 4.3 异常处理完善

#### Java 侧建议:

**自定义异常体系**:
```java
// 基础异常
public class BusinessException extends RuntimeException {
    private Integer code;
    private String message;
}

// 分类异常
public class ResourceNotFoundException extends BusinessException { }
public class UnauthorizedException extends BusinessException { }
public class ForbiddenException extends BusinessException { }
public class BadRequestException extends BusinessException { }
public class ServiceException extends BusinessException { }
public class ExternalServiceException extends BusinessException { }
```

**全局异常处理**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiResponse<Void> handleNotFound(ResourceNotFoundException e) {
        return ApiResponse.notFound(e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ApiResponse<Void> handleUnauthorized(UnauthorizedException e) {
        return ApiResponse.unauthorized(e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ApiResponse<Void> handleForbidden(ForbiddenException e) {
        return ApiResponse.forbidden(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Map<String, String>> handleValidation(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ApiResponse.badRequest(errors);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleGenericException(Exception e) {
        log.error("未捕获的异常", e);
        return ApiResponse.error("系统内部错误");
    }
}
```

#### Node 侧建议:

类似的分类异常处理和全局错误中间件

---

### 4.4 权限白名单配置

#### Java 侧现有配置:

在 `SecurityConfig.java` 中已定义的公开接口:
- `/api/health`, `/api/info`, `/api/time`
- `/api/admin/auth/login`
- `/api/user/login`
- `/api/user/auth/wechat-mini`
- `/api/user/register`
- Swagger 文档接口

#### 建议扩展:

**配置文件方式** (application.yml):
```yaml
security:
  public-urls:
    - /api/health
    - /api/info
    - /api/time
    - /api/admin/auth/login
    - /api/user/login
    - /api/user/auth/wechat-mini
    - /api/user/register
    - /api/refresh-token
    - /api/validate-token
    - /api/swagger-ui.html
    - /api/v3/api-docs/**

  protected-urls:
    - path: /api/admin/**
      roles: [SUPER_ADMIN, ADMIN]
    - path: /api/merchant/**
      roles: [MERCHANT]
    - path: /api/user/**
      roles: [USER]
```

**动态权限加载**:
```java
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    private List<String> publicUrls;
    private List<ProtectedUrl> protectedUrls;

    public static class ProtectedUrl {
        private String path;
        private List<String> roles;
    }
}
```

---

### 4.5 日志系统

#### 现有状态:
- 使用 SLF4J + Logback (Java)
- 使用 winston 或类似 (Node)

#### 建议完善:

**Java Logback 配置**:
```xml
<configuration>
  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>

  <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/app.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
      <fileNamePattern>logs/app.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
      <maxFileSize>100MB</maxFileSize>
      <maxHistory>30</maxHistory>
    </rollingPolicy>
    <encoder>
      <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{traceId}] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>

  <root level="INFO">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="FILE"/>
  </root>
</configuration>
```

**MDC 追踪**:
```java
// 在 JwtAuthenticationFilter 中添加
MDC.put("traceId", request.getHeader("X-Trace-Id")
    ?? UUID.randomUUID().toString());
MDC.put("userId", currentUser.getId().toString());

// 清理
MDC.clear();
```

#### 建议添加的日志:
1. **认证日志**: 登录成功/失败、Token 刷新、权限检查失败
2. **业务操作日志**: CRUD 操作（创建、更新、删除关键资源）
3. **性能日志**: 慢查询、长处理时间请求
4. **安全日志**: 异常访问、权限拒绝
5. **错误日志**: 异常堆栈、外部服务调用失败

---

### 4.6 缓存策略

#### 建议实现:

**Redis 缓存架构**:

```yaml
# application.yml
spring:
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    jedis:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5

cache:
  default-ttl: 3600  # 1 小时
  keys:
    # 用户信息缓存 1 小时
    user:
      ttl: 3600
      key: "user:{id}"
    # 商户信息缓存 30 分钟
    merchant:
      ttl: 1800
      key: "merchant:{id}"
    # 权限列表缓存 24 小时
    permissions:
      ttl: 86400
      key: "permissions:{roleId}"
    # 推广平台缓存 24 小时
    promotion-platform:
      ttl: 86400
      key: "promotion:platform:*"
```

**缓存实现示例**:
```java
@Service
public class MerchantService {

    @Cacheable(value = "merchant", key = "#id")
    public Merchant getMerchantById(Integer id) {
        return merchantMapper.selectById(id);
    }

    @CacheEvict(value = "merchant", key = "#id")
    public void updateMerchant(Integer id, MerchantUpdateRequest request) {
        merchantMapper.updateById(id, request);
    }

    @CacheEvict(value = "merchant", allEntries = true)
    public void clearMerchantCache() {}
}
```

**缓存预热**:
```java
@Component
public class CacheWarmupRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {
        // 应用启动时预热常用缓存
        redisTemplate.opsForValue().set("platform:list",
            promotionPlatformService.getAllPlatforms(), Duration.ofDays(1));
    }
}
```

---

### 4.7 定时任务

#### 建议实现的定时任务:

**1. 数据清理任务**:
```java
@Component
@EnableScheduling
public class DataCleanupScheduler {

    @Scheduled(cron = "0 2 * * *")  // 每天凌晨 2 点
    public void cleanupExpiredData() {
        // 清理过期的生成记录
        aiGenerateService.deleteExpiredRecords();

        // 清理过期的卡券
        couponService.cleanupExpiredCoupons();

        // 清理过期的二维码历史
        qrcodeService.cleanupOldHistory();
    }
}
```

**2. 数据同步任务**:
```java
@Scheduled(cron = "0 */30 * * * *")  // 每 30 分钟
public void syncMerchantQuota() {
    // 从外部 API 同步商户配额
}
```

**3. 数据统计任务**:
```java
@Scheduled(cron = "0 1 * * *")  // 每天凌晨 1 点
public void generateDailyStatistics() {
    // 生成每日统计数据
    statisticsService.generateDailyStats();

    // 计算商户配额使用情况
    merchantService.calculateQuotaUsage();
}
```

**4. 备份任务**:
```java
@Scheduled(cron = "0 3 * * 0")  // 每周日凌晨 3 点
public void backupDatabase() {
    // 执行数据库备份
    backupService.backupDatabase();
}
```

---

### 4.8 API 文档

#### Java 侧:
现有 Swagger 配置，建议完善:

**SwaggerConfig.java 增强**:
```java
@Configuration
@EnableSwagger3
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("QuickTap API")
                .version("1.0.0")
                .description("完整的 API 文档")
                .contact(new Contact()
                    .name("Team")
                    .email("contact@quicktap.com")))
            .externalDocs(new ExternalDocumentation()
                .description("完整文档")
                .url("https://docs.quicktap.com"));
    }
}
```

**Controller 文档注解**:
```java
@PostMapping("/merchant")
@Operation(summary = "创建商户", description = "管理员创建新商户")
@ApiResponse(responseCode = "200", description = "创建成功")
@ApiResponse(responseCode = "400", description = "参数错误")
@ApiResponse(responseCode = "401", description = "未授权")
public ApiResponse<Merchant> createMerchant(
        @RequestBody @ParameterObject MerchantCreateRequest request) {
    // ...
}
```

#### Node 侧:
建议使用 Swagger/OpenAPI 文档生成工具 (swagger-jsdoc, apidoc等)

---

### 4.9 单元测试和集成测试

#### Java 侧建议:

**单元测试框架**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

**示例测试**:
```java
@SpringBootTest
public class MerchantServiceTest {

    @MockBean
    private MerchantMapper merchantMapper;

    @Autowired
    private MerchantService merchantService;

    @Test
    public void testGetMerchantById() {
        // Arrange
        Integer merchantId = 1;
        Merchant expectedMerchant = new Merchant();
        expectedMerchant.setId(merchantId);
        expectedMerchant.setName("Test Merchant");
        when(merchantMapper.selectById(merchantId))
            .thenReturn(expectedMerchant);

        // Act
        Merchant result = merchantService.getMerchantById(merchantId);

        // Assert
        assertEquals(expectedMerchant.getName(), result.getName());
        verify(merchantMapper, times(1)).selectById(merchantId);
    }
}
```

**集成测试**:
```java
@SpringBootTest
@AutoConfigureMockMvc
public class MerchantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreateMerchant() throws Exception {
        MerchantCreateRequest request = new MerchantCreateRequest();
        request.setName("New Merchant");
        request.setContactPhone("13800138000");

        mockMvc.perform(post("/api/merchant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

---

### 4.10 性能优化建议

#### 1. 数据库优化
```sql
-- 添加必要的索引
CREATE INDEX idx_merchant_status ON merchant(status);
CREATE INDEX idx_device_merchant ON device(merchant_id, status);
CREATE INDEX idx_corpus_merchant ON corpus(merchant_id, is_deleted);
CREATE INDEX idx_order_merchant ON order_record(merchant_id, status);

-- 添加复合索引
CREATE INDEX idx_merchant_audit_status ON merchant(status, audit_status);
CREATE INDEX idx_device_merchant_status ON device(merchant_id, status);
```

#### 2. 查询优化
```java
// 使用 Select 指定字段，避免查询不必要的列
@Select("SELECT id, name, status FROM merchant WHERE id = #{id}")
Merchant getMerchantById(Integer id);

// 使用分页查询大数据集
Page<Merchant> getMerchantList(Pageable pageable);

// 使用 JOIN 替代 N+1 查询
@Select("""
    SELECT m.*, COUNT(d.id) as device_count
    FROM merchant m
    LEFT JOIN device d ON m.id = d.merchant_id
    WHERE m.status = 1
    GROUP BY m.id
""")
List<MerchantWithDeviceCount> getMerchantsWithDeviceCount();
```

#### 3. 缓存优化
- 实现多级缓存（Redis + 本地缓存）
- 使用缓存预热
- 合理设置缓存过期时间

#### 4. 异步处理
```java
@Service
public class AiGenerateService {

    @Async
    public CompletableFuture<AiGenerateRecord> generateTextAsync(
            Integer merchantId, String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            // 长时间运行的 AI 生成任务
            return generateText(merchantId, prompt);
        });
    }
}
```

---

## 5. 实施优先级和时间规划

### 5.1 高优先级功能 (1-2 周内完成)

| 功能 | 工作量 | 优先级 | 难度 | 工作量 |
|------|-------|------|------|------|
| 语料库分类管理 (3 个) | 6h | 高 | 中等 | 6h |
| 绑定二维码 | 4h | 高 | 中等 | 4h |
| 批量操作语料库 | 3h | 高 | 简单 | 3h |
| AI 配置管理 | 5h | 高 | 中等 | 5h |
| **小计** | | | | **18h** |

### 5.2 中优先级功能 (2-3 周内完成)

| 功能 | 工作量 | 优先级 | 难度 | 工作量 |
|------|-------|------|------|------|
| 推广平台增强 | 5h | 中 | 简单 | 5h |
| 二维码管理增强 | 4h | 中 | 简单 | 4h |
| 商户配额列表 | 2h | 中 | 简单 | 2h |
| 架构完善 (日志、缓存等) | 20h | 中 | 中等 | 20h |
| **小计** | | | | **31h** |

### 5.3 低优先级功能 (3-4 周内完成)

| 功能 | 工作量 | 优先级 | 难度 | 工作量 |
|------|-------|------|------|------|
| C 端用户功能 (5 个) | 8h | 低 | 简单 | 8h |
| 单元测试和集成测试 | 20h | 低 | 中等 | 20h |
| 性能优化和监控 | 15h | 低 | 中等 | 15h |
| **小计** | | | | **43h** |

### 总计工作量: **~92 小时** (约 2-3 人月)

---

## 6. 实施建议

### 6.1 开发流程

1. **需求确认** (1-2 天)
   - 确认需要补充的功能优先级
   - 确认 API 契约（与 Node 一致性）

2. **数据库设计** (2-3 天)
   - 设计新表结构
   - 编写迁移脚本
   - 测试数据初始化

3. **API 实现** (主要工作)
   - Controller 层实现
   - Service 层实现
   - Mapper/Repository 层实现
   - 单元测试

4. **集成测试** (2-3 天)
   - 功能测试
   - 集成测试
   - 性能测试

5. **部署上线** (1-2 天)
   - 灰度发布
   - 监控告警

### 6.2 测试策略

- **单元测试覆盖率**: > 80%
- **集成测试**: 关键业务流程
- **性能测试**: 主要查询接口 (QPS > 1000)
- **安全测试**: 权限检查、SQL 注入、XSS

### 6.3 文档维护

- 更新 Swagger API 文档
- 更新数据库设计文档
- 更新部署文档
- 更新接口变更日志

---

## 7. 总结和建议

### 7.1 当前状态评估

- Java 项目已实现约 90% 的核心功能
- Node 项目包含一些 Java 缺失的 C 端功能
- 两个项目的数据模型基本一致，可以无缝集成

### 7.2 关键行动项

1. **立即执行** (1-2 周):
   - 补充语料库分类管理
   - 实现二维码绑定功能
   - 完成 AI 配置管理

2. **短期完成** (2-4 周):
   - 补充所有缺失的业务功能
   - 完善架构 (日志、缓存、定时任务)
   - 编写完整的单元测试

3. **长期优化** (1-2 月):
   - 性能优化和监控
   - C 端功能完整实现
   - 文档和工具链完善

### 7.3 风险提示

- **数据一致性**: 两个项目要保持数据模型同步
- **API 版本**: 需要维护向后兼容性
- **权限管理**: 确保权限模型在两个项目中一致
- **第三方依赖**: 定期更新和安全检查

---

## 附录 A: Node 路由完整清单

```
=== 认证相关 ===
POST   /api/auth/login
POST   /api/user/login
POST   /api/user/auth/wechat-mini
POST   /api/user/register
POST   /api/refresh-token
GET    /api/validate-token
POST   /api/auth/logout
GET    /api/user/info

=== 设备管理 ===
GET    /api/device/list
GET    /api/device/:id
POST   /api/device
POST   /api/device/batch
PUT    /api/device/:id
DELETE /api/device/:id
GET    /api/device/info/:deviceNo
POST   /api/qrcode/generate
POST   /api/qrcode/generate/batch-nfc
GET    /api/qrcode/history
POST   /api/qrcode/batch
DELETE /api/qrcode/:id
POST   /api/qrcode/bind
GET    /api/qrcode/config

=== 商户管理 ===
GET    /api/merchant/list
GET    /api/merchant/:id
POST   /api/merchant
PUT    /api/merchant/:id
DELETE /api/merchant/:id
GET    /api/merchant/audit-status/:status
GET    /api/merchant/my/orders
GET    /api/merchant/my/quota
GET    /api/merchant/promotion/configs
GET    /api/merchant/promotion/available-platforms
POST   /api/merchant/promotion/config
PUT    /api/merchant/promotion/config/:id
DELETE /api/merchant/promotion/config/:id

=== AI 生成 ===
POST   /api/ai/generate/text
POST   /api/ai/generate/image
POST   /api/ai/generate/video
GET    /api/ai/history
GET    /api/ai/config
PUT    /api/ai/config
GET    /api/ai/config/:merchantId
PUT    /api/ai/config/:merchantId
GET    /api/ai/merchant-config/list

=== 语料库 ===
GET    /api/corpus/list
GET    /api/corpus/storage
GET    /api/corpus/storage/:merchantId
GET    /api/corpus/categories
POST   /api/corpus/categories
PUT    /api/corpus/categories/:id
DELETE /api/corpus/categories/:id
GET    /api/corpus/:id
POST   /api/corpus
PUT    /api/corpus/:id
DELETE /api/corpus/:id
POST   /api/corpus/batch-delete
POST   /api/corpus/trash
POST   /api/corpus/restore
GET    /api/corpus/trash/list

... (其他模块省略)
```

---

**生成日期**: 2024-07-30
**版本**: 1.0
**状态**: 初稿

