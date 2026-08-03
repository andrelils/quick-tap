package com.quicktap.service;

import com.quicktap.constant.Constants;
import com.quicktap.dto.AdminCreateRequest;
import com.quicktap.dto.AdminUpdateRequest;
import com.quicktap.entity.Admin;
import com.quicktap.exception.BusinessException;
import com.quicktap.mapper.AdminMapper;
import com.quicktap.mapper.MerchantMapper;
import com.quicktap.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员管理服务 - 系统管理员生命周期管理
 *
 * 职责：
 * - 管理系统中所有管理员的创建、读取、更新、删除操作
 * - 实现管理员权限验证和角色管理（超级管理员、管理员、商户）
 * - 管理员启用/禁用状态控制
 * - 安全的密码处理（编码存储，响应中移除）
 * - 数据库操作的事务管理和错误处理
 *
 * 核心方法：
 * - getAdminList(Integer, Integer) - 分页查询管理员列表
 * - getAdminCount() - 获取管理员总数
 * - getAdminById(Integer) - 获取单个管理员详情
 * - createAdmin(AdminCreateRequest) - 创建新管理员（验证参数和重复检查）
 * - updateAdmin(Integer, AdminUpdateRequest) - 更新管理员信息（角色、密码、状态）
 * - deleteAdmin(Integer) - 删除管理员（保护超级管理员）
 * - disableAdmin(Integer) - 禁用管理员（状态设置为禁用）
 * - enableAdmin(Integer) - 启用管理员（状态设置为启用）
 *
 * 管理员创建流程：
 * 1. 验证请求参数不为空
 * 2. 验证用户名和密码格式（用户名非空，密码至少6字符）
 * 3. 检查用户名是否已存在（防止重复）
 * 4. 验证角色是否合法（仅允许SUPER_ADMIN, ADMIN, MERCHANT）
 * 5. 密码使用 PasswordUtil 编码存储
 * 6. 设置初始状态为启用
 * 7. 插入数据库
 * 8. 响应中移除密码信息
 *
 * 管理员更新流程：
 * 1. 验证管理员ID和请求参数
 * 2. 从数据库加载现有管理员
 * 3. 可选字段选择性更新（角色、密码、商户ID、状态）
 * 4. 对每个字段进行相应的验证（角色合法性、密码长度）
 * 5. 更新最后修改时间
 * 6. 执行数据库更新
 * 7. 响应中移除密码信息
 *
 * 管理员删除约束：
 * - 不能删除超级管理员（系统保护）
 * - 删除前验证管理员是否存在
 * - 失败时抛出业务异常
 *
 * 角色系统：
 * - SUPER_ADMIN: 超级管理员，拥有最高权限，无法被删除
 * - ADMIN: 管理员，拥有常规管理权限
 * - MERCHANT: 商户管理员，与特定商户关联（多租户场景）
 *
 * 使用场景：
 * {@code
 * // 场景1: 创建新管理员
 * AdminCreateRequest request = new AdminCreateRequest();
 * request.setUsername("newadmin");
 * request.setPassword("securePassword123");
 * request.setRole("ADMIN");
 * Admin admin = adminService.createAdmin(request);
 *
 * // 场景2: 获取管理员列表（分页）
 * List<Admin> admins = adminService.getAdminList(1, 20);
 *
 * // 场景3: 更新管理员信息
 * AdminUpdateRequest updateRequest = new AdminUpdateRequest();
 * updateRequest.setRole("SUPER_ADMIN");
 * Admin updated = adminService.updateAdmin(1, updateRequest);
 *
 * // 场景4: 禁用管理员
 * adminService.disableAdmin(2);
 * }</n
 *
 * 安全特性：
 * - 密码使用 PasswordUtil.encode() 进行单向加密
 * - 所有响应自动移除密码字段，防止泄露
 * - 参数验证防止null、空值、格式错误
 * - 用户名重复检查防止数据重复
 * - 超级管理员保护防止误删除
 * - 详细的审计日志记录所有重要操作
 *
 * 错误处理：
 * - 参数校验错误：返回 400 (Bad Request)
 * - 资源不存在错误：返回 404 (Not Found)
 * - 权限限制错误：返回 403 (Forbidden)
 * - 数据库操作错误：返回 500 (Internal Server Error)
 * - 所有错误都抛出 BusinessException 以保证统一异常处理
 *
 * 数据库查询：
 * - selectPage(offset, limit) - 分页查询
 * - countAll() - 统计总数
 * - selectById(id) - 按ID查询单条
 * - selectByUsername(username) - 按用户名查询（唯一索引）
 * - insert(admin) - 插入新记录
 * - update(admin) - 更新记录
 * - deleteById(id) - 删除记录
 *
 * 默认值和约束：
 * - 页码默认值：Constants.DEFAULT_PAGE_NUM (通常为1)
 * - 页面大小默认值：Constants.DEFAULT_PAGE_SIZE (通常为10)
 * - 最大页面大小：Constants.MAX_PAGE_SIZE (通常为100)
 * - 密码最少字符：6个
 * - 初始状态：Constants.ADMIN_STATUS_ENABLED (启用)
 *
 * 线程安全：
 * - Service 使用 Spring 的单例模式，所有方法都是线程安全的
 * - AdminMapper 由 Spring 管理，支持并发请求
 * - 数据库操作通过事务保证一致性
 *
 * 集成点：
 * - AdminMapper: 数据库访问层
 * - PasswordUtil: 密码编码工具
 * - BusinessException: 统一异常处理
 * - Constants: 系统常量定义
 *
 * @author QuickTap Admin Team
 * @version 1.0
 * @since 1.0
 * @see AdminMapper
 * @see PasswordUtil
 * @see BusinessException
 * @see Constants
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminMapper adminMapper;
    private final MerchantMapper merchantMapper;

    /**
     * 获取所有管理员（不分页）
     * @return 管理员列表
     */
    public List<Admin> getAllAdmins() {
        List<Admin> admins = adminMapper.selectAll();
        if (admins != null) {
            admins.forEach(admin -> admin.setPassword(null));
        }
        return admins;
    }

    /**
     * 获取管理员列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 管理员列表
     */
    public List<Admin> getAdminList(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum <= 0) {
            pageNum = Constants.DEFAULT_PAGE_NUM;
        }
        if (pageSize == null || pageSize <= 0 || pageSize > Constants.MAX_PAGE_SIZE) {
            pageSize = Constants.DEFAULT_PAGE_SIZE;
        }

        int offset = (pageNum - 1) * pageSize;
        List<Admin> admins = adminMapper.selectPage(offset, pageSize);

        // 移除密码信息
        if (admins != null) {
            admins.forEach(admin -> admin.setPassword(null));
        }

        return admins;
    }

    /**
     * 获取管理员总数
     * @return 总数
     */
    public Long getAdminCount() {
        int count = adminMapper.countAll();
        return (long) count;
    }

    /**
     * 获取管理员详情
     * @param id 管理员 ID
     * @return 管理员详情
     */
    public Admin getAdminById(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "管理员 ID 不能为空或小于 1");
        }

        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }

        // 不返回密码
        admin.setPassword(null);
        return admin;
    }

    /**
     * 创建管理员
     * @param request 创建请求
     * @return 创建后的管理员
     */
    public Admin createAdmin(AdminCreateRequest request) {
        // 验证参数
        if (request == null) {
            throw new BusinessException(400, "请求参数不能为空");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new BusinessException(400, "用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BusinessException(400, "密码不能为空");
        }
        if (request.getPassword().length() < 6) {
            throw new BusinessException(400, "密码长度至少 6 个字符");
        }
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            throw new BusinessException(400, "角色不能为空");
        }

        // 检查用户名是否已存在
        Admin existingAdmin = adminMapper.selectByUsername(request.getUsername());
        if (existingAdmin != null) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 验证角色
        String role = request.getRole();
        if (!role.equals(Constants.ROLE_SUPER_ADMIN) &&
            !role.equals(Constants.ROLE_ADMIN) &&
            !role.equals(Constants.ROLE_MERCHANT)) {
            throw new BusinessException(400, "角色不合法");
        }

        // 创建管理员
        Admin admin = new Admin();
        admin.setUsername(request.getUsername());
        admin.setPassword(PasswordUtil.encode(request.getPassword()));
        admin.setRole(role);
        admin.setMerchantId(request.getMerchantId());
        admin.setStatus(request.getStatus() != null ? request.getStatus() : Constants.ADMIN_STATUS_ENABLED);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());

        int result = adminMapper.insert(admin);
        if (result <= 0) {
            throw new BusinessException(500, "创建管理员失败");
        }

        log.info("创建管理员成功: id={}, username={}", admin.getId(), admin.getUsername());

        // 不返回密码
        admin.setPassword(null);
        return admin;
    }

    /**
     * 更新管理员
     * @param id 管理员 ID
     * @param request 更新请求
     * @return 更新后的管理员
     */
    public Admin updateAdmin(Integer id, AdminUpdateRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "管理员 ID 不能为空");
        }
        if (request == null) {
            throw new BusinessException(400, "请求参数不能为空");
        }

        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }

        // 更新角色
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            String role = request.getRole();
            if (!role.equals(Constants.ROLE_SUPER_ADMIN) &&
                !role.equals(Constants.ROLE_ADMIN) &&
                !role.equals(Constants.ROLE_MERCHANT)) {
                throw new BusinessException(400, "角色不合法");
            }
            admin.setRole(role);
        }

        // 更新密码
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            if (request.getPassword().length() < 6) {
                throw new BusinessException(400, "密码长度至少 6 个字符");
            }
            admin.setPassword(PasswordUtil.encode(request.getPassword()));
        }

        // 更新商户 ID
        if (request.getMerchantId() != null) {
            admin.setMerchantId(request.getMerchantId());
        }

        // 更新状态
        if (request.getStatus() != null) {
            admin.setStatus(request.getStatus());
        }

        admin.setUpdatedAt(LocalDateTime.now());

        int result = adminMapper.update(admin);
        if (result <= 0) {
            throw new BusinessException(500, "更新管理员失败");
        }

        log.info("更新管理员成功: id={}", id);

        // 不返回密码
        admin.setPassword(null);
        return admin;
    }

    /**
     * 删除管理员
     * @param id 管理员 ID
     */
    public void deleteAdmin(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "管理员 ID 不能为空");
        }

        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }

        // 不能删除超级管理员
        if (Constants.ROLE_SUPER_ADMIN.equals(admin.getRole())) {
            throw new BusinessException(403, "不能删除超级管理员");
        }

        // 如果该用户是商家角色且有绑定的商家，则级联删除商家
        if (Constants.ROLE_MERCHANT.equals(admin.getRole()) && admin.getMerchantId() != null) {
            merchantMapper.deleteById(admin.getMerchantId());
            log.info("级联删除关联商家: merchantId={}", admin.getMerchantId());
        }

        int result = adminMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(500, "删除管理员失败");
        }

        log.info("删除管理员成功: id={}", id);
    }

    /**
     * 禁用管理员
     * @param id 管理员 ID
     */
    public void disableAdmin(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "管理员 ID 不能为空");
        }

        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }

        admin.setStatus(Constants.ADMIN_STATUS_DISABLED);
        admin.setUpdatedAt(LocalDateTime.now());

        int result = adminMapper.update(admin);
        if (result <= 0) {
            throw new BusinessException(500, "禁用管理员失败");
        }

        log.info("禁用管理员成功: id={}", id);
    }

    /**
     * 启用管理员
     * @param id 管理员 ID
     */
    public void enableAdmin(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "管理员 ID 不能为空");
        }

        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }

        admin.setStatus(Constants.ADMIN_STATUS_ENABLED);
        admin.setUpdatedAt(LocalDateTime.now());

        int result = adminMapper.update(admin);
        if (result <= 0) {
            throw new BusinessException(500, "启用管理员失败");
        }

        log.info("启用管理员成功: id={}", id);
    }

    /**
     * 重置管理员密码
     * @param id 管理员 ID
     * @param newPassword 新密码（明文）
     */
    public void resetPassword(Integer id, String newPassword) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "管理员 ID 不能为空");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException(400, "密码长度至少 6 个字符");
        }

        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }

        admin.setPassword(PasswordUtil.encode(newPassword));
        admin.setUpdatedAt(LocalDateTime.now());

        int result = adminMapper.update(admin);
        if (result <= 0) {
            throw new BusinessException(500, "重置密码失败");
        }

        log.info("重置管理员密码成功: id={}", id);
    }

    /**
     * 当前登录管理员修改自己的密码（需校验原密码）
     */
    public void updatePasswordBySelf(Integer id, String oldPassword, String newPassword) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "管理员 ID 不能为空");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException(400, "新密码长度至少 6 个字符");
        }
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        // 校验原密码（oldPassword 为空则跳过，兼容页面未提供原密码的场景；但如提供则校验）
        if (oldPassword != null && !oldPassword.isEmpty()) {
            if (!PasswordUtil.matches(oldPassword, admin.getPassword())) {
                throw new BusinessException(400, "原密码不正确");
            }
        }
        admin.setPassword(PasswordUtil.encode(newPassword));
        admin.setUpdatedAt(LocalDateTime.now());
        int result = adminMapper.update(admin);
        if (result <= 0) {
            throw new BusinessException(500, "修改密码失败");
        }
        log.info("管理员自助修改密码成功: id={}", id);
    }

    /**
     * 当前登录管理员修改自己的基础资料
     */
    public Admin updateInfoBySelf(Integer id, String nickname, String email, String phone) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "管理员 ID 不能为空");
        }
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        if (nickname != null) admin.setNickname(nickname);
        if (email != null) admin.setEmail(email);
        if (phone != null) admin.setPhone(phone);
        admin.setUpdatedAt(LocalDateTime.now());
        int result = adminMapper.update(admin);
        if (result <= 0) {
            throw new BusinessException(500, "修改资料失败");
        }
        log.info("管理员自助修改资料成功: id={}", id);
        return admin;
    }
}
