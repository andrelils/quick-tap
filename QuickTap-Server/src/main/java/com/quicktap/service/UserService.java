package com.quicktap.service;

import com.quicktap.dto.UserDTO;
import com.quicktap.dto.UserRegisterRequest;
import com.quicktap.dto.UserLoginRequest;
import com.quicktap.entity.User;
import com.quicktap.exception.BusinessException;
import com.quicktap.mapper.UserMapper;
import com.quicktap.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * C 端用户 Service
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
                throw new BusinessException("该用户名已被注册");
            }
        }

        // 检查电话是否已注册（如果提供）
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            User existing = userMapper.selectByPhone(request.getPhone());
            if (existing != null) {
                throw new BusinessException("该电话号码已被注册");
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
            throw new BusinessException("用户不存在");
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
            throw new BusinessException("用户不存在");
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            // 检查电话是否已被其他用户使用
            User existingPhone = userMapper.selectByPhone(request.getPhone());
            if (existingPhone != null && !existingPhone.getId().equals(userId)) {
                throw new BusinessException("该电话号码已被使用");
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
            throw new BusinessException("用户不存在");
        }

        // 检查电话是否已被使用
        User existingPhone = userMapper.selectByPhone(phone);
        if (existingPhone != null && !existingPhone.getId().equals(userId)) {
            throw new BusinessException("该电话号码已被使用");
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
}
