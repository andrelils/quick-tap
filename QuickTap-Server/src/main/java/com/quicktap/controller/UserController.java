package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.dto.UserDTO;
import com.quicktap.dto.UserRegisterRequest;
import com.quicktap.security.SecurityUtil;
import com.quicktap.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * C 端用户 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 用户注册
     * 对应 Node: POST /api/user/register
     */
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("用户注册请求 | phone: {}", request.getPhone());

        Map<String, Object> result = userService.register(request);
        return ApiResponse.success("注册成功", result);
    }

    /**
     * 获取用户信息
     * 对应 Node: GET /api/user/info
     */
    @GetMapping("/info")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<UserDTO> getUserInfo() {
        Long userId = securityUtil.getCurrentUserId();
        log.info("获取用户信息 | userId: {}", userId);

        UserDTO result = userService.getUserInfo(userId);
        return ApiResponse.success("用户信息获取成功", result);
    }

    /**
     * 更新用户信息
     * 对应 Node: PUT /api/user/info
     */
    @PutMapping("/info")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<UserDTO> updateUserInfo(@Valid @RequestBody UserRegisterRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        log.info("更新用户信息 | userId: {}", userId);

        UserDTO result = userService.updateUserInfo(userId, request);
        return ApiResponse.success("用户信息更新成功", result);
    }

    /**
     * 绑定电话号码
     * 对应 Node: POST /api/user/register-bind
     */
    @PostMapping("/register-bind")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<UserDTO> bindPhone(@RequestParam String phone) {
        Long userId = securityUtil.getCurrentUserId();
        log.info("绑定用户电话 | userId: {} | phone: {}", userId, phone);

        UserDTO result = userService.bindPhone(userId, phone);
        return ApiResponse.success("电话号码绑定成功", result);
    }
}
