# 架构设计文档

## 概述

QuickTap Server采用分层架构设计，结合事件驱动模式，实现高内聚、低耦合的系统设计。

## 系统架构

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                    客户端（Web/Mobile）                     │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP/HTTPS
┌────────────────────────▼────────────────────────────────────┐
│              API Gateway / Reverse Proxy                     │
│              (Nginx / Spring Cloud Gateway)                  │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│           Spring Boot Application (QuickTap Server)          │
├────────────────────────────────────────────────────────────┤
│ 1. Controller Layer - API接口层                            │
│    └─ 处理HTTP请求，参数验证，响应格式化                 │
├────────────────────────────────────────────────────────────┤
│ 2. Service Layer - 业务逻辑层                              │
│    ├─ AuthService - 认证业务                              │
│    ├─ UserService - 用户业务                              │
│    ├─ TokenBlacklistService - Token管理                   │
│    └─ ...                                                  │
├────────────────────────────────────────────────────────────┤
│ 3. Event-Driven Layer - 事件驱动层                         │
│    ├─ TokenValidationEvent                                │
│    ├─ TokenRefreshEvent                                   │
│    ├─ UserLogoutEvent                                     │
│    ├─ UserPermissionChangedEvent                          │
│    └─ JwtTokenValidationListener (监听器)                 │
├────────────────────────────────────────────────────────────┤
│ 4. Data Access Layer - 数据访问层                          │
│    ├─ MyBatis Mapper (XML方式)                            │
│    ├─ Spring Data JPA Repository                          │
│    └─ Entity Objects                                      │
├────────────────────────────────────────────────────────────┤
│ 5. Infrastructure Layer - 基础设施层                        │
│    ├─ Security Config - 安全配置                          │
│    ├─ Exception Handler - 异常处理                        │
│    └─ Utils - 工具类                                      │
└────────────────────────────────────────────────────────────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
┌────────▼──────┐ ┌────────▼──────┐ ┌───▼──────────┐
│  MySQL数据库   │ │  Redis缓存    │ │ Kafka消息队列 │
│   5.7+        │ │   5.0+        │ │  2.8+        │
└────────────────┘ └────────────────┘ └──────────────┘
```

### 分层设计详解

#### 1. Controller Layer (表现层)

**责任**：
- 接收HTTP请求
- 参数验证和格式转换
- 调用业务层处理
- 返回统一格式的响应

**示例**：
```java
@RestController
@RequestMapping("/api")
public class AuthController {
    @PostMapping("/admin/auth/login")
    public ApiResponse<LoginResponse> adminLogin(
        @Valid @RequestBody LoginRequest request) {
        // 参数验证由@Valid完成
        LoginResponse response = authService.adminLogin(request);
        return ApiResponse.success("登录成功", response);
    }
}
```

#### 2. Service Layer (业务层)

**责任**：
- 核心业务逻辑处理
- 事务管理
- 事件发布
- 调用数据访问层

**示例**：
```java
@Service
@Transactional
public class AuthService {
    @Autowired
    private AuthService authService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public LoginResponse adminLogin(LoginRequest request) {
        // 参数验证
        Admin admin = adminMapper.selectByUsername(request.getUsername());
        if (admin == null) {
            throw new BusinessException(403, "用户不存在");
        }

        // 密码验证
        if (!PasswordUtil.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException(403, "密码错误");
        }

        // 生成Token
        String token = jwtTokenProvider.generateToken(...);

        // 发布事件
        TokenValidationEvent event = new TokenValidationEvent(...);
        eventPublisher.publishEvent(event);

        return LoginResponse.builder()...build();
    }
}
```

#### 3. Event-Driven Layer (事件驱动层)

**责任**：
- 定义事件类
- 发布事件
- 监听和处理事件

**事件流程**：
```
Service发布事件 → EventPublisher → Listener处理 → 后续操作
```

**核心事件**：
```java
// 1. Token验证事件
@EventListener
public void onTokenValidationEvent(TokenValidationEvent event) {
    // 检查黑名单
    if (tokenBlacklistService.isBlacklisted(event.getToken())) {
        event.setValid(false);
        return;
    }
    // 验证token
    event.setValid(jwtTokenProvider.validateToken(event.getToken()));
}

// 2. Token刷新事件
@EventListener
public void onTokenRefreshEvent(TokenRefreshEvent event) {
    // 将旧token加入黑名单
    tokenBlacklistService.addToBlacklist(event.getOldToken());
}

// 3. 用户登出事件
@EventListener
public void onUserLogoutEvent(UserLogoutEvent event) {
    // 登出token加入黑名单
    tokenBlacklistService.addToBlacklist(event.getToken());
    // 清除用户缓存等操作
}

// 4. 权限变更事件
@EventListener
public void onUserPermissionChangedEvent(UserPermissionChangedEvent event) {
    // 使旧token失效
    // 强制用户重新认证
}
```

#### 4. Data Access Layer (数据访问层)

**MyBatis方式**（推荐用于复杂查询）：
```java
@Mapper
public interface UserMapper {
    User selectById(Long id);
    User selectByUsername(String username);
    int insert(User user);
}
```

**对应XML**：
```xml
<mapper namespace="com.quicktap.mapper.UserMapper">
    <select id="selectByUsername" parameterType="string" resultType="com.quicktap.entity.User">
        SELECT * FROM user WHERE username = #{username}
    </select>
</mapper>
```

**Spring Data JPA方式**（推荐用于简单CRUD）：
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    List<User> findByStatus(Integer status);
}
```

#### 5. Infrastructure Layer (基础设施层)

**安全配置**：
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/api/admin/**").hasRole("ADMIN")
            .antMatchers("/api/user/**").hasRole("USER")
            .anyRequest().authenticated()
            .and()
            .addFilter(new JwtAuthenticationFilter(...));
        return http.build();
    }
}
```

**异常处理**：
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }
}
```

## 数据流向

### 登录流程

```
┌──────────────┐
│  前端登录    │
└──────┬───────┘
       │ POST /api/admin/auth/login
       │ {username, password}
       ▼
┌──────────────────────────────────────┐
│    AuthController.adminLogin()       │
├──────────────────────────────────────┤
│  1. 验证请求参数                     │
│  2. 调用AuthService.adminLogin()    │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│    AuthService.adminLogin()          │
├──────────────────────────────────────┤
│  1. 验证用户名和密码                 │
│  2. 从数据库查询用户                 │
│  3. BCrypt验证密码                   │
│  4. 生成JWT Token                    │
│  5. 发布TokenValidationEvent        │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│  JwtTokenValidationListener          │
│  .onTokenValidationEvent()           │
├──────────────────────────────────────┤
│  1. 检查黑名单                       │
│  2. 验证token有效性                  │
│  3. 记录审计日志                     │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│    返回LoginResponse                 │
│    {token, userId, username, ...}    │
└──────────────────────────────────────┘
```

### Token验证流程

```
┌──────────────┐
│  API请求     │
│  +Token      │
└──────┬───────┘
       │
       ▼
┌──────────────────────────────────────┐
│  JwtAuthenticationFilter             │
├──────────────────────────────────────┤
│  1. 从Header提取Token                │
│  2. 调用AuthService.validateToken()  │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│  AuthService.validateToken()         │
├──────────────────────────────────────┤
│  1. 发布TokenValidationEvent        │
│  2. 等待Listener返回验证结果        │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│  JwtTokenValidationListener          │
├──────────────────────────────────────┤
│  1. 检查黑名单                       │
│  2. 验证签名                         │
│  3. 检查过期时间                     │
│  4. 返回验证结果                     │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────┬─────────────────────┐
│  是  │   验证通过          │  否
└──────┴─────────────────────┘
       │                     │
       ▼                     ▼
   继续处理              返回401/403
```

## 依赖管理

### 外部依赖关系

```
Controller
    └── Service
        ├── Mapper
        │   └── Database
        ├── Repository
        │   └── Database
        └── EventPublisher
            └── Listener

SecurityConfig
    └── JwtTokenProvider
        └── Token生成和验证

TokenBlacklistService
    └── Redis (可选)
        └── 分布式缓存
```

### 循环依赖避免

```java
// ❌ 错误：产生循环依赖
@Service
public class ServiceA {
    @Autowired
    private ServiceB serviceB;  // A依赖B
}

@Service
public class ServiceB {
    @Autowired
    private ServiceA serviceA;  // B依赖A
}

// ✅ 正确：使用事件解耦
@Service
public class ServiceA {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void doSomething() {
        eventPublisher.publishEvent(new SomeEvent());
    }
}

@Component
public class ServiceBListener {
    @EventListener
    public void onSomeEvent(SomeEvent event) {
        // 处理事件
    }
}
```

## 缓存策略

### Redis缓存应用

```
应用启动
    │
    ▼
┌─────────────────────────┐
│  Token黑名单缓存        │
│  key: jwt:blacklist:xxx │
│  value: blacklisted     │
│  TTL: Token过期时间     │
└─────────────────────────┘

用户数据缓存
    key: user:id:123
    value: JSON serialized User
    TTL: 1 hour

会话缓存
    key: session:userId:123
    value: session data
    TTL: depends on settings
```

### 缓存一致性

```
数据更新流程：
1. 更新数据库
2. 删除相关缓存
3. 下次请求时重新加载到缓存

权限变更时：
1. 更新数据库
2. 发布UserPermissionChangedEvent
3. Listener清除该用户的所有缓存
4. 强制Token失效
```

## 扩展性设计

### 添加新的事件类型

```java
// 1. 创建事件类
@Data
public class NewEvent extends ApplicationEvent {
    private String traceId;
    private Long userId;
    // ... 其他字段
}

// 2. 创建监听器
@Component
public class NewEventListener {
    @EventListener
    public void onNewEvent(NewEvent event) {
        // 处理事件
    }
}

// 3. 在Service中发布事件
@Service
public class SomeService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void doSomething() {
        eventPublisher.publishEvent(new NewEvent(this, traceId, userId));
    }
}
```

### 添加新的Service

```java
// 1. 创建Service类
@Service
public class NewService {
    // 依赖注入
}

// 2. 在Controller中使用
@RestController
public class NewController {
    @Autowired
    private NewService newService;
}
```

### 性能优化

1. **数据库查询优化**：
   - 添加适当的索引
   - 使用分页查询大数据集
   - 使用SELECT特定列而不是SELECT *

2. **缓存优化**：
   - 合理设置TTL
   - 使用缓存预热
   - 实现缓存更新策略

3. **异步处理**：
   - 使用Kafka处理非关键任务
   - 使用@Async进行异步方法调用
   - 事件驱动解耦不同模块

---

**最后更新**: 2024年
**版本**: 1.0.0
