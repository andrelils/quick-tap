package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.dto.UserDTO;
import com.quicktap.dto.UserRegisterRequest;
import com.quicktap.entity.User;
import com.quicktap.security.SecurityUtil;
import com.quicktap.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 单元测试
 * 重点验证 uploadUserAvatar 接口上传头像后是否正确保存到数据库
 */
@Slf4j
@WebMvcTest(UserController.class)
@DisplayName("用户控制器测试")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private SecurityUtil securityUtil;

    private Long testUserId;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
        testUserDTO = UserDTO.builder()
                .id(testUserId)
                .username("testuser")
                .nickname("测试用户")
                .phone("13800138000")
                .avatar("/uploads/avatars/uuid-test.jpg")
                .status(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("上传用户头像 - 成功上传并保存到数据库")
    void testUploadUserAvatarSuccess() throws Exception {
        // 模拟当前用户
        when(securityUtil.getCurrentUserId()).thenReturn(testUserId);

        // 创建测试文件
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        // 模拟 UserService 的 updateUserInfo 方法（这是关键修复点）
        // 验证该方法是否被调用，即数据库是否被更新
        doAnswer(invocation -> {
            Long userId = invocation.getArgument(0);
            UserRegisterRequest request = invocation.getArgument(1);
            assertEquals(testUserId, userId, "用户ID应该匹配");
            assertNotNull(request.getAvatar(), "头像URL不应该为空");
            assertTrue(request.getAvatar().contains("/uploads/avatars/"), "头像URL应该包含正确的路径");
            log.info("✅ 验证：UserService.updateUserInfo 被调用，头像已保存到数据库");
            return testUserDTO;
        }).when(userService).updateUserInfo(eq(testUserId), any(UserRegisterRequest.class));

        // 执行上传请求
        MvcResult result = mockMvc.perform(
                multipart("/api/user/avatar")
                        .file(file)
                        .header("Authorization", "Bearer test-token")
        )
                .andExpect(status().isOk())
                .andReturn();

        // 验证返回的响应
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("\"code\":0"), "响应代码应该是0（成功）");
        assertTrue(content.contains("头像上传成功"), "响应应该包含成功信息");
        assertTrue(content.contains("/uploads/avatars/"), "响应应该包含文件URL");
        log.info("✅ 验证：API 响应包含正确的头像URL");

        // 【关键验证】确保 updateUserInfo 被调用了一次
        verify(userService, times(1)).updateUserInfo(eq(testUserId), any(UserRegisterRequest.class));
        log.info("✅ 验证：updateUserInfo 方法被调用恰好一次，确认数据库已更新");
    }

    @Test
    @DisplayName("上传用户头像 - 文件为空时应该返回错误")
    void testUploadUserAvatarWithNullFile() throws Exception {
        when(securityUtil.getCurrentUserId()).thenReturn(testUserId);

        mockMvc.perform(
                multipart("/api/user/avatar")
                        .header("Authorization", "Bearer test-token")
        )
                .andExpect(status().isBadRequest())
                .andReturn();

        // 验证：没有调用数据库更新方法
        verify(userService, never()).updateUserInfo(anyLong(), any(UserRegisterRequest.class));
        log.info("✅ 验证：文件为空时，updateUserInfo 方法没有被调用");
    }

    @Test
    @DisplayName("上传用户头像 - 文件过大（>5MB）时应该返回错误")
    void testUploadUserAvatarWithLargeFile() throws Exception {
        when(securityUtil.getCurrentUserId()).thenReturn(testUserId);

        // 创建大于 5MB 的文件
        byte[] largeContent = new byte[6 * 1024 * 1024 + 1]; // 6MB + 1byte
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large-avatar.jpg",
                "image/jpeg",
                largeContent
        );

        mockMvc.perform(
                multipart("/api/user/avatar")
                        .file(file)
                        .header("Authorization", "Bearer test-token")
        )
                .andExpect(status().isBadRequest())
                .andReturn();

        // 验证：没有调用数据库更新方法
        verify(userService, never()).updateUserInfo(anyLong(), any(UserRegisterRequest.class));
        log.info("✅ 验证：文件过大时，updateUserInfo 方法没有被调用");
    }

    @Test
    @DisplayName("上传用户头像 - 验证修复前后的行为对比")
    void testAvatarUploadDatabasePersistenceComparison() throws Exception {
        when(securityUtil.getCurrentUserId()).thenReturn(testUserId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        // 跟踪 updateUserInfo 被调用的次数和参数
        when(userService.updateUserInfo(eq(testUserId), any(UserRegisterRequest.class)))
                .thenReturn(testUserDTO);

        mockMvc.perform(
                multipart("/api/user/avatar")
                        .file(file)
                        .header("Authorization", "Bearer test-token")
        )
                .andExpect(status().isOk())
                .andReturn();

        // 【修复验证】确保数据库已被更新
        verify(userService, times(1)).updateUserInfo(
                eq(testUserId),
                argThat(request -> {
                    // 验证：UserRegisterRequest 的 avatar 字段已被设置
                    assertNotNull(request.getAvatar(), "avatar 字段不应该为空");
                    assertTrue(request.getAvatar().startsWith("/uploads/avatars/"), "avatar 路径应该正确");
                    log.info("✅ 修复验证：avatar URL 已正确传递给 updateUserInfo: {}", request.getAvatar());
                    return true;
                })
        );

        log.info("✅ 修复对比验证完成：上传头像时现在会自动保存到数据库");
    }

    @Test
    @DisplayName("确保修复后的响应包含必要的信息")
    void testAvatarUploadResponseContent() throws Exception {
        when(securityUtil.getCurrentUserId()).thenReturn(testUserId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        when(userService.updateUserInfo(eq(testUserId), any(UserRegisterRequest.class)))
                .thenReturn(testUserDTO);

        MvcResult result = mockMvc.perform(
                multipart("/api/user/avatar")
                        .file(file)
                        .header("Authorization", "Bearer test-token")
        )
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        // 验证响应结构
        assertTrue(content.contains("\"code\":0"), "成功响应应该有 code:0");
        assertTrue(content.contains("\"msg\":\"头像上传成功\""), "应该包含成功消息");
        assertTrue(content.contains("\"url\""), "应该包含 url 字段");
        assertTrue(content.contains("\"filename\""), "应该包含 filename 字段");
        assertTrue(content.contains("\"originalName\""), "应该包含 originalName 字段");
        assertTrue(content.contains("\"size\""), "应该包含 size 字段");

        log.info("✅ 响应内容验证通过，所有必要字段都存在");
    }
}
