# 晓居智能管理系统 - 功能说明文档

## 一、系统概述

**系统名称：** QuickTap 晓居智能管理系统

**技术架构：**

| 层级 | 技术栈 | 端口 |
|------|--------|------|
| 管理后台前端 | Vue 3 + Ant Design Vue + Vite | 5173/5174 |
| 后端服务 | Spring Boot + MyBatis + MySQL | 8222 |
| C端小程序 | uni-app (H5) | 5175 |
| 数据库 | MySQL 5.7 (远程) | 3306 |

**角色体系：** 系统预定义三种角色

| 角色 | 标识 | 权限范围 |
|------|------|---------|
| 超级管理员 | `super_admin` | 全部功能，含系统配置、用户管理、商家权限配置 |
| 管理员 | `admin` | 商家、设备、营销、AI 等业务模块 |
| 商家 | `merchant` | 自身商家信息、设备、AI创作、营销配置 |

---

## 二、功能模块详述

### 1. 登录认证

**页面：** `views/login/index.vue`

| 功能 | 说明 |
|------|------|
| 账号登录 | 用户名 + 密码，JWT Token 认证 |
| 路由守卫 | 未登录跳转登录页，已登录按角色控制页面访问权限 |
| Token 管理 | 存储于 localStorage，请求头自动携带 `Bearer` Token |
| 商家详情隔离 | 商家角色只能访问自己的商家详情页 |

**API 接口：**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/auth/login` | 登录 |
| GET | `/api/admin/user/info` | 获取当前用户信息 |
| POST | `/api/admin/auth/logout` | 登出 |
| PUT | `/api/admin/user/password` | 修改密码 |
| PUT | `/api/admin/user/info` | 更新个人信息 |

---

### 2. 仪表盘

**页面：** `views/dashboard/index.vue`

| 功能 | 说明 |
|------|------|
| 数据概览 | 商家总数、设备总数、扫码次数、AI 生成次数等统计卡片 |
| 趋势图表 | 扫码量 / AI 使用量趋势（按日/周/月） |
| TOP 商家 | 按收入/订单量排名的商家列表 |
| AI 使用统计 | 文本/图片/视频生成次数及占比 |

**API 接口：**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/statistics/overview` | 总览数据 |
| GET | `/api/admin/statistics/trend` | 趋势数据 |
| GET | `/api/admin/statistics/top/merchants` | TOP 商家排名 |
| GET | `/api/admin/statistics/ai-stats` | AI 使用统计 |
| GET | `/api/admin/statistics/merchant/{merchantId}` | 单商家统计 |

---

### 3. 商家管理

| 页面 | 路径 | 说明 |
|------|------|------|
| `views/merchant/list.vue` | `/merchant/list` | 商家分页列表，支持搜索、启用/禁用、审核 |
| `views/merchant/detail.vue` | `/merchant/detail/:id` | 编辑商家完整信息 |
| `views/merchant/quota.vue` | `/merchant/quota` | 管理员查看所有商家额度，重置月度配额 |

**商家信息字段：**

| 字段 | 说明 |
|------|------|
| name | 商户名称 |
| logo | 商户 Logo |
| contactName / contactPhone / contactEmail | 联系人信息 |
| address | 商户地址 |
| businessHours | 营业时间 |
| wifiName / wifiPassword | WiFi 信息 |
| bossWechat | 老板微信 |
| bannerImages | 横幅轮播图（JSON 数组） |
| shopImages | 店铺图片（JSON 数组） |
| description | 商户简介 |
| auditStatus | 审核状态：0待审核 / 1通过 / 2拒绝 |
| status | 启用状态：1启用 / 0停用 |
| planId | 套餐 ID |
| storageUsed / storageLimit | 存储使用量 / 限制 |

**API 接口：**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/merchant/list` | 商家列表（分页） |
| GET | `/api/merchant/{id}` | 商家详情 |
| POST | `/api/merchant` | 创建商家 |
| PUT | `/api/merchant/{id}` | 更新商家信息 |
| DELETE | `/api/merchant/{id}` | 删除商家（仅超管） |
| PUT | `/api/merchant/{id}/approve` | 审核通过 |
| PUT | `/api/merchant/{id}/reject` | 审核拒绝 |
| PUT | `/api/merchant/{id}/enable` | 启用 |
| PUT | `/api/merchant/{id}/disable` | 禁用 |
| GET | `/api/admin/merchant-quota/all` | 全部商家额度 |
| POST | `/api/admin/merchant-quota/{merchantId}/reset` | 重置额度 |

---

### 4. 设备管理

**页面：** `views/device/list.vue`

| 功能 | 说明 |
|------|------|
| 设备列表 | 按"设备组"展示（QR + NFC 成对），显示名称、systemCode、所属商家、绑定状态 |
| 新增设备 | 自动生成 systemCode，批量创建 QR + NFC 两个设备，共享同一名称和 systemCode |
| 设备 URL | QR: `{前缀}?code={systemCode}`，NFC: `{前缀}/{systemCode}` |
| 批量操作 | 批量启用 / 禁用 / 删除 |
| 二维码生成 | 调用后端生成二维码图片 |
| 绑定状态 | 展示设备是否已关联商家（C端注册时自动绑定） |

**设备组架构：** 同一名称 + systemCode 的 QR 和 NFC 设备归为一组，列表中以 `qrcode` 和 `nfc` 对象分别展示。

**API 接口：**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/device/list` | 设备列表（后端分组返回） |
| POST | `/api/device/batch` | 批量创建设备（QR + NFC） |
| PUT | `/api/device/{id}` | 更新设备 |
| DELETE | `/api/device/{id}` | 删除设备 |
| PUT | `/api/device/batch/enable` | 批量启用 |
| PUT | `/api/device/batch/disable` | 批量禁用 |
| DELETE | `/api/device/batch` | 批量删除 |
| POST | `/api/admin/qrcode/generate` | 生成二维码 |
| POST | `/api/admin/qrcode/batch` | 批量生成二维码 |
| GET | `/api/admin/qrcode/list` | 二维码历史记录 |

---

### 5. AI 创作

| 页面 | 路径 | 说明 |
|------|------|------|
| `views/ai/generate.vue` | `/ai/generate` | 文案/图片/视频 AI 生成，支持选择商家、输入 Prompt |
| `views/ai/corpus.vue` | `/ai/corpus` | 商家知识库语料的增删改查，支持分类、回收站、恢复 |
| `views/ai/config.vue` | `/ai/config` | 商家级 AI 配置（API Key、模型、温度等参数） |
| `views/ai/merchantConfig.vue` | `/ai/merchant-config` | 超管查看所有商家 AI 配置总览（仅超管） |

**AI 生成类型：** 文案(text)、图片(image)、视频(video)

**API 接口：**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/merchant/ai-generate/text` | 生成文案 |
| POST | `/api/merchant/ai-generate/image` | 生成图片 |
| POST | `/api/merchant/ai-generate/video` | 生成视频 |
| GET | `/api/merchant/ai-generate/history` | 生成历史 |
| GET | `/api/merchant/ai-config` | 获取商家 AI 配置 |
| PUT | `/api/merchant/ai-config` | 更新商家 AI 配置 |
| GET | `/api/admin/ai-config` | 获取全局 AI 配置（超管） |
| PUT | `/api/admin/ai-config` | 更新全局 AI 配置（超管） |
| GET | `/api/merchant/corpus` | 语料列表 |
| POST | `/api/merchant/corpus` | 创建语料 |
| PUT | `/api/merchant/corpus/{id}` | 更新语料 |
| DELETE | `/api/merchant/corpus/{id}` | 删除语料（软删入回收站） |
| POST | `/api/merchant/corpus/{id}/restore` | 恢复语料 |
| GET | `/api/merchant/corpus/categories` | 语料分类列表 |
| POST | `/api/merchant/corpus/categories` | 创建分类 |
| PUT | `/api/merchant/corpus/categories/{id}` | 更新分类 |
| DELETE | `/api/merchant/corpus/categories/{id}` | 删除分类 |

---

### 6. 营销管理

| 页面 | 路径 | 说明 |
|------|------|------|
| `views/marketing/platforms.vue` | `/marketing/platforms` | 超管配置全局推广平台（大众点评、美团、抖音等），含跳转模板和参数（仅超管） |
| `views/marketing/merchantPromotion.vue` | `/marketing/merchant-promotion` | 商家选择已开通的推广平台，配置个性化跳转链接和参数 |
| `views/marketing/promotionDetail.vue` | `/marketing/promotion-detail/:id` | 查看单个推广配置详情 |
| `views/marketing/coupons.vue` | `/marketing/coupons` | 优惠券 CRUD，支持金额/门槛/有效期/启用禁用 |
| `views/marketing/plans.vue` | `/marketing/plans` | 套餐方案管理（名称/价格/功能/AI额度/存储），支持推荐标记 |
| `views/marketing/orders.vue` | `/marketing/orders` | 订单列表，支持按商家/状态筛选 |

**API 接口：**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/promotion/platforms` | 推广平台列表 |
| POST | `/api/promotion/platforms` | 创建推广平台 |
| PUT | `/api/promotion/platforms/{id}` | 更新推广平台 |
| DELETE | `/api/promotion/platforms/{id}` | 删除推广平台 |
| GET | `/api/promotion/merchant-configs` | 商家推广配置列表 |
| POST | `/api/promotion/merchant-configs` | 创建商家推广配置 |
| PUT | `/api/promotion/merchant-configs/{id}` | 更新商家推广配置 |
| DELETE | `/api/promotion/merchant-configs/{id}` | 删除商家推广配置 |
| GET | `/api/coupon/list` | 优惠券列表 |
| POST | `/api/coupon` | 创建优惠券 |
| PUT | `/api/coupon/{id}` | 更新优惠券 |
| DELETE | `/api/coupon/{id}` | 删除优惠券 |
| PUT | `/api/coupon/{id}/enable` | 启用优惠券 |
| PUT | `/api/coupon/{id}/disable` | 禁用优惠券 |
| GET | `/api/plan/list` | 套餐列表 |
| POST | `/api/plan` | 创建套餐 |
| PUT | `/api/plan/{id}` | 更新套餐 |
| DELETE | `/api/plan/{id}` | 删除套餐 |
| PUT | `/api/plan/{id}/recommend` | 推荐套餐 |
| PUT | `/api/plan/{id}/unrecommend` | 取消推荐 |
| GET | `/api/order/list` | 订单列表 |

---

### 7. 系统设置

| 页面 | 路径 | 说明 |
|------|------|------|
| `views/system/settings.vue` | `/system/settings` | 全局参数：设备URL前缀、二维码URL前缀、提示语、加密密钥等 |
| `views/system/profile.vue` | `/system/profile` | 修改密码、更新个人信息 |
| `views/system/user.vue` | `/system/user` | 管理员/商家账号 CRUD，支持启用/禁用/重置密码，删除时级联删除商家 |
| `views/system/role.vue` | `/system/role` | 查看预定义角色及权限矩阵，为用户分配角色 |
| `views/system/merchantAccess.vue` | `/system/merchant-access` | 超管配置管理员可访问的商家范围（仅超管） |
| `views/system/myQuota.vue` | `/system/my-quota` | 商家查看自身额度使用情况（仅商家角色） |

**API 接口：**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/system/settings` | 读取系统配置 |
| PUT | `/api/admin/system/settings` | 更新系统配置 |
| GET | `/api/admin/system/admin-merchant-access/list` | 权限配置列表 |
| GET | `/api/admin/system/admin-merchant-access/{adminId}` | 管理员可访问的商家 |
| POST | `/api/admin/system/admin-merchant-access/{adminId}` | 更新权限 |
| GET | `/api/admin/list` | 用户列表 |
| POST | `/api/admin` | 创建用户 |
| PUT | `/api/admin/{id}` | 更新用户 |
| DELETE | `/api/admin/{id}` | 删除用户（级联删除商家） |
| PUT | `/api/admin/{id}/enable` | 启用用户 |
| PUT | `/api/admin/{id}/disable` | 禁用用户 |
| PUT | `/api/admin/{id}/reset-password` | 重置密码 |
| GET | `/api/admin/roles` | 角色列表 |
| GET | `/api/admin/roles/permissions` | 全部权限列表 |
| GET | `/api/admin/roles/matrix` | 权限矩阵 |
| POST | `/api/admin/roles/assign` | 分配角色 |
| GET | `/api/merchant/merchant-quota/usage` | 商家自身额度 |

---

### 8. C端小程序（miniapp）

| 页面 | 说明 |
|------|------|
| 首页 index | 扫码/NFC 进入，检查设备绑定状态，已绑定跳转商家详情，未绑定跳转注册 |
| 商家详情 detail | 展示轮播图、优惠券、一键工具栏（推广平台/加微信/连WiFi）、商家介绍 |
| 商家入驻 register | 填写商家信息 + 创建管理员账号 + 绑定设备 |
| 推广跳转 jump | 中转页，跳转到外部推广平台 |
| WiFi 连接 wifi | 展示 WiFi 信息，引导连接 |

**C端 API（前缀 `/api/miniapp`）：**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/merchant/check-bind?code=xxx` | 检查设备绑定状态 |
| GET | `/merchant/info/{merchantId}` | 获取商家完整信息 |
| GET | `/merchant/promotion?merchantId=xxx` | 获取推广平台配置 |
| GET | `/merchant/wifi?merchantId=xxx` | 获取 WiFi 信息 |
| POST | `/merchant/register` | 商家自助入驻（创建账号+商家+绑定设备） |

---

## 三、核心业务流程

### 1. 设备创建与绑定流程

```
管理员新增设备
    → 后端批量创建 QR + NFC（共享 systemCode）
    → 设备列表展示设备组（QR + NFC 信息）

用户扫码 / NFC 碰一碰
    → C端 check-bind 接口检查绑定状态
    → 未绑定 → 跳转注册页 → 填写信息 → 注册接口创建商家 + 绑定设备
    → 已绑定 → 跳转商家详情页 → 展示商家信息 + 推广工具
```

### 2. 权限控制流程

```
用户登录
    → JWT Token（含 role + userId）
    → 路由守卫检查 meta.roles / meta.superAdminOnly
    → 页面渲染 v-if 检查 hasPermission 控制按钮/菜单显隐
    → API 请求后端 @PreAuthorize 注解二次校验角色
```

### 3. 设备 URL 格式

| 类型 | URL 格式 | 说明 |
|------|---------|------|
| QR 二维码 | `{deviceUrl}?code={systemCode}` | 扫码进入 C端 |
| NFC 芯片 | `{qrcodeUrl}/{systemCode}` | 碰一碰进入 C端 |

### 4. 商家入驻流程

```
C端用户扫描设备
    → check-bind 返回 bound=false
    → 跳转注册页
    → 填写：商家名称、联系人、电话、地址、WiFi、微信等
    → 调用 /merchant/register
    → 后端创建 admin 账号 + merchant 记录 + 绑定设备(merchant_id)
    → 跳转商家详情页
```

---

## 四、状态管理

### Store 结构

| Store | 文件 | 职责 |
|-------|------|------|
| useUserStore | `store/user.js` | Token 存储、用户信息、权限列表、登录/登出、hasPermission 权限判断 |
| useAppStore | `store/app.js` | 侧边栏折叠状态、当前选中商家 |

### 权限判断

```javascript
// 支持前缀匹配，如 hasPermission('merchant') 可匹配 'merchant.list'、'merchant.create' 等
hasPermission(perm) {
  return permissions.some(p => p === perm || p.startsWith(perm + '.'))
}
```

---

## 五、前端基础设施

### 请求封装

`utils/request.js` 封装了 Axios 实例：

| 功能 | 说明 |
|------|------|
| 请求拦截 | 自动添加 `Authorization: Bearer {token}` 请求头 |
| 响应拦截 | 统一处理 ApiResponse 结构，code=0 时直接返回 data |
| 错误处理 | 401 跳转登录页，其他错误统一 message 提示 |
| Vite 代理 | `/api` → `http://localhost:8222` |

### 布局结构

`layouts/BasicLayout.vue`：

| 区域 | 说明 |
|------|------|
| 侧边栏 | 根据路由配置动态生成菜单，支持折叠，按角色/权限过滤显示 |
| 顶部栏 | 面包屑导航、用户下拉菜单（个人中心/修改密码/退出登录） |
| 内容区 | `<router-view>` 渲染页面内容 |
