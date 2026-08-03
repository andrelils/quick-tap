# 安全指南

## 概述

本文档详细说明QuickTap Server的安全设计、实现和最佳实践。

## 1. 认证安全

### JWT Token安全

#### Token生成安全

```java
// ✅ 正确的实现
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")  // 从环境变量读取
    private String jwtSecret;

    @Value("${jwt.expiration:604800000}")  // 7天过期
    private long jwtExpirationInMs;

    public String generateToken(String username, Integer userId, String role) {
        long now = System.currentTimeMillis();
        long expiryTime = now + jwtExpirationInMs;

        return Jwts.builder()
            .setSubject(username)
            .claim("userId", userId)
            .claim("role", role)
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(expiryTime))
            .signWith(SignatureAlgorithm.HS512, jwtSecret)  // 使用HS512算法
            .compact();
    }
}

// ❌ 错误的做法
private String jwtSecret = "hardcoded-secret-123";  // 硬编码
// 或
private String jwtSecret = "short";  // 太短，不安全
```

#### Token验证安全

```java
// 完整的验证流程
public boolean validateToken(String token) {
    try {
        // 1. 验证签名和格式
        Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token);

        // 2. 检查黑名单
        if (tokenBlacklistService.isBlacklisted(token)) {
            log.warn("Token已在黑名单中");
            return false;
        }

        // 3. 检查过期时间
        Date expiration = getExpirationDateFromToken(token);
        if (expiration.before(new Date())) {
            log.warn("Token已过期");
            return false;
        }

        return true;
    } catch (JwtException | IllegalArgumentException e) {
        log.error("Token验证失败: {}", e.getMessage());
        return false;
    }
}
```

### Token黑名单管理

#### Redis黑名单实现

```java
@Service
public class TokenBlacklistService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    /**
     * 添加token到黑名单
     * @param token JWT token
     */
    public void addToBlacklist(String token) {
        try {
            long ttl = calculateTTL(token);  // 计算token剩余过期时间
            String key = BLACKLIST_PREFIX + token;

            // 设置Redis key，TTL为token过期时间
            redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.SECONDS);
            log.info("Token已加入黑名单 - TTL: {}s", ttl);
        } catch (Exception e) {
            log.error("黑名单操作失败: {}", e.getMessage());
        }
    }

    /**
     * 检查token是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            return redisTemplate.hasKey(key) != null && redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("检查黑名单失败: {}", e.getMessage());
            return false;  // 失败时允许请求通过
        }
    }
}
```

#### 登出流程

```java
@PostMapping("/api/admin/auth/logout")
public ApiResponse<Void> logout(@RequestHeader("Authorization") String token) {
    // 1. 提取token
    String actualToken = token.substring(7);  // 移除"Bearer "前缀

    // 2. 从token中提取用户信息
    Integer userId = jwtTokenProvider.getUserIdFromToken(actualToken);
    String username = jwtTokenProvider.getUsernameFromToken(actualToken);

    // 3. 发布登出事件
    UserLogoutEvent event = new UserLogoutEvent(this, UUID.randomUUID().toString(),
                                                userId, actualToken, username);
    eventPublisher.publishEvent(event);

    // 4. Listener会自动处理token黑名单
    return ApiResponse.success("登出成功");
}
```

## 2. 密码安全

### 密码加密

#### BCrypt实现

```java
@Slf4j
@Component
public class PasswordUtil {
    /**
     * 使用BCrypt加密密码
     * BCrypt特性：
     * - 自动生成salt
     * - 适应性强：可调整强度
     * - 慢哈希：防止暴力破解
     */
    public static String encode(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPassword, encodedPassword);
    }
}
```

#### 正确的密码存储

```java
// ✅ 正确做法：保存加密后的密码
User user = new User();
user.setPassword(PasswordUtil.encode("user_input_password"));  // BCrypt加密
userRepository.save(user);

// 验证时使用matches方法
boolean isValid = PasswordUtil.matches(inputPassword, storedPassword);

// ❌ 错误做法：保存明文密码
user.setPassword("user_input_password");  // 明文密码，非常危险！
```

### 密码策略

```yaml
# application.yml
password:
  minLength: 8              # 最少8个字符
  requireUppercase: true    # 需要大写字母
  requireLowercase: true    # 需要小写字母
  requireNumbers: true      # 需要数字
  requireSpecialChars: true # 需要特殊字符
  expireDays: 90           # 密码90天过期
```

## 3. 权限控制

### 基于角色的访问控制 (RBAC)

```java
@RestController
@RequestMapping("/api")
public class AdminController {

    /**
     * 只有ADMIN角色可以访问
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        // 删除用户逻辑
        return ApiResponse.success("删除成功");
    }

    /**
     * ADMIN和MANAGER都可以访问
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/admin/reports")
    public ApiResponse<?> getReports() {
        // 生成报表
        return ApiResponse.success("报表数据");
    }

    /**
     * 任何已认证的用户都可以访问
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/profile")
    public ApiResponse<?> getProfile() {
        // 用户信息
        return ApiResponse.success("用户信息");
    }
}
```

### 权限变更安全

```java
@Service
public class UserService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * 修改用户角色（权限变更）
     */
    @Transactional
    public void changeUserRole(Long userId, String newRole, Long changedBy) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        List<String> oldRoles = Arrays.asList(user.getRole());
        List<String> newRoles = Arrays.asList(newRole);

        // 更新数据库
        user.setRole(newRole);
        userRepository.save(user);

        // 发布权限变更事件（重要：强制重新认证）
        UserPermissionChangedEvent event = new UserPermissionChangedEvent(
            this,
            UUID.randomUUID().toString(),
            userId,
            oldRoles,
            newRoles
        );
        event.setChangeReason("管理员修改用户角色");
        event.setChangedBy(changedBy);

        eventPublisher.publishEvent(event);

        log.info("用户角色已变更 - userId: {}, oldRoles: {}, newRoles: {}",
                userId, oldRoles, newRoles);
    }
}
```

## 4. 敏感信息保护

### 环境变量配置

```yaml
# ✅ 正确做法：使用环境变量
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:quicktap}
    username: ${DB_USERNAME:quicktap}
    password: ${DB_PASSWORD:default_password}

  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}

jwt:
  secret: ${JWT_SECRET:default_secret_key_min_64_bytes}
```

```bash
# .env文件（不提交到Git）
DB_HOST=prod-db.example.com
DB_PORT=3306
DB_NAME=quicktap
DB_USERNAME=db_user
DB_PASSWORD=very_secure_password_here_min_16_chars

JWT_SECRET=production_jwt_secret_key_very_long_and_secure_min_64_bytes

REDIS_HOST=prod-redis.example.com
REDIS_PASSWORD=redis_password
```

### .gitignore配置

```
# 环境变量文件
.env
.env.local
.env.*.local

# IDE配置
.idea/
.vscode/
*.swp
*.swo

# 构建产物
target/
build/
*.jar
*.war

# 日志
logs/
*.log

# 临时文件
tmp/
temp/
```

### 日志安全

```java
// ❌ 错误：记录敏感信息
log.info("用户登录 - username: {}, password: {}", username, password);
log.debug("数据库连接: {}, username: {}, password: {}", url, dbUsername, dbPassword);

// ✅ 正确：掩盖敏感信息
log.info("用户登录 - username: {}", maskUsername(username));
log.debug("数据库连接 - host: {}", maskUrl(url));

// 实现掩盖函数
private String maskUsername(String username) {
    if (username == null || username.length() < 3) return "***";
    return username.substring(0, 1) + "***" + username.substring(username.length() - 1);
}

private String maskPassword(String password) {
    return "***";
}

private String maskUrl(String url) {
    return url.replaceAll("://[^@]*@", "://***@");
}
```

## 5. API安全

### HTTPS强制

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.requiresChannel()
            .anyRequest()
            .requiresSecure();  // 强制使用HTTPS

        http.headers()
            .contentSecurityPolicy("default-src 'self'");  // CSP防护

        return http.build();
    }
}
```

### CORS配置

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("https://yourdomain.com")  // 只允许特定域名
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .maxAge(3600);  // 预检请求缓存时间
    }
}
```

### 请求限流

```java
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;

    private static final long WINDOW_SIZE = 60;  // 1分钟
    private static final int MAX_REQUESTS = 100;  // 最多100个请求

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        String clientIp = getClientIp(request);
        String key = "rate_limit:" + clientIp;

        Integer currentCount = redisTemplate.opsForValue().get(key);
        if (currentCount == null) {
            redisTemplate.opsForValue().set(key, 1, WINDOW_SIZE, TimeUnit.SECONDS);
        } else if (currentCount >= MAX_REQUESTS) {
            response.setStatus(429);  // Too Many Requests
            response.getWriter().write("请求过于频繁，请稍后再试");
            return;
        } else {
            redisTemplate.opsForValue().increment(key);
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
```

## 6. SQL注入防护

### 参数化查询

```java
// ✅ 正确：使用参数化查询
@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(String username);  // MyBatis自动参数化
}

// XML方式
<select id="selectByUsername" parameterType="string" resultType="User">
    SELECT * FROM user WHERE username = #{username}
</select>

// ❌ 错误：字符串拼接
String sql = "SELECT * FROM user WHERE username = '" + username + "'";
// 如果username = "' OR '1'='1"，将会选出所有用户！
```

## 7. XSS防护

```java
@Component
public class XssFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        // 包装请求，清理恶意输入
        request = new XssHttpServletRequestWrapper(request);
        filterChain.doFilter(request, response);
    }
}

// 使用OWASP ESAPI或Spring Security的防XSS工具
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) {
            return null;
        }
        return Arrays.stream(values)
            .map(value -> removeXss(value))
            .toArray(String[]::new);
    }

    private String removeXss(String value) {
        // 移除危险标签和属性
        return value.replaceAll("<[^>]*>", "");
    }
}
```

## 8. 审计日志

```java
@Component
@Aspect
public class AuditAspect {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * 记录所有敏感操作的审计日志
     */
    @Before("@annotation(com.quicktap.annotation.Audit)")
    public void audit(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String operator = getCurrentUsername();
        LocalDateTime operationTime = LocalDateTime.now();

        AuditLog log = AuditLog.builder()
            .operator(operator)
            .action(className + "." + methodName)
            .operationTime(operationTime)
            .status("SUCCESS")
            .build();

        auditLogRepository.save(log);
    }

    @AfterThrowing(pointcut = "@annotation(com.quicktap.annotation.Audit)", throwing = "e")
    public void auditError(JoinPoint joinPoint, Exception e) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String operator = getCurrentUsername();

        AuditLog log = AuditLog.builder()
            .operator(operator)
            .action(className + "." + methodName)
            .operationTime(LocalDateTime.now())
            .status("FAILED")
            .errorMessage(e.getMessage())
            .build();

        auditLogRepository.save(log);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "UNKNOWN";
    }
}
```

## 9. 定期安全检查清单

### 部署前检查

- [ ] JWT_SECRET是否已设置（至少64字节）
- [ ] 数据库密码是否已更改
- [ ] Redis密码是否已设置
- [ ] HTTPS是否已启用
- [ ] CORS白名单是否正确配置
- [ ] 敏感日志是否已清理
- [ ] .env文件是否已从Git移除
- [ ] SQL注入防护是否正确实现
- [ ] XSS防护是否已启用
- [ ] 访问控制是否已配置

### 定期维护

- [ ] 检查Token黑名单大小
- [ ] 清理过期的审计日志
- [ ] 更新依赖库（安全补丁）
- [ ] 审查访问日志
- [ ] 验证备份完整性
- [ ] 测试灾难恢复流程

---

**最后更新**: 2024年
**版本**: 1.0.0
