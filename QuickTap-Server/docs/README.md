# QuickTap Server

Java 版本 QuickTap 后台服务，基于 Spring Boot 2.7 + MyBatis + MySQL 5.7

## 项目概览

- **语言**: Java 8
- **框架**: Spring Boot 2.7.14
- **数据库**: MySQL 5.7.44
- **ORM**: MyBatis
- **缓存**: Redis
- **消息队列**: Kafka
- **分布式事务**: Seata
- **版本**: 1.0.0

## 快速开始

### 开发环境启动

#### 方式一：本地启动（需要先安装 MySQL、Redis、Kafka）

```bash
# 1. 创建数据库
mysql -u root -p < src/main/resources/db/init.sql

# 2. 启动应用
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# 3. 访问
http://localhost:8080/api/health
```

#### 方式二：Docker Compose 启动（推荐）

```bash
# 开发环境（包含 MySQL、Redis、Kafka、Zookeeper）
docker-compose up -d

# 查看日志
docker-compose logs -f quicktap-server

# 停止服务
docker-compose down
```

### 生产环境启动

```bash
# 生产环境（包含 MySQL、Redis、Kafka、Nacos、Seata）
docker-compose -f docker-compose-prod.yml up -d

# 查看日志
docker-compose -f docker-compose-prod.yml logs -f quicktap-server

# 停止服务
docker-compose -f docker-compose-prod.yml down
```

## 项目结构

```
QuickTap-Server/
├── src/main/java/com/quicktap/
│   ├── controller/          # API 控制层
│   ├── service/             # 业务层
│   ├── mapper/              # MyBatis 数据层
│   ├── entity/              # 实体类
│   ├── dto/                 # 数据传输对象
│   ├── security/            # Spring Security 配置
│   ├── config/              # 配置类
│   ├── utils/               # 工具类
│   └── QuickTapApplication.java  # 启动类
│
├── src/main/resources/
│   ├── application.yml      # 主配置文件
│   ├── application-dev.yml  # 开发环境配置
│   ├── application-prod.yml # 生产环境配置
│   ├── mybatis/mapper/      # MyBatis XML 映射文件
│   └── db/
│       └── init.sql         # 数据库初始化脚本
│
├── pom.xml                  # Maven 依赖配置
├── Dockerfile              # Docker 镜像配置
├── docker-compose.yml      # 开发环境编排
└── docker-compose-prod.yml # 生产环境编排
```

## 配置说明

### 开发环境 (application-dev.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/quick_tap
    username: root
    password: root123
  redis:
    host: localhost
    port: 6379
  kafka:
    bootstrap-servers: localhost:9092
```

### 生产环境 (application-prod.yml)

支持环境变量覆盖配置：

```bash
DB_PASSWORD=your_db_password
REDIS_PASSWORD=your_redis_password
JWT_SECRET=your_jwt_secret
```

## API 文档

### 健康检查

```bash
GET /api/health
```

### 认证接口

#### 管理员登录

```bash
POST /api/admin/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

# 成功响应
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "expiresIn": 604800000
  },
  "timestamp": 1234567890
}
```

#### 使用 JWT Token 访问受保护资源

所有需要认证的 API 请求都需要在 HTTP 请求头中包含 JWT Token：

```bash
GET /api/admin/list
Authorization: Bearer <your-jwt-token>

# 成功响应 (code=0)
{
  "code": 0,
  "message": "请求成功",
  "data": [ ... ],
  "timestamp": 1234567890
}

# 认证失败 (code=401)
{
  "code": 401,
  "message": "认证失败: 无效的 Token",
  "timestamp": 1234567890
}
```

### 默认登录凭证（开发环境）

- **用户名**: admin
- **密码**: admin123（bcrypt加密: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/SLm）

### JWT Token 说明

- **签名算法**: HS512
- **过期时间**: 7 天（604800000 毫秒）
- **Token 包含信息**: username, userId, role, issuedAt, expiration
- **配置参数** (环境变量):
  - `JWT_SECRET`: JWT 签名密钥（生产环境必须修改）
  - `JWT_EXPIRATION`: Token 过期时间（毫秒）

## 数据库

### 初始化

```bash
# 自动初始化（Docker Compose）
docker-compose up -d

# 手动初始化
mysql -u root -p quick_tap < src/main/resources/db/init.sql
```

### 主要表

- `admin` - 管理员表
- `user` - C端用户表
- `merchant` - 商户表
- `device` - 设备表
- `promotion_platform` - 推广平台表
- `merchant_promotion_config` - 商户推广配置表
- `coupon` - 卡券表
- `plan` - 套餐表
- `qrcode` - 二维码表
- `order_record` - 订单表
- `ai_generate_record` - AI生成记录表

## 核心功能

- ✅ 身份认证（JWT + Spring Security）
- ✅ 商户管理
- ✅ 设备管理
- ✅ 推广营销
- ✅ 卡券系统
- ✅ 订单管理
- ✅ AI内容生成
- ✅ Redis 热数据缓存
- ✅ Kafka 异步任务和事件驱动
- ✅ Seata 分布式事务支持

## 依赖版本

- Spring Boot: 2.7.14
- MySQL: 5.1.49
- MyBatis: 2.3.1
- Redis: 7-alpine
- Kafka: 7.4.0
- JWT: 0.11.5
- Seata: 1.6.1

## 常用命令

```bash
# Maven 构建
mvn clean package

# Docker 构建镜像
docker build -t quicktap-server:latest .

# 启动开发环境
docker-compose up -d

# 启动生产环境
docker-compose -f docker-compose-prod.yml up -d

# 查看日志
docker-compose logs -f quicktap-server

# 进入容器
docker exec -it quicktap-server-dev /bin/bash

# 停止所有服务
docker-compose down
```

## 日志

```
logs/quicktap-server.log
```

## 许可证

MIT

## 作者

QuickTap Team

## 更新日志

### v1.0.0 (2024-01-01)
- 初始版本发布
- 完整的管理后台和用户API
- Spring Security JWT 认证
- MyBatis ORM 集成
- Redis 缓存支持
- Kafka 消息队列
- Seata 分布式事务
- Docker 容器化支持
