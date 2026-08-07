package com.quicktap.service;

import com.quicktap.dto.UserDTO;
import com.quicktap.dto.UserRegisterRequest;
import com.quicktap.dto.UserLoginRequest;
import com.quicktap.dto.PageResponse;
import com.quicktap.entity.User;
import com.quicktap.exception.BusinessException;
import com.quicktap.mapper.UserMapper;
import com.quicktap.security.JwtTokenProvider;
import com.quicktap.utils.PasswordUtil;
import com.quicktap.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

/**
 * C 端用户 Service
 * 提供用户注册、登录、信息管理和管理员用户管理功能
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 用户注册（电话+密码）
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> register(UserRegisterRequest request) {
        log.info("用户注册 | username: {} | phone: {}", request.getUsername(), request.getPhone());

        // 检查用户名是否已存在（如果提供）
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            User existingUser = userMapper.selectByUsername(request.getUsername());
            if (existingUser != null) {
                throw new BusinessException(ErrorCode.USERNAME_EXISTS, "该用户名已被注册");
            }
        }

        // 检查电话是否已注册（如果提供）
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            User existing = userMapper.selectByPhone(request.getPhone());
            if (existing != null) {
                throw new BusinessException(ErrorCode.PHONE_EXISTS, "该电话号码已被注册");
            }
        }

        User user = User.builder()
                .username(request.getUsername())
                .phone(request.getPhone())
                .nickname(request.getNickname())
                .avatar(request.getAvatar() != null ? request.getAvatar() : "")
                .password(passwordEncoder.encode(request.getPassword()))
                .status(1)
                .build();

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);

        // 生成 Token
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("phone", user.getPhone());
        response.put("token", token);
        response.put("expiresIn", 604800);

        log.info("用户注册成功 | userId: {}", user.getId());
        return response;
    }

    /**
     * 微信小程序登录（自动注册）
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> wechatMiniLogin(String openid, String unionid, String nickname, String avatar) {
        log.info("微信登录 | openid: {}", openid);

        User user = userMapper.selectByOpenid(openid);

        if (user == null) {
            // 自动注册新用户
            user = User.builder()
                    .openid(openid)
                    .unionid(unionid)
                    .nickname(nickname != null ? nickname : "微信用户")
                    .avatar(avatar != null ? avatar : "")
                    .status(1)
                    .build();

            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            userMapper.insert(user);
            log.info("新用户自动注册 | userId: {}", user.getId());
        } else {
            // 更新用户信息
            user.setNickname(nickname);
            user.setAvatar(avatar);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.update(user);
        }

        // 生成 Token
        String token = jwtTokenProvider.generateToken(user.getOpenid(), user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("openid", user.getOpenid());
        response.put("nickname", user.getNickname());
        response.put("token", token);
        response.put("expiresIn", 604800);

        return response;
    }

    /**
     * 获取用户信息
     */
    public UserDTO getUserInfo(Long userId) {
        User user = userMapper.selectById((long) userId.intValue());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        return convertToDTO(user);
    }

    /**
     * 更新用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO updateUserInfo(Long userId, UserRegisterRequest request) {
        User user = userMapper.selectById((long) userId.intValue());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            // 检查电话是否已被其他用户使用
            User existingPhone = userMapper.selectByPhone(request.getPhone());
            if (existingPhone != null && !existingPhone.getId().equals(userId)) {
                throw new BusinessException(ErrorCode.PHONE_EXISTS, "该电话号码已被使用");
            }
            user.setPhone(request.getPhone());
        }
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.update(user);
        log.info("用户信息更新成功 | userId: {}", userId);

        return convertToDTO(user);
    }

    /**
     * 绑定电话号码
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO bindPhone(Long userId, String phone) {
        User user = userMapper.selectById((long) userId.intValue());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        // 检查电话是否已被使用
        User existingPhone = userMapper.selectByPhone(phone);
        if (existingPhone != null && !existingPhone.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PHONE_EXISTS, "该电话号码已被使用");
        }

        user.setPhone(phone);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);

        log.info("用户电话绑定成功 | userId: {}", userId);
        return convertToDTO(user);
    }

    /**
     * 转换为 DTO
     */
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId() != null ? user.getId().longValue() : null)
                .username(user.getUsername())
                .openid(user.getOpenid())
                .unionid(user.getUnionid())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // ========================================== 管理员用户管理接口 ==========================================

    /**
     * 分页查询用户列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param keyword  关键词（用户名、电话、昵称）
     * @return 分页结果
     */
    public PageResponse<UserDTO> listUsers(Integer pageNum, Integer pageSize, String keyword) {
        int offset = (pageNum - 1) * pageSize;

        // 构建查询条件
        Map<String, Object> params = new HashMap<>();
        if (keyword != null && !keyword.isEmpty()) {
            params.put("keyword", "%" + keyword + "%");
        }

        // 查询总数和分页数据
        long total = userMapper.countByKeyword(params);
        List<User> users = userMapper.selectByKeyword(params, offset, pageSize);

        List<UserDTO> dtoList = users.stream().map(this::convertToDTO).collect(Collectors.toList());
        return PageResponse.of(dtoList, pageNum, pageSize, total);
    }

    /**
     * 创建用户（管理员端）
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO createUser(UserRegisterRequest request) {
        log.info("管理员创建用户 | phone: {}", request.getPhone());

        // 检查电话是否已注册
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            User existing = userMapper.selectByPhone(request.getPhone());
            if (existing != null) {
                throw new BusinessException(ErrorCode.PHONE_EXISTS, "该电话号码已被注册");
            }
        }

        // 检查用户名是否已存在
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            User existingUser = userMapper.selectByUsername(request.getUsername());
            if (existingUser != null) {
                throw new BusinessException(ErrorCode.USERNAME_EXISTS, "该用户名已被注册");
            }
        }

        User user = User.builder()
                .username(request.getUsername())
                .phone(request.getPhone())
                .nickname(request.getNickname())
                .avatar(request.getAvatar() != null ? request.getAvatar() : "")
                .password(passwordEncoder.encode(request.getPassword() != null ? request.getPassword() : generateDefaultPassword()))
                .status(1)
                .build();

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        log.info("用户创建成功 | userId: {}", user.getId());

        return convertToDTO(user);
    }

    /**
     * 更新用户信息（管理员端）
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO updateUser(Long userId, UserRegisterRequest request) {
        User user = userMapper.selectById((long) userId.intValue());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            // 检查电话是否已被其他用户使用
            User existingPhone = userMapper.selectByPhone(request.getPhone());
            if (existingPhone != null && !existingPhone.getId().equals(userId)) {
                throw new BusinessException(ErrorCode.PHONE_EXISTS, "该电话号码已被使用");
            }
            user.setPhone(request.getPhone());
        }
        if (request.getUsername() != null) {
            // 检查用户名是否已被其他用户使用
            User existingUsername = userMapper.selectByUsername(request.getUsername());
            if (existingUsername != null && !existingUsername.getId().equals(userId)) {
                throw new BusinessException(ErrorCode.USERNAME_EXISTS, "该用户名已被使用");
            }
            user.setUsername(request.getUsername());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
        log.info("用户信息更新成功 | userId: {}", userId);

        return convertToDTO(user);
    }

    /**
     * 删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        User user = userMapper.selectById((long) userId.intValue());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        userMapper.delete((long) userId.intValue());
        log.info("用户删除成功 | userId: {}", userId);
    }

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态（1=启用, 0=禁用）
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById((long) userId.intValue());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        if (status != null && (status == 0 || status == 1)) {
            user.setStatus(status);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.update(user);
            log.info("用户状态更新成功 | userId: {}, status: {}", userId, status);
        } else {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "无效的状态值");
        }

        return convertToDTO(user);
    }

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @return 新密码信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> resetPassword(Long userId) {
        User user = userMapper.selectById((long) userId.intValue());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        String newPassword = generateDefaultPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);

        log.info("用户密码重置成功 | userId: {}", userId);

        Map<String, String> result = new HashMap<>();
        result.put("newPassword", newPassword);
        return result;
    }

    /**
     * 生成默认密码
     */
    private String generateDefaultPassword() {
        // 生成一个随机的8位密码
        return "Pwd" + System.currentTimeMillis() % 100000;
    }

}
