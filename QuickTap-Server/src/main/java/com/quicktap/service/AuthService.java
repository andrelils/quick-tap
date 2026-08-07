package com.quicktap.service;

import com.quicktap.common.ErrorCode;
import com.quicktap.dto.LoginRequest;
import com.quicktap.dto.LoginResponse;
import com.quicktap.dto.RegisterRequest;
import com.quicktap.entity.Admin;
import com.quicktap.entity.User;
import com.quicktap.event.TokenRefreshEvent;
import com.quicktap.event.TokenValidationEvent;
import com.quicktap.event.UserLogoutEvent;
import com.quicktap.exception.BusinessException;
import com.quicktap.mapper.AdminMapper;
import com.quicktap.mapper.UserMapper;
import com.quicktap.security.JwtTokenProvider;
import com.quicktap.security.UserPrincipal;
import com.quicktap.utils.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 认证服务
 * 处理登录、注册、token 刷新等业务逻辑
 * 集成事件驱动架构，发布JWT相关事件
 */
@Slf4j
@Service
public class AuthService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // 用于防止并发token刷新的锁映射表
    // key: userId, value: 该用户的读写锁
    // 目的：每个用户有自己的锁，避免全局锁导致的性能问题
    private final ConcurrentHashMap<Integer, ReentrantReadWriteLock> userRefreshLocks = new ConcurrentHashMap<>();

    /**
     * 获取用户的token刷新锁
     * 用于解决并发刷新同一用户token导致的race condition
     */
    private ReentrantReadWriteLock getUserRefreshLock(Integer userId) {
        return userRefreshLocks.computeIfAbsent(userId, k -> new ReentrantReadWriteLock());
    }

    /**
     * 管理员登录
     * @param loginRequest 登录请求（用户名、密码）
     * @return 登录响应（token、用户信息）
     */
    public LoginResponse adminLogin(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // 验证用户名和密码不为空
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "用户名或密码不能为空");
        }

        // 从数据库加载管理员
        Admin admin = adminMapper.selectByUsername(username);
        if (admin == null) {
            log.warn("登录失败：管理员不存在, username={}", username);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "用户名或密码错误");
        }

        // 验证密码
        if (!PasswordUtil.matches(password, admin.getPassword())) {
            log.warn("登录失败：密码错误, username={}", username);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "用户名或密码错误");
        }

        // 检查账号是否启用
        if (admin.getStatus() == null || admin.getStatus() == 0) {
            log.warn("登录失败：账号已禁用, username={}", username);
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "该账号已被禁用");
        }

        // 生成 JWT token
        String token = jwtTokenProvider.generateToken(admin.getUsername(), admin.getId(), admin.getRole());

        log.info("管理员登录成功: username={}, id={}", username, admin.getId());

        return LoginResponse.builder()
            .token(token)
            .expiresIn(604800000L)  // 7 days
            .userId(admin.getId())
            .username(admin.getUsername())
            .role(admin.getRole())
            .merchantId(admin.getMerchantId())
            .build();
    }

    /**
     * 普通用户登录
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    public LoginResponse userLogin(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "用户名或密码不能为空");
        }

        // 从数据库加载用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            log.warn("用户登录失败：用户不存在, username={}", username);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "用户名或密码错误");
        }

        // 验证密码
        if (!PasswordUtil.matches(password, user.getPassword())) {
            log.warn("用户登录失败：密码错误, username={}", username);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "用户名或密码错误");
        }

        // 检查账号是否启用
        if (user.getStatus() == null || user.getStatus() == 0) {
            log.warn("用户登录失败：账号已禁用, username={}", username);
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "该账号已被禁用");
        }

        // 生成 JWT token
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId(), "user");

        log.info("用户登录成功: username={}, id={}", username, user.getId());

        return LoginResponse.builder()
            .token(token)
            .expiresIn(604800000L)
            .userId(user.getId())
            .username(user.getUsername())
            .role("user")
            .build();
    }

    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 注册响应
     */
    public LoginResponse userRegister(RegisterRequest registerRequest) {
        String username = registerRequest.getUsername();
        String password = registerRequest.getPassword();
        String nickname = registerRequest.getNickname();

        // 验证参数
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "密码不能为空");
        }
        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_SHORT, "密码长度至少 6 个字符");
        }

        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(username);
        if (existingUser != null) {
            log.warn("注册失败：用户名已存在, username={}", username);
            throw new BusinessException(ErrorCode.USERNAME_EXISTS, "用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.encode(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setStatus(1);  // 启用
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 保存用户
        int result = userMapper.insert(user);
        if (result <= 0) {
            log.error("用户注册失败: username={}", username);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "注册失败，请稍后重试");
        }

        log.info("用户注册成功: username={}, id={}", username, user.getId());

        // 自动登录
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId(), "user");

        return LoginResponse.builder()
            .token(token)
            .expiresIn(604800000L)
            .userId(user.getId())
            .username(user.getUsername())
            .role("user")
            .build();
    }

    /**
     * 刷新 JWT token
     * 解决并发刷新race condition：
     * - 多个并发请求可能同时刷新同一个用户的token
     * - 使用per-user ReadWriteLock确保token刷新的原子性
     * - 只允许一个写操作（刷新），多个读操作可并行
     * @param oldToken 旧的 token
     * @return 新的 token
     */
    public LoginResponse refreshToken(String oldToken) {
        String traceId = UUID.randomUUID().toString();

        // 验证 token 是否有效（即使已过期也能继续使用）
        if (!jwtTokenProvider.validateToken(oldToken)) {
            // 发布Token刷新失败事件
            TokenRefreshEvent event = new TokenRefreshEvent(this, traceId, null, oldToken);
            event.setRefreshFailed(true);
            eventPublisher.publishEvent(event);

            throw new BusinessException(ErrorCode.TOKEN_INVALID, "Token 无效");
        }

        String username = jwtTokenProvider.getUsernameFromToken(oldToken);
        Integer userId = jwtTokenProvider.getUserIdFromToken(oldToken);
        String role = jwtTokenProvider.getRoleFromToken(oldToken);

        // 获取该用户的刷新锁，防止并发刷新同一token
        ReentrantReadWriteLock lock = getUserRefreshLock(userId);
        lock.writeLock().lock();
        try {
            log.debug("Token刷新：获得写锁成功，userId={}, traceId={}", userId, traceId);

            // 生成新 token
            String newToken = jwtTokenProvider.generateToken(username, userId, role);

            // 发布Token刷新事件
            TokenRefreshEvent event = new TokenRefreshEvent(this, traceId, userId, oldToken);
            event.setNewToken(newToken);
            event.setRefreshFailed(false);
            eventPublisher.publishEvent(event);

            log.info("Token 刷新成功: traceId={}, username={}, userId={}", traceId, username, userId);

            return LoginResponse.builder()
                .token(newToken)
                .expiresIn(604800000L)
                .userId(userId)
                .username(username)
                .role(role)
                .build();
        } finally {
            lock.writeLock().unlock();
            log.debug("Token刷新：释放写锁，userId={}, traceId={}", userId, traceId);
        }
    }

    /**
     * 验证 token 有效性
     * 通过同步事件由 Listener 完成黑名单检查 + 签名验证，并读取事件结果作为最终结论。
     * @param token JWT token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        String traceId = UUID.randomUUID().toString();
        Integer userId = null;

        try {
            userId = jwtTokenProvider.getUserIdFromToken(token);
        } catch (Exception e) {
            log.debug("无法从token中提取userId");
        }

        // 发布Token验证事件（同步执行），由 Listener 综合黑名单与签名验证后设置 valid
        TokenValidationEvent event = new TokenValidationEvent(this, traceId, userId, token, "VALIDATE");
        eventPublisher.publishEvent(event);

        // 读取 Listener 设置的验证结果
        return event.isValid();
    }

    /**
     * 获取当前登录用户信息
     * @param userPrincipal 用户主体
     * @return 用户信息
     */
    public UserPrincipal getCurrentUser(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未认证");
        }
        return userPrincipal;
    }

    /**
     * 用户登出
     * @param token JWT token
     * @param userId 用户ID
     * @param username 用户名
     */
    public void logout(String token, Integer userId, String username) {
        String traceId = UUID.randomUUID().toString();

        // 发布用户登出事件
        UserLogoutEvent event = new UserLogoutEvent(this, traceId, userId, token, username);
        eventPublisher.publishEvent(event);

        log.info("用户登出事件已发布: traceId={}, userId={}, username={}", traceId, userId, username);
    }

    /**
     * 微信小程序登录
     * @param code 微信授权码
     * @return 登录响应
     */
    public LoginResponse wechatMiniLogin(String code) {
        // 验证授权码
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "授权码不能为空");
        }

        // 从微信服务器交换 session_key 和 openid
        // 实际应用中应调用微信 API: https://api.weixin.qq.com/sns/jscode2session
        // 这里使用模拟的 openid（实际应用中应该调用真实的微信 API）
        String openid = "openid_" + UUID.randomUUID().toString().substring(0, 16);
        String unionid = UUID.randomUUID().toString();

        // 检查用户是否存在（通过 openid）
        User existingUser = userMapper.selectByOpenid(openid);

        if (existingUser != null) {
            // 用户已存在，直接生成 token
            log.info("微信用户登录: openid={}, username={}", openid, existingUser.getUsername());

            String token = jwtTokenProvider.generateToken(existingUser.getUsername(), existingUser.getId(), "user");

            return LoginResponse.builder()
                .token(token)
                .expiresIn(604800000L)
                .userId(existingUser.getId())
                .username(existingUser.getUsername())
                .role("user")
                .build();
        }

        // 用户不存在，创建新用户
        User newUser = new User();
        newUser.setOpenid(openid);
        newUser.setUnionid(unionid);

        // 生成用户名：使用 code 的后6位前缀加上随机数
        String username = "用户" + code.substring(Math.max(0, code.length() - 6));

        // 如果用户名已存在，添加随机后缀
        int suffix = 1;
        String originalUsername = username;
        while (userMapper.selectByUsername(username) != null) {
            username = originalUsername + suffix;
            suffix++;
        }

        newUser.setUsername(username);
        newUser.setNickname(username);
        newUser.setStatus(1);  // 启用
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        // 保存新用户
        int result = userMapper.insert(newUser);
        if (result <= 0) {
            log.error("微信用户创建失败: openid={}", openid);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "登录失败，请稍后重试");
        }

        log.info("微信用户创建成功: username={}, id={}, openid={}", username, newUser.getId(), openid);

        // 生成 token
        String token = jwtTokenProvider.generateToken(newUser.getUsername(), newUser.getId(), "user");

        return LoginResponse.builder()
            .token(token)
            .expiresIn(604800000L)
            .userId(newUser.getId())
            .username(newUser.getUsername())
            .role("user")
            .build();
    }
}
