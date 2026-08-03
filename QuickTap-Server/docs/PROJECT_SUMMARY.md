# QuickTap Server - 项目完整指南

## 📋 目录

1. [项目概览](#项目概览)
2. [技术栈](#技术栈)
3. [架构设计](#架构设计)
4. [快速开始](#快速开始)
5. [项目结构](#项目结构)
6. [核心功能](#核心功能)
7. [API文档](#api文档)
8. [安全指南](#安全指南)
9. [部署指南](#部署指南)
10. [开发者指南](#开发者指南)
11. [常见问题](#常见问题)
12. [贡献指南](#贡献指南)

---

## 项目概览

**QuickTap Server** 是一个基于 Java Spring Boot 的企业级后端服务，为 QuickTap 应用提供完整的业务逻辑和API接口支持。

### 项目特点

- ✅ **完全的企业级架构设计**：分层架构、事件驱动、服务治理
- ✅ **完善的安全机制**：JWT认证、权限控制、Token黑名单管理
- ✅ **高可用性支持**：分布式事务、缓存策略、消息队列集成
- ✅ **可观测性完善**：详细日志、性能监控、审计追踪
- ✅ **开发友好**：完整文档、示例代码、单元测试

### 项目信息

| 项目 | 详情 |
|------|------|
| **名称** | QuickTap Server |
| **版本** | 1.0.0 |
| **状态** | 生产就绪 |
| **主要语言** | Java |
| **框架** | Spring Boot 2.7.14 |
| **数据库** | MySQL 5.7+ |
| **缓存** | Redis |
| **消息队列** | Kafka |

---

## 技术栈

### 后端框架

```
Spring Boot 2.7.14
├── Spring Framework 5.3.x
├── Spring Security 5.7.x  # 认证和授权
├── Spring Data JPA       # 数据访问
└── Spring Data Redis     # 缓存支持
```

### 数据持久化

```
MyBatis 3.5+             # ORM框架
├── XML映射文件          # SQL管理
├── 动态SQL              # 灵活查询
└── 批量操作支持         # 性能优化
```

### 工具库

```
Lombok                   # 代码简化
JUnit 4 + Mockito 5      # 单元测试
TestContainers           # 集成测试
JaCoCo                   # 代码覆盖率
```

### 运维部署

```
Docker & Docker Compose  # 容器化
Maven 3.6+               # 项目构建
```

---

## 架构设计

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway / Load Balancer               │
└────────────────┬────────────────────────────────────────────┘
                 │
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                   │
├─────────────────────────────────────────────────────────────┤
│ Controller Layer (API层)                                    │
│ ├── AuthController        # 认证管理                        │
│ ├── UserController        # 用户管理                        │
│ ├── MerchantController    # 商户管理                        │
│ └── ...其他控制器                                           │
├─────────────────────────────────────────────────────────────┤
│ Service Layer (业务层)                                      │
│ ├── AuthService           # 认证业务                        │
│ ├── UserService           # 用户业务                        │
│ ├── TokenBlacklistService # Token黑名单                     │
│ └── ...其他服务                                             │
├─────────────────────────────────────────────────────────────┤
│ Event-Driven Architecture (事件驱动)                        │
│ ├── TokenValidationEvent           # Token验证事件         │
│ ├── TokenRefreshEvent              # Token刷新事件         │
│ ├── UserLogoutEvent                # 登出事件             │
│ └── JwtTokenValidationListener     # 监听器处理            │
├─────────────────────────────────────────────────────────────┤
│ Data Layer (数据层)                                         │
│ ├── MyBatis Mapper        # SQL映射                         │
│ ├── Spring Data JPA       # ORM操作                         │
│ └── Entity                # 数据实体                        │
├─────────────────────────────────────────────────────────────┤
│ Data Sources (数据源)                                       │
│ ├── MySQL Database        # 主数据存储                      │
│ ├── Redis Cache           # 分布式缓存                      │
│ └── Kafka Queue           # 消息队列                        │
└─────────────────────────────────────────────────────────────┘
```

### JWT认证流程

```
客户端                          服务器
  │                              │
  ├─── POST /login ──────────────>│
  │    (username, password)        │
  │                          验证用户
  │                        生成JWT Token
  │<───── 200 OK ──────────────────┤
  │       {token: "xxx..."}        │
  │                              │
  ├─── GET /api/data ─────────────>│
  │    Authorization: Bearer xxx...│
  │                         验证Token
  │                      检查黑名单
  │                     验证权限信息
  │<───── 200 OK ──────────────────┤
  │       {data: {...}}            │
  │                              │
  ├─── POST /logout ──────────────>│
  │    Authorization: Bearer xxx...│
  │                      Token加入黑名单
  │<───── 200 OK ──────────────────┤
  │       {msg: "logout ok"}       │
```

### Token生命周期

```
┌─────────────────────────────────────────┐
│         Token生命周期管理              │
├─────────────────────────────────────────┤
│ 1. 生成                                │
│    ├── 登录时生成                      │
│    ├── 用户注册后自动登录              │
│    └── 微信登录创建Token               │
│                                        │
│ 2. 验证                                │
│    ├── 请求到达时验证                  │
│    ├── 检查签名有效性                  │
│    ├── 检查是否过期                    │
│    └── 检查是否在黑名单中              │
│                                        │
│ 3. 刷新                                │
│    ├── Token即将过期时刷新             │
│    ├── 生成新Token                     │
│    └── 旧Token加入黑名单               │
│                                        │
│ 4. 失效                                │
│    ├── 用户登出                        │
│    ├── Token过期                       │
│    ├── 权限变更                        │
│    └── 黑名单检查                      │
└─────────────────────────────────────────┘
```

---

## 快速开始

### 前置条件

- Java 8+
- Maven 3.6+
- MySQL 5.7+
- Redis 5.0+
- Kafka 2.8+ (可选)

### 本地开发启动

#### 1. 克隆项目

```bash
git clone <repository-url>
cd QuickTap-Server
```

#### 2. 配置环境

创建 `.env` 文件（参考 `.env.example`）：

```bash
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=quicktap
DB_USERNAME=quicktap
DB_PASSWORD=your_secure_password

# JWT配置
JWT_SECRET=your_very_long_and_secure_jwt_secret_key_here

# Redis配置（可选）
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Kafka配置（可选）
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

#### 3. 初始化数据库

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE quicktap CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入初始数据
mysql -u root -p quicktap < src/main/resources/db/init.sql
```

#### 4. 启动应用

**方式一：Maven命令**

```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

**方式二：IDE运行**

- 打开 IDE (IntelliJ IDEA / Eclipse)
- 右键点击 `QuickTapApplication.java`
- 选择 "Run 'QuickTapApplication.main()'"

**方式三：Docker启动（推荐开发环境）**

```bash
docker-compose up -d
```

#### 5. 验证启动

```bash
# 检查健康状态
curl http://localhost:8080/api/health

# 预期响应
{
  "code": 0,
  "message": "success",
  "data": "ok"
}
```

---

## 项目结构

```
QuickTap-Server/
│
├── src/main/java/com/quicktap/
│   │
│   ├── controller/                  # API控制层
│   │   ├── AuthController.java      # 认证接口
│   │   ├── UserController.java      # 用户接口
│   │   └── ...
│   │
│   ├── service/                     # 业务服务层
│   │   ├── AuthService.java         # 认证服务
│   │   ├── TokenBlacklistService.java  # Token黑名单服务
│   │   └── ...
│   │
│   ├── event/                       # 事件驱动模块
│   │   ├── TokenValidationEvent.java
│   │   ├── TokenRefreshEvent.java
│   │   ├── UserLogoutEvent.java
│   │   └── UserPermissionChangedEvent.java
│   │
│   ├── listener/                    # 事件监听器
│   │   └── JwtTokenValidationListener.java
│   │
│   ├── mapper/                      # MyBatis数据访问层
│   │   ├── UserMapper.java
│   │   ├── AdminMapper.java
│   │   └── ...
│   │
│   ├── repository/                  # Spring Data JPA仓储
│   │   ├── UserCouponRepository.java
│   │   └── ...
│   │
│   ├── entity/                      # 数据实体类
│   │   ├── User.java
│   │   ├── Admin.java
│   │   └── ...
│   │
│   ├── dto/                         # 数据传输对象
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   └── ...
│   │
│   ├── mapstruct/                   # MapStruct映射器
│   │   └── ...
│   │
│   ├── security/                    # 安全配置
│   │   ├── JwtTokenProvider.java    # JWT令牌提供者
│   │   ├── SecurityConfig.java      # Spring Security配置
│   │   └── UserPrincipal.java       # 用户主体
│   │
│   ├── config/                      # 全局配置
│   │   ├── WebConfig.java
│   │   └── ...
│   │
│   ├── utils/                       # 工具类
│   │   ├── PasswordUtil.java        # 密码加密工具
│   │   └── ...
│   │
│   ├── exception/                   # 异常处理
│   │   ├── BusinessException.java
│   │   └── GlobalExceptionHandler.java
│   │
│   └── QuickTapApplication.java     # 启动类
│
├── src/main/resources/
│   ├── application.yml              # 主配置文件
│   ├── application-dev.yml          # 开发环境配置
│   ├── application-prod.yml         # 生产环境配置
│   │
│   ├── mybatis/mapper/              # MyBatis XML映射文件
│   │   ├── UserMapper.xml
│   │   ├── AdminMapper.xml
│   │   └── ...
│   │
│   └── db/
│       └── init.sql                 # 数据库初始化脚本
│
├── src/test/java/
│   └── com/quicktap/                # 单元测试代码
│
├── docs/                            # 文档目录
│   ├── PROJECT_SUMMARY.md           # 项目完整指南（本文件）
│   ├── ARCHITECTURE.md              # 架构设计详解
│   ├── SECURITY_GUIDE.md            # 安全指南
│   ├── DEPLOYMENT_GUIDE.md          # 部署指南
│   └── API_DOCUMENTATION.md         # API文档
│
├── pom.xml                          # Maven依赖和构建配置
├── Dockerfile                       # Docker镜像配置
├── docker-compose.yml               # 开发环境编排
├── docker-compose-prod.yml          # 生产环境编排
├── .env.example                     # 环境变量示例
└── README.md                        # 快速入门文档
```

---

## 核心功能

### 1. 用户认证系统

- **支持多种登录方式**：
  - 用户名密码登录
  - 微信小程序登录
  - 用户自注册

- **JWT Token管理**：
  - 自动生成和刷新Token
  - Token黑名单管理（支持Redis）
  - Token过期自动清理

- **权限控制**：
  - 基于角色的访问控制（RBAC）
  - 权限变更实时生效
  - 灵活的权限验证

### 2. 事件驱动架构

- **Token相关事件**：
  - TokenValidationEvent：Token验证
  - TokenRefreshEvent：Token刷新
  - TokenExpiredEvent：Token过期
  - UserLogoutEvent：用户登出
  - UserPermissionChangedEvent：权限变更

- **事件监听**：
  - JwtTokenValidationListener：统一处理所有JWT事件
  - 自动黑名单管理
  - 异步处理和性能优化

### 3. 数据持久化

- **MyBatis ORM**：
  - XML映射管理所有SQL
  - 支持动态SQL和批量操作
  - 16个MyBatis Mapper接口

- **Spring Data JPA**：
  - 6个JPA Repository接口
  - 支持分页和排序
  - 自定义查询方法

### 4. 缓存和消息

- **Redis缓存**：
  - Token黑名单缓存
  - 用户数据缓存
  - 支持自定义TTL

- **Kafka消息队列**：
  - 异步事件处理
  - 系统间的解耦

---

## API文档

### 认证接口

#### 1. 管理员登录

```http
POST /api/admin/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**成功响应 (200)**
```json
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "expiresIn": 604800000,
    "userId": 1,
    "username": "admin",
    "role": "admin",
    "merchantId": 1
  },
  "timestamp": 1234567890
}
```

#### 2. 用户登录

```http
POST /api/user/login
Content-Type: application/json

{
  "username": "user123",
  "password": "password123"
}
```

#### 3. 用户注册

```http
POST /api/user/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "secure_password",
  "nickname": "用户昵称"
}
```

#### 4. Token刷新

```http
POST /api/refresh-token
Authorization: Bearer <old_token>
```

#### 5. Token验证

```http
GET /api/validate-token
Authorization: Bearer <token>
```

**响应**
```json
{
  "code": 0,
  "message": "success",
  "data": true,
  "timestamp": 1234567890
}
```

#### 6. 用户登出

```http
POST /api/admin/auth/logout
Authorization: Bearer <token>
```

### 使用Token访问受保护资源

所有需要认证的API请求都需要在HTTP头中包含Token：

```http
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

---

## 安全指南

### 1. JWT Token安全

- **Secret密钥**：
  - 最少64字节长度（HS512算法要求）
  - 使用强随机生成
  - 从环境变量读取，不要硬编码

- **Token黑名单**：
  - 登出时自动加入黑名单
  - Token刷新时旧Token加入黑名单
  - Redis自动过期清理

### 2. 密码安全

- **密码存储**：
  - 使用BCrypt加密算法（salt+hash）
  - 不存储明文密码

- **密码验证**：
  - 使用PasswordUtil.matches()方法
  - 防止暴力破解（可添加尝试限制）

### 3. 权限验证

- **权限检查**：
  - 每个API都需要验证Token有效性
  - 每个操作都需要检查用户权限
  - 权限变更时强制重新认证

- **审计日志**：
  - 记录所有认证事件
  - 记录所有权限变更
  - 便于安全审计和故障追溯

### 4. 环境配置安全

- **敏感信息**：
  ```yaml
  # ✅ 正确做法：使用环境变量
  spring:
    datasource:
      username: ${DB_USERNAME:default}
      password: ${DB_PASSWORD:default}
  jwt:
    secret: ${JWT_SECRET:default_secret}

  # ❌ 错误做法：硬编码密码
  # username: root
  # password: admin123
  ```

- **环境变量管理**：
  - 开发环境：使用.env文件（不提交到Git）
  - 生产环境：使用系统环境变量或密钥管理服务

---

## 部署指南

### Docker部署

#### 开发环境

```bash
# 启动所有服务（包含MySQL、Redis、Kafka等）
docker-compose up -d

# 查看日志
docker-compose logs -f quicktap-server

# 停止服务
docker-compose down
```

#### 生产环境

```bash
# 构建镜像
docker build -t quicktap-server:1.0.0 .

# 启动容器
docker run -d \
  --name quicktap-server \
  -p 8080:8080 \
  -e DB_HOST=prod-db.example.com \
  -e DB_USERNAME=prod_user \
  -e DB_PASSWORD=secure_password \
  -e JWT_SECRET=production_secret \
  -e SPRING_PROFILES_ACTIVE=prod \
  quicktap-server:1.0.0
```

### Kubernetes部署

参考 `docs/DEPLOYMENT_GUIDE.md` 获取详细的K8s部署说明。

---

## 开发者指南

### 代码规范

#### Java代码风格

```java
// ✅ 好的实践

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户对象，如果不存在返回null
     */
    public User getUserById(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(400, "用户ID不能为空或非法");
        }

        User user = userMapper.selectById(userId);
        log.info("获取用户信息 - userId: {}", userId);
        return user;
    }
}

// ❌ 避免的做法

public class UserService {
    // 避免魔法数字
    private int MAX_RETRY = 3;  // 无意义的注释

    public User getUser(Long id) {
        return userMapper.selectById(id);  // 没有参数验证
    }
}
```

#### 事件发布示例

```java
@Service
public class AuthService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void logout(String token, Integer userId, String username) {
        String traceId = UUID.randomUUID().toString();

        // 发布登出事件
        UserLogoutEvent event = new UserLogoutEvent(this, traceId, userId, token, username);
        eventPublisher.publishEvent(event);

        log.info("用户登出事件已发布 - userId: {}", userId);
    }
}
```

### 单元测试

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthServiceTest {

    @MockBean
    private AdminMapper adminMapper;

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private AuthService authService;

    @Test
    public void testAdminLogin_Success() {
        // Given
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword(PasswordUtil.encode("password123"));
        admin.setStatus(1);

        when(adminMapper.selectByUsername("admin")).thenReturn(admin);

        // When
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password123");

        LoginResponse response = authService.adminLogin(request);

        // Then
        assertNotNull(response.getToken());
        assertEquals("admin", response.getUsername());
    }
}
```

### 新增API步骤

1. **创建Controller**
   ```java
   @PostMapping("/api/resource/create")
   public ApiResponse<ResourceDTO> create(@RequestBody CreateResourceRequest request) {
       ResourceDTO result = resourceService.create(request);
       return ApiResponse.success("创建成功", result);
   }
   ```

2. **创建Service**
   ```java
   @Service
   public class ResourceService {
       public ResourceDTO create(CreateResourceRequest request) {
           // 业务逻辑
       }
   }
   ```

3. **创建Mapper**
   ```java
   @Mapper
   public interface ResourceMapper {
       int insert(Resource resource);
   }
   ```

4. **编写XML**
   ```xml
   <insert id="insert" parameterType="com.quicktap.entity.Resource">
       INSERT INTO resource (name, description, created_at)
       VALUES (#{name}, #{description}, NOW())
   </insert>
   ```

5. **编写测试**
   ```java
   @Test
   public void testCreate() { ... }
   ```

---

## 常见问题

### Q1: 如何重置密码？

**A:** 目前没有前端重置密码功能。可以：
1. 直接修改数据库密码字段（需要BCrypt加密）
2. 或添加重置密码API

### Q2: Token过期了怎么办？

**A:** 有两种处理方式：
1. **前端处理**：捕获401错误，调用`/api/refresh-token`刷新Token
2. **自动刷新**：在拦截器中自动刷新即将过期的Token

### Q3: 如何查看系统日志？

**A:**
```bash
# Docker环境
docker-compose logs -f quicktap-server

# 本地运行
tail -f logs/application.log
```

### Q4: 如何添加新的权限角色？

**A:**
1. 在数据库Admin表中添加新的role值
2. 在SecurityConfig中配置权限
3. 在Controller中使用@PreAuthorize验证权限

### Q5: 性能优化建议？

**A:**
1. **缓存策略**：使用Redis缓存热数据
2. **数据库优化**：添加索引、优化查询
3. **连接池**：配置合理的数据库连接池
4. **异步处理**：使用Kafka异步处理非关键业务

---

## 贡献指南

### 报告问题

提交Issue时请包含：
- 问题描述和复现步骤
- 期望行为和实际行为
- 系统环境（Java版本、OS等）

### 提交代码

1. Fork项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

### 代码审查标准

- 遵循项目代码规范
- 包含单元测试（覆盖率>80%）
- 更新相关文档
- 通过所有CI检查

---

## 许可证

MIT License - 详见 LICENSE 文件

---

## 联系方式

- **项目维护者**：[维护者信息]
- **文档反馈**：提交Issue或PR
- **安全漏洞报告**：security@example.com

---

**最后更新**: 2024年
**文档版本**: 1.0.0

