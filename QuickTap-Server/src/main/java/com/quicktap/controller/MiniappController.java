package com.quicktap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quicktap.dto.ApiResponse;
import com.quicktap.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 小程序端（C 端）接口控制器
 * <p>
 * 路径前缀：/api/miniapp
 * 直接使用 JdbcTemplate 执行 SQL，统一返回 {@code ApiResponse}。
 * 公开接口由 SecurityConfig 放行；需登录接口不加 @PreAuthorize，由 SecurityConfig 控制。
 */
@Slf4j
@RestController
@RequestMapping("/api/miniapp")

public class MiniappController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    /** 图片 URL 补全前缀 */
    private static final String IMG_BASE = "http://154.8.138.48:3000";

    /** 日期时间格式化 */
    private static final String DT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    // =====================================================================================
    // 1. 商家相关
    // =====================================================================================

    /**
     * 1.1 检查二维码/设备是否已绑定商家
     * 公开接口。先查 qrcode 表(code=?, bind_status=1)，未命中再查 device 表(system_code=? 或 device_no=?, merchant_id 非空)
     */
    @GetMapping("/merchant/check-bind")
    public ApiResponse<Map<String, Object>> checkBind(@RequestParam("code") String code) {
        log.info("小程序-检查绑定 | code: {}", code);
        Long merchantId = null;

        // 1) 先查 qrcode 表（bind_status=1 表示已绑定）
        try {
            List<Map<String, Object>> qrRows = jdbcTemplate.queryForList(
                    "SELECT id, merchant_id FROM qrcode WHERE code = ? AND bind_status = 1 LIMIT 1", code);
            if (!qrRows.isEmpty()) {
                merchantId = toLong(qrRows.get(0).get("merchant_id"));
            }
        } catch (Exception e) {
            // bind_status 列可能不存在，降级到 device 表查询
            log.warn("查询 qrcode 表失败（可能缺少 bind_status 列），降级查询 device 表: {}", e.getMessage());
        }

        // 2) 未命中，查 device 表（system_code、device_no、或 url 中包含 code=xxx 参数）
        if (merchantId == null) {
            List<Map<String, Object>> devRows = jdbcTemplate.queryForList(
                    "SELECT id, merchant_id FROM device WHERE (system_code = ? OR device_no = ? OR url LIKE CONCAT('%code=', ?, '%')) AND merchant_id IS NOT NULL LIMIT 1",
                    code, code, code);
            if (!devRows.isEmpty()) {
                merchantId = toLong(devRows.get(0).get("merchant_id"));
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        if (merchantId != null) {
            // 查询商家基本信息
            List<Map<String, Object>> mRows = jdbcTemplate.queryForList(
                    "SELECT id, name, logo FROM merchant WHERE id = ? AND status = 1 LIMIT 1", merchantId);
            Map<String, Object> merchant = new LinkedHashMap<>();
            if (!mRows.isEmpty()) {
                Map<String, Object> m = mRows.get(0);
                merchant.put("id", m.get("id"));
                merchant.put("name", m.get("name"));
                merchant.put("logo", fullUrl(toStr(m.get("logo"))));
            } else {
                merchant.put("id", merchantId);
                merchant.put("name", null);
                merchant.put("logo", null);
            }
            result.put("bound", true);
            result.put("merchantId", merchantId);
            result.put("merchant", merchant);
        } else {
            result.put("bound", false);
            result.put("merchantId", null);
            result.put("merchant", null);
        }
        return ApiResponse.success(result);
    }

    /**
     * 1.2 获取商家详情信息
     * 公开接口。查 merchant 表(id=?, status=1)，banner_images/shop_images 解析为 JSON 数组并补全图片 URL
     */
    @GetMapping("/merchant/info/{merchantId}")
    public ApiResponse<Map<String, Object>> merchantInfo(@PathVariable("merchantId") Integer merchantId) {
        log.info("小程序-商家详情 | merchantId: {}", merchantId);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, name, logo, banner_images, shop_images, contact_name, contact_phone, " +
                        "boss_wechat, address, business_hours, wifi_name, wifi_password, status " +
                        "FROM merchant WHERE id = ? LIMIT 1", merchantId);
        if (rows.isEmpty()) {
            return ApiResponse.notFound("商家不存在或已停用");
        }
        Map<String, Object> m = rows.get(0);
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", m.get("id"));
        info.put("name", m.get("name"));
        info.put("logo", fullUrl(toStr(m.get("logo"))));
        info.put("bannerImages", parseUrlArray(toStr(m.get("banner_images"))));
        info.put("shopImages", parseUrlArray(toStr(m.get("shop_images"))));
        info.put("contactName", m.get("contact_name"));
        info.put("contactPhone", m.get("contact_phone"));
        info.put("bossWechat", m.get("boss_wechat"));
        info.put("address", m.get("address"));
        info.put("businessHours", m.get("business_hours"));
        info.put("wifiName", m.get("wifi_name"));
        info.put("wifiPassword", m.get("wifi_password"));
        // merchant 表无 description 列，按契约返回 null
        info.put("description", null);
        info.put("status", m.get("status"));
        return ApiResponse.success(info);
    }

    /**
     * 1.3 获取商家推广平台配置列表
     * 公开接口。JOIN merchant_promotion_config + promotion_platform
     */
    @GetMapping("/merchant/promotion")
    public ApiResponse<List<Map<String, Object>>> merchantPromotion(@RequestParam("merchantId") Integer merchantId) {
        log.info("小程序-商家推广配置列表 | merchantId: {}", merchantId);
        String sql = "SELECT mpc.id AS configId, mpc.merchant_id, mpc.platform_id, mpc.params, mpc.custom_name, " +
                "mpc.custom_icon, mpc.sort, mpc.status, pp.code, pp.name, pp.icon, pp.color, pp.description, " +
                "pp.jump_mode, pp.scheme_template, pp.web_url_template, pp.miniprogram_appid, " +
                "pp.miniprogram_path_template, pp.required_params, pp.optional_params " +
                "FROM merchant_promotion_config mpc " +
                "JOIN promotion_platform pp ON mpc.platform_id = pp.id " +
                "WHERE mpc.merchant_id = ? AND mpc.status = 1 AND pp.status = 1 " +
                "ORDER BY mpc.sort DESC, mpc.created_at DESC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, merchantId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("configId", r.get("configId"));
            item.put("merchantId", r.get("merchant_id"));
            item.put("platformId", r.get("platform_id"));
            item.put("params", safeParseJson(toStr(r.get("params"))));
            item.put("customName", r.get("custom_name"));
            item.put("customIcon", fullUrl(toStr(r.get("custom_icon"))));
            item.put("sort", r.get("sort"));
            item.put("status", r.get("status"));
            item.put("code", r.get("code"));
            item.put("name", r.get("name"));
            item.put("icon", fullUrl(toStr(r.get("icon"))));
            item.put("color", r.get("color"));
            item.put("description", r.get("description"));
            item.put("jumpMode", r.get("jump_mode"));
            item.put("schemeTemplate", r.get("scheme_template"));
            item.put("webUrlTemplate", r.get("web_url_template"));
            item.put("miniprogramAppid", r.get("miniprogram_appid"));
            item.put("miniprogramPathTemplate", r.get("miniprogram_path_template"));
            item.put("requiredParams", safeParseJson(toStr(r.get("required_params"))));
            item.put("optionalParams", safeParseJson(toStr(r.get("optional_params"))));
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    /**
     * 1.4 获取商家 WiFi 信息
     * 公开接口
     */
    @GetMapping("/merchant/wifi")
    public ApiResponse<Map<String, Object>> merchantWifi(@RequestParam("merchantId") Integer merchantId) {
        log.info("小程序-商家 WiFi | merchantId: {}", merchantId);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, name, wifi_name, wifi_password, wifi_encryption FROM merchant WHERE id = ? AND status = 1 LIMIT 1", merchantId);
        if (rows.isEmpty()) {
            return ApiResponse.notFound("商家不存在或已停用");
        }
        Map<String, Object> m = rows.get(0);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ssid", m.get("wifi_name"));
        result.put("password", m.get("wifi_password"));
        result.put("encryption", m.get("wifi_encryption"));
        result.put("merchantName", m.get("name"));
        return ApiResponse.success(result);
    }

    /**
     * 1.5 商家自助入驻
     * 公开接口。事务性操作：校验 -> 建 merchant -> 建 admin -> 绑定 device/qrcode
     */
    @PostMapping("/merchant/register")
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Map<String, Object>> merchantRegister(@RequestBody Map<String, Object> body) {
        String username = toStr(body.get("username"));
        String password = toStr(body.get("password"));
        String nickname = toStr(body.get("nickname"));
        String code = toStr(body.get("code"));
        String merchantName = toStr(body.get("merchantName"));
        String contactName = toStr(body.get("contactName"));
        String contactPhone = toStr(body.get("contactPhone"));
        log.info("小程序-商家自助入驻 | username: {} | code: {}", username, code);

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return ApiResponse.badRequest("用户名或密码不能为空");
        }

        // 1) 检查 admin 是否已存在 username
        List<Map<String, Object>> existAdmin = jdbcTemplate.queryForList(
                "SELECT id FROM admin WHERE username = ? LIMIT 1", username);
        if (!existAdmin.isEmpty()) {
            return ApiResponse.badRequest("该用户名已被注册");
        }

        // 2) 检查 qrcode/device 是否已绑定
        Long boundMerchantId = resolveBoundMerchantId(code);
        if (boundMerchantId != null) {
            return ApiResponse.badRequest("该二维码/设备已被绑定");
        }

        // 3) INSERT merchant (status=0, audit_status=0)
        KeyHolder merchantKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO merchant (name, contact_name, contact_phone, audit_status, status, created_at, updated_at) " +
                            "VALUES (?, ?, ?, 0, 0, NOW(), NOW())",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, merchantName);
            ps.setString(2, contactName);
            ps.setString(3, contactPhone);
            return ps;
        }, merchantKeyHolder);
        Long newMerchantId = merchantKeyHolder.getKey() != null ? merchantKeyHolder.getKey().longValue() : null;
        if (newMerchantId == null) {
            throw new RuntimeException("创建商家失败");
        }

        // 4) 生成 user_code: U + yyMMdd + 4 位随机
        String userCode = generateUserCode();

        // 5) bcrypt 密码
        String encodedPassword = passwordEncoder.encode(password);

        // 6) INSERT admin (role='merchant', merchant_id=新 id, status=0 待审核)
        //    商家审核通过后由 MerchantService.approveMerchant 同步启用 admin 账号
        jdbcTemplate.update(
                "INSERT INTO admin (username, user_code, password, nickname, role, merchant_id, status, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, 'merchant', ?, 0, NOW(), NOW())",
                username, userCode, encodedPassword, nickname != null ? nickname : merchantName, newMerchantId);

        // 7) 绑定 device/qrcode 的 merchant_id
        bindCodeToMerchant(code, newMerchantId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("merchantId", newMerchantId);
        result.put("username", username);
        return ApiResponse.success("入驻申请提交成功", result);
    }

    // =====================================================================================
    // 2. 优惠券相关
    // =====================================================================================

    /**
     * 2.1 优惠券列表（可按商家过滤）
     * 公开接口
     */
    @GetMapping("/coupon/list")
    public ApiResponse<List<Map<String, Object>>> couponList(@RequestParam(value = "merchantId", required = false) Integer merchantId) {
        log.info("小程序-优惠券列表 | merchantId: {}", merchantId);
        StringBuilder sql = new StringBuilder(
                "SELECT c.*, m.name AS merchant_name FROM coupon c " +
                        "LEFT JOIN merchant m ON c.merchant_id = m.id " +
                        "WHERE c.status = 1 AND c.remain_count > 0 " +
                        "AND c.start_time <= NOW() AND c.end_time >= NOW() ");
        List<Object> args = new ArrayList<>();
        if (merchantId != null) {
            sql.append("AND c.merchant_id = ? ");
            args.add(merchantId);
        }
        sql.append("ORDER BY c.created_at DESC");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Integer total = toInt(r.get("total_count"));
            Integer remain = toInt(r.get("remain_count"));
            int issued = (total != null && remain != null) ? (total - remain) : 0;
            BigDecimal amount = (BigDecimal) r.get("amount");

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.get("id"));
            item.put("merchantId", r.get("merchant_id"));
            item.put("title", r.get("title"));
            item.put("name", r.get("title"));
            item.put("type", r.get("type"));
            item.put("amount", norm(amount));
            item.put("value", norm(amount));
            item.put("minAmount", norm(r.get("min_amount")));
            item.put("totalCount", total);
            item.put("remainCount", remain);
            item.put("issuedCount", issued);
            item.put("link", r.get("link"));
            item.put("startTime", norm(r.get("start_time")));
            item.put("endTime", norm(r.get("end_time")));
            item.put("status", r.get("status"));
            item.put("merchantName", r.get("merchant_name"));
            item.put("createdAt", norm(r.get("created_at")));
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    /**
     * 2.2 领取优惠券
     * 需登录。事务性：校验 -> 扣减 remain_count -> 写入 user_coupon
     */
    @PostMapping("/coupon/claim")
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Map<String, Object>> claimCoupon(@RequestBody Map<String, Object> body,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = currentUserId(principal);
        if (userId == null) {
            return ApiResponse.badRequest("用户未登录");
        }
        Object couponIdObj = body.get("couponId");
        if (couponIdObj == null) {
            return ApiResponse.badRequest("couponId 不能为空");
        }
        Long couponId = toLong(couponIdObj);
        log.info("小程序-领取优惠券 | userId: {} | couponId: {}", userId, couponId);

        // 1) 查 coupon
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, merchant_id, remain_count, status FROM coupon WHERE id = ? AND status = 1 LIMIT 1", couponId);
        if (rows.isEmpty()) {
            return ApiResponse.notFound("优惠券不存在或已停用");
        }
        Integer remain = toInt(rows.get(0).get("remain_count"));
        Long merchantId = toLong(rows.get(0).get("merchant_id"));
        if (remain == null || remain <= 0) {
            return ApiResponse.badRequest("优惠券已领完");
        }

        // 2) 扣减 remain_count
        jdbcTemplate.update("UPDATE coupon SET remain_count = remain_count - 1 WHERE id = ?", couponId);

        // 3) 写入 user_coupon（status=0 待使用；merchant_id 为 NOT NULL，故一并写入）
        jdbcTemplate.update(
                "INSERT INTO user_coupon (user_id, coupon_id, merchant_id, status, created_at, updated_at) " +
                        "VALUES (?, ?, ?, 0, NOW(), NOW())", userId, couponId, merchantId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("couponId", couponId);
        result.put("userId", userId);
        result.put("status", 0);
        return ApiResponse.success("领取成功", result);
    }

    /**
     * 2.3 我的优惠券
     * 需登录
     */
    @GetMapping("/coupon/my")
    public ApiResponse<List<Map<String, Object>>> myCoupons(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = currentUserId(principal);
        if (userId == null) {
            return ApiResponse.badRequest("用户未登录");
        }
        log.info("小程序-我的优惠券 | userId: {}", userId);
        String sql = "SELECT uc.id, uc.status, uc.created_at, c.title, c.type, c.amount, c.min_amount, " +
                "c.start_time, c.end_time, c.merchant_id, m.name AS merchant_name " +
                "FROM user_coupon uc " +
                "LEFT JOIN coupon c ON uc.coupon_id = c.id " +
                "LEFT JOIN merchant m ON c.merchant_id = m.id " +
                "WHERE uc.user_id = ? ORDER BY uc.created_at DESC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.get("id"));
            item.put("status", r.get("status"));
            item.put("couponId", r.get("coupon_id"));
            item.put("couponName", r.get("title"));
            item.put("couponCode", r.get("id"));
            item.put("discountValue", norm(r.get("amount")));
            item.put("minAmount", norm(r.get("min_amount")));
            item.put("startTime", norm(r.get("start_time")));
            item.put("endTime", norm(r.get("end_time")));
            item.put("validEndTime", norm(r.get("end_time")));
            item.put("merchantName", r.get("merchant_name"));
            item.put("createTime", norm(r.get("created_at")));
            item.put("description", "");
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    // =====================================================================================
    // 3. 设备相关
    // =====================================================================================

    /**
     * 3.1 扫码（记录扫码日志并返回设备/商家信息）
     * 需登录
     */
    @PostMapping("/device/scan")
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Map<String, Object>> deviceScan(@RequestBody Map<String, Object> body,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = currentUserId(principal);
        if (userId == null) {
            return ApiResponse.badRequest("用户未登录");
        }
        String deviceId = toStr(body.get("deviceId"));
        log.info("小程序-扫码 | userId: {} | deviceId: {}", userId, deviceId);

        Map<String, Object> device = findDeviceByIdOrNo(deviceId);
        if (device == null) {
            return ApiResponse.notFound("设备不存在");
        }
        Long dbDeviceId = toLong(device.get("id"));
        Long merchantId = toLong(device.get("merchant_id"));

        // 记录扫码日志
        jdbcTemplate.update(
                "INSERT INTO scan_log (user_id, device_id, merchant_id, created_at) VALUES (?, ?, ?, NOW())",
                userId, dbDeviceId, merchantId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("device", buildDeviceView(device));
        return ApiResponse.success(result);
    }

    /**
     * 3.2 绑定设备
     * 需登录
     */
    @PostMapping("/device/bind")
    public ApiResponse<Map<String, Object>> deviceBind(@RequestBody Map<String, Object> body,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = currentUserId(principal);
        if (userId == null) {
            // 兜底使用 body 中的 userId
            userId = toLong(body.get("userId"));
        }
        if (userId == null) {
            return ApiResponse.badRequest("用户未登录");
        }
        String deviceId = toStr(body.get("deviceId"));
        log.info("小程序-绑定设备 | userId: {} | deviceId: {}", userId, deviceId);

        Map<String, Object> device = findDeviceByIdOrNo(deviceId);
        if (device == null) {
            return ApiResponse.notFound("设备不存在");
        }
        Long dbDeviceId = toLong(device.get("id"));

        jdbcTemplate.update(
                "INSERT INTO user_device (user_id, device_id, created_at) VALUES (?, ?, NOW()) " +
                        "ON DUPLICATE KEY UPDATE created_at = NOW()", userId, dbDeviceId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("deviceId", dbDeviceId);
        result.put("bound", true);
        return ApiResponse.success("绑定成功", result);
    }

    /**
     * 3.3 设备详情
     * 需登录
     */
    @GetMapping("/device/info/{deviceNo}")
    public ApiResponse<Map<String, Object>> deviceInfo(@PathVariable("deviceNo") String deviceNo,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = currentUserId(principal);
        if (userId == null) {
            return ApiResponse.badRequest("用户未登录");
        }
        log.info("小程序-设备详情 | userId: {} | deviceNo: {}", userId, deviceNo);
        Map<String, Object> device = findDeviceByNo(deviceNo);
        if (device == null) {
            return ApiResponse.notFound("设备不存在");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("device", buildDeviceView(device));
        return ApiResponse.success(result);
    }

    // =====================================================================================
    // 4. 推广相关
    // =====================================================================================

    /** 推广配置 JOIN SQL（同时拉平台与优惠券） */
    private static final String PROMOTION_JOIN_SQL =
            "SELECT mpc.id, mpc.type, mpc.params, mpc.custom_name, mpc.custom_icon, mpc.sort, " +
                    "pp.id AS platform_id, pp.code, pp.name, pp.icon, pp.color, pp.description, pp.jump_mode, " +
                    "pp.scheme_template, pp.web_url_template, pp.miniprogram_appid, pp.miniprogram_path_template, " +
                    "pp.required_params, pp.optional_params, " +
                    "c.id AS coupon_id, c.title AS coupon_name, c.type AS coupon_type, c.amount AS coupon_value, " +
                    "c.min_amount AS coupon_threshold, c.total_count AS coupon_total_count, " +
                    "c.remain_count AS coupon_remain_count, c.start_time AS coupon_valid_start, " +
                    "c.end_time AS coupon_valid_end, c.status AS coupon_status, c.link AS coupon_link " +
                    "FROM merchant_promotion_config mpc " +
                    "LEFT JOIN promotion_platform pp ON mpc.platform_id = pp.id AND mpc.type = 'platform' " +
                    "LEFT JOIN coupon c ON mpc.coupon_id = c.id AND mpc.type = 'coupon' ";

    /**
     * 4.1 商家推广配置（平台 + 优惠券，分流返回）
     * 公开接口
     */
    @GetMapping("/promotion/platforms")
    public ApiResponse<Map<String, Object>> promotionPlatforms(@RequestParam("merchantId") Integer merchantId) {
        log.info("小程序-推广配置列表 | merchantId: {}", merchantId);
        try {
            String sql = PROMOTION_JOIN_SQL +
                    "WHERE mpc.merchant_id = ? AND mpc.status = 1 ORDER BY mpc.sort DESC, mpc.created_at DESC";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, merchantId);

            List<Map<String, Object>> platforms = new ArrayList<>();
            List<Map<String, Object>> coupons = new ArrayList<>();
            for (Map<String, Object> r : rows) {
                String type = toStr(r.get("type"));
                if ("coupon".equals(type)) {
                    Integer couponStatus = toInt(r.get("coupon_status"));
                    if (couponStatus == null || couponStatus != 1) {
                        continue;
                    }
                    coupons.add(buildCouponConfigItem(r));
                } else {
                    Integer platformId = toInt(r.get("platform_id"));
                    if (platformId == null) {
                        continue;
                    }
                    platforms.add(buildPlatformConfigItem(r));
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("merchantId", merchantId);
            result.put("platforms", platforms);
            result.put("coupons", coupons);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("查询推广配置失败 | merchantId: {} | error: {}", merchantId, e.getMessage(), e);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("merchantId", merchantId);
            result.put("platforms", new ArrayList<>());
            result.put("coupons", new ArrayList<>());
            return ApiResponse.success(result);
        }
    }

    /**
     * 4.2 单条推广配置详情
     * 公开接口。根据 type 分流返回
     */
    @GetMapping("/promotion/platform/{id}")
    public ApiResponse<Map<String, Object>> promotionPlatformDetail(@PathVariable("id") Integer id) {
        log.info("小程序-推广配置详情 | id: {}", id);
        String sql = PROMOTION_JOIN_SQL + "WHERE mpc.id = ? AND mpc.status = 1 LIMIT 1";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, id);
        if (rows.isEmpty()) {
            return ApiResponse.notFound("推广配置不存在");
        }
        Map<String, Object> r = rows.get(0);
        String type = toStr(r.get("type"));
        Map<String, Object> result = new LinkedHashMap<>();
        if ("coupon".equals(type)) {
            result.put("type", "coupon");
            result.put("configId", r.get("id"));
            result.put("coupon", buildCouponConfigItem(r));
        } else {
            result.put("type", "platform");
            result.put("configId", r.get("id"));
            result.put("platform", buildPlatformConfigItem(r));
        }
        return ApiResponse.success(result);
    }

    /**
     * 4.3 记录推广点击日志
     * 公开接口（可选认证）
     */
    @PostMapping("/promotion/log")
    public ApiResponse<Map<String, Object>> promotionLog(@RequestBody Map<String, Object> body,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = currentUserId(principal);
        Long platformId = toLong(body.get("platformId"));
        Long merchantId = toLong(body.get("merchantId"));
        Long deviceId = toLong(body.get("deviceId"));
        log.info("小程序-推广点击日志 | userId: {} | platformId: {} | merchantId: {}", userId, platformId, merchantId);

        if (platformId == null || merchantId == null) {
            return ApiResponse.badRequest("platformId 与 merchantId 不能为空");
        }
        jdbcTemplate.update(
                "INSERT INTO promotion_click_log (user_id, platform_id, merchant_id, device_id, created_at) " +
                        "VALUES (?, ?, ?, ?, NOW())", userId, platformId, merchantId, deviceId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("logged", true);
        return ApiResponse.success(result);
    }

    // =====================================================================================
    // 5. 用户相关
    // =====================================================================================

    /**
     * 5.1 推荐人列表
     * 公开接口
     */
    @GetMapping("/user/referrer/list")
    public ApiResponse<List<Map<String, Object>>> referrerList() {
        log.info("小程序-推荐人列表");
        String sql = "SELECT id, username, nickname, user_code, avatar, role, phone FROM admin " +
                "WHERE role IN ('super_admin', 'admin') AND status = 1 " +
                "AND user_code IS NOT NULL AND user_code <> '' " +
                "ORDER BY FIELD(role, 'super_admin', 'admin'), created_at ASC LIMIT 50";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.get("id"));
            item.put("username", r.get("username"));
            item.put("nickname", r.get("nickname"));
            item.put("userCode", r.get("user_code"));
            item.put("avatar", fullUrl(toStr(r.get("avatar"))));
            item.put("role", r.get("role"));
            item.put("phone", r.get("phone"));
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    /**
     * 5.2 注册并绑定（完善资料 + 绑定设备/商家）
     * 需登录。事务性
     */
    @PostMapping("/user/register-bind")
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Map<String, Object>> registerBind(@RequestBody Map<String, Object> body,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = currentUserId(principal);
        if (userId == null) {
            return ApiResponse.badRequest("用户未登录");
        }
        String phone = toStr(body.get("phone"));
        String nickname = toStr(body.get("nickname"));
        String avatar = toStr(body.get("avatar"));
        String deviceNo = toStr(body.get("deviceNo"));
        String systemCode = toStr(body.get("systemCode"));
        Long bodyMerchantId = toLong(body.get("merchantId"));
        String referrerCode = toStr(body.get("referrerCode"));
        String code = toStr(body.get("code"));
        log.info("小程序-注册绑定 | userId: {} | phone: {}", userId, phone);

        // 1) UPDATE user 资料
        jdbcTemplate.update(
                "UPDATE user SET phone = ?, nickname = ?, avatar = ?, updated_at = NOW() WHERE id = ?",
                phone, nickname, avatar, userId);

        // 2) 查 qrcode/device 获取 merchantId
        Long merchantId = bodyMerchantId != null ? bodyMerchantId : resolveBoundMerchantId(code);
        if (merchantId == null && (systemCode != null || deviceNo != null)) {
            String key = systemCode != null ? systemCode : deviceNo;
            Map<String, Object> device = findDeviceByIdOrNo(key);
            if (device != null) {
                merchantId = toLong(device.get("merchant_id"));
            }
        }

        // 3) INSERT IGNORE user_device（若拿到设备）
        if (systemCode != null || deviceNo != null) {
            String key = systemCode != null ? systemCode : deviceNo;
            Map<String, Object> device = findDeviceByIdOrNo(key);
            if (device != null) {
                Long dbDeviceId = toLong(device.get("id"));
                jdbcTemplate.update(
                        "INSERT IGNORE INTO user_device (user_id, device_id, created_at) VALUES (?, ?, NOW())",
                        userId, dbDeviceId);
            }
        }

        // 4) INSERT/UPDATE user_merchant（若拿到商家）
        if (merchantId != null) {
            jdbcTemplate.update(
                    "INSERT INTO user_merchant (user_id, merchant_id, created_at, updated_at) VALUES (?, ?, NOW(), NOW()) " +
                            "ON DUPLICATE KEY UPDATE updated_at = NOW()", userId, merchantId);
        }

        // 5) UPDATE device/qrcode 绑定 merchant_id
        if (merchantId != null) {
            bindCodeToMerchant(code, merchantId);
            if (systemCode != null || deviceNo != null) {
                String key = systemCode != null ? systemCode : deviceNo;
                jdbcTemplate.update("UPDATE device SET merchant_id = ? WHERE system_code = ? OR device_no = ? OR url LIKE CONCAT('%code=', ?, '%')",
                        merchantId, key, key, key);
            }
        }

        // 6) 处理 referrerCode
        Long referrerId = null;
        if (referrerCode != null && !referrerCode.isEmpty()) {
            List<Map<String, Object>> refRows = jdbcTemplate.queryForList(
                    "SELECT id FROM admin WHERE user_code = ? LIMIT 1", referrerCode);
            if (!refRows.isEmpty()) {
                referrerId = toLong(refRows.get(0).get("id"));
                log.info("小程序-推荐人命中 | referrerCode: {} | referrerId: {}", referrerCode, referrerId);
            }
        }

        // 7) 返回更新后的 user 信息
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("phone", phone);
        result.put("nickname", nickname);
        result.put("avatar", avatar);
        result.put("merchantId", merchantId);
        result.put("referrerId", referrerId);
        return ApiResponse.success("绑定成功", result);
    }

    /**
     * 5.3 发送短信验证码（模拟）
     * 需登录
     */
    @PostMapping("/user/send-sms")
    public ApiResponse<Map<String, Object>> sendSms(@RequestBody Map<String, Object> body,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = currentUserId(principal);
        if (userId == null) {
            return ApiResponse.badRequest("用户未登录");
        }
        String phone = toStr(body.get("phone"));
        if (phone == null || phone.isEmpty()) {
            return ApiResponse.badRequest("手机号不能为空");
        }
        // 生成 6 位随机验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        // 模拟发送
        log.info("小程序-发送短信验证码 | phone: {} | code: {}", phone, code);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("phone", phone);
        result.put("code", code);
        return ApiResponse.success("验证码已发送（模拟）", result);
    }

    // =====================================================================================
    // 6. 用户数据（我的设备 / 扫描记录 / 推广记录 / 统计）
    // =====================================================================================

    /**
     * 6.1 我的设备列表
     * 需登录
     */
    @GetMapping("/user/devices")
    public ApiResponse<List<Map<String, Object>>> myDevices(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = currentUserId(principal);
        if (userId == null) {
            return ApiResponse.badRequest("用户未登录");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT ud.created_at AS bound_at, d.id, d.device_no, d.name, d.type, d.status, " +
                        "m.id AS merchant_id, m.name AS merchant_name " +
                        "FROM user_device ud " +
                        "JOIN device d ON ud.device_id = d.id " +
                        "LEFT JOIN merchant m ON d.merchant_id = m.id " +
                        "WHERE ud.user_id = ? ORDER BY ud.created_at DESC LIMIT 100", userId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.get("id"));
            item.put("deviceNo", r.get("device_no"));
            item.put("name", r.get("name"));
            item.put("type", r.get("type"));
            item.put("status", r.get("status"));
            item.put("merchantId", r.get("merchant_id"));
            item.put("merchantName", r.get("merchant_name"));
            item.put("boundAt", norm(r.get("bound_at")));
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    /**
     * 6.2 我的扫描记录
     * 需登录
     */
    @GetMapping("/user/scan-logs")
    public ApiResponse<List<Map<String, Object>>> myScanLogs(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = currentUserId(principal);
        if (userId == null) {
            return ApiResponse.badRequest("用户未登录");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT sl.id, sl.created_at, d.device_no, d.name AS device_name, " +
                        "m.id AS merchant_id, m.name AS merchant_name " +
                        "FROM scan_log sl " +
                        "LEFT JOIN device d ON sl.device_id = d.id " +
                        "LEFT JOIN merchant m ON sl.merchant_id = m.id " +
                        "WHERE sl.user_id = ? ORDER BY sl.created_at DESC LIMIT 100", userId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.get("id"));
            item.put("deviceNo", r.get("device_no"));
            item.put("deviceName", r.get("device_name"));
            item.put("merchantId", r.get("merchant_id"));
            item.put("merchantName", r.get("merchant_name"));
            item.put("createdAt", norm(r.get("created_at")));
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    /**
     * 6.3 我的推广记录
     * 需登录
     */
    @GetMapping("/user/promotion-logs")
    public ApiResponse<List<Map<String, Object>>> myPromotionLogs(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = currentUserId(principal);
        if (userId == null) {
            return ApiResponse.badRequest("用户未登录");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT pl.id, pl.created_at, pp.name AS platform_name, pp.code AS platform_code, " +
                        "m.id AS merchant_id, m.name AS merchant_name " +
                        "FROM promotion_click_log pl " +
                        "LEFT JOIN promotion_platform pp ON pl.platform_id = pp.id " +
                        "LEFT JOIN merchant m ON pl.merchant_id = m.id " +
                        "WHERE pl.user_id = ? ORDER BY pl.created_at DESC LIMIT 100", userId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.get("id"));
            item.put("platformName", r.get("platform_name"));
            item.put("platformCode", r.get("platform_code"));
            item.put("merchantId", r.get("merchant_id"));
            item.put("merchantName", r.get("merchant_name"));
            item.put("createdAt", norm(r.get("created_at")));
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    /**
     * 6.4 我的统计（扫描次数/推广点击/优惠券数）
     * 需登录
     */
    @GetMapping("/user/stats")
    public ApiResponse<Map<String, Object>> myStats(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = currentUserId(principal);
        if (userId == null) {
            return ApiResponse.badRequest("用户未登录");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        Long scanCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM scan_log WHERE user_id = ?", Long.class, userId);
        Long promoCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM promotion_click_log WHERE user_id = ?", Long.class, userId);
        Long couponCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_coupon WHERE user_id = ?", Long.class, userId);
        result.put("totalScans", scanCount == null ? 0 : scanCount);
        result.put("totalPromotions", promoCount == null ? 0 : promoCount);
        result.put("coupons", couponCount == null ? 0 : couponCount);
        return ApiResponse.success(result);
    }

    // =====================================================================================
    // 私有工具方法
    // =====================================================================================

    /**
     * 当前登录用户 ID（通过 username 查 admin/user 表）
     */
    private Long currentUserId(UserPrincipal principal) {
        if (principal == null) {
            return null;
        }
        String username = principal.getUsername();
        return resolveUserId(username);
    }

    /**
     * 通过 username 查 admin/user 表获取 userId（先 admin，未命中再 user）
     */
    private Long resolveUserId(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> admins = jdbcTemplate.queryForList(
                "SELECT id FROM admin WHERE username = ? LIMIT 1", username);
        if (!admins.isEmpty() && admins.get(0).get("id") != null) {
            return toLong(admins.get(0).get("id"));
        }
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "SELECT id FROM user WHERE username = ? OR phone = ? OR openid = ? LIMIT 1", username, username, username);
        if (!users.isEmpty() && users.get(0).get("id") != null) {
            return toLong(users.get(0).get("id"));
        }
        return null;
    }

    /**
     * 通过二维码/设备编码解析已绑定的 merchantId
     */
    private Long resolveBoundMerchantId(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        try {
            List<Map<String, Object>> qrRows = jdbcTemplate.queryForList(
                    "SELECT merchant_id FROM qrcode WHERE code = ? AND bind_status = 1 LIMIT 1", code);
            if (!qrRows.isEmpty()) {
                Long mid = toLong(qrRows.get(0).get("merchant_id"));
                if (mid != null) {
                    return mid;
                }
            }
        } catch (Exception e) {
            log.warn("查询 qrcode bind_status 失败: {}", e.getMessage());
        }
        List<Map<String, Object>> devRows = jdbcTemplate.queryForList(
                "SELECT merchant_id FROM device WHERE (system_code = ? OR device_no = ? OR url LIKE CONCAT('%code=', ?, '%')) AND merchant_id IS NOT NULL LIMIT 1",
                code, code, code);
        if (!devRows.isEmpty()) {
            return toLong(devRows.get(0).get("merchant_id"));
        }
        return null;
    }

    /**
     * 将 code 绑定到 merchantId（更新 qrcode 与 device）
     */
    private void bindCodeToMerchant(String code, Long merchantId) {
        if (code == null || code.isEmpty() || merchantId == null) {
            return;
        }
        try {
            jdbcTemplate.update("UPDATE qrcode SET merchant_id = ?, bind_status = 1, updated_at = NOW() WHERE code = ?",
                    merchantId, code);
        } catch (Exception e) {
            log.warn("更新 qrcode 绑定失败（可能缺少 bind_status 列）: {}", e.getMessage());
            jdbcTemplate.update("UPDATE qrcode SET merchant_id = ?, updated_at = NOW() WHERE code = ?", merchantId, code);
        }
        jdbcTemplate.update("UPDATE device SET merchant_id = ?, updated_at = NOW() WHERE system_code = ? OR device_no = ? OR url LIKE CONCAT('%code=', ?, '%')",
                merchantId, code, code, code);
    }

    /**
     * 通过 id 或 device_no 查询设备（含商家信息）
     */
    private Map<String, Object> findDeviceByIdOrNo(String idOrNo) {
        if (idOrNo == null || idOrNo.isEmpty()) {
            return null;
        }
        // 优先按数字 id，否则按 device_no
        List<Map<String, Object>> rows;
        try {
            Long numId = Long.parseLong(idOrNo);
            rows = jdbcTemplate.queryForList(
                    "SELECT d.*, m.name AS merchant_name, m.id AS merchant_id FROM device d " +
                            "LEFT JOIN merchant m ON d.merchant_id = m.id WHERE d.id = ? OR d.device_no = ? LIMIT 1",
                    numId, idOrNo);
        } catch (NumberFormatException e) {
            rows = jdbcTemplate.queryForList(
                    "SELECT d.*, m.name AS merchant_name, m.id AS merchant_id FROM device d " +
                            "LEFT JOIN merchant m ON d.merchant_id = m.id WHERE d.device_no = ? LIMIT 1", idOrNo);
        }
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * 通过 device_no 查询设备（含商家信息）
     */
    private Map<String, Object> findDeviceByNo(String deviceNo) {
        if (deviceNo == null || deviceNo.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT d.*, m.name AS merchant_name, m.id AS merchant_id FROM device d " +
                        "LEFT JOIN merchant m ON d.merchant_id = m.id WHERE d.device_no = ? LIMIT 1", deviceNo);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * 构造设备视图（驼峰字段）
     */
    private Map<String, Object> buildDeviceView(Map<String, Object> d) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", d.get("id"));
        view.put("deviceNo", d.get("device_no"));
        view.put("name", d.get("name"));
        view.put("type", d.get("type"));
        view.put("systemCode", d.get("system_code"));
        view.put("status", d.get("status"));
        view.put("merchantId", d.get("merchant_id"));
        view.put("merchantName", d.get("merchant_name"));
        view.put("createdAt", norm(d.get("created_at")));
        return view;
    }

    /**
     * 构造平台配置项（含 jumpInfo）
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildPlatformConfigItem(Map<String, Object> r) {
        Map<String, Object> params = (Map<String, Object>) safeParseJson(toStr(r.get("params")));
        if (params == null) {
            params = new LinkedHashMap<>();
        }
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", r.get("id"));
        item.put("type", r.get("type"));
        item.put("params", params);
        item.put("customName", r.get("custom_name"));
        item.put("customIcon", fullUrl(toStr(r.get("custom_icon"))));
        item.put("sort", r.get("sort"));
        item.put("platformId", r.get("platform_id"));
        item.put("code", r.get("code"));
        item.put("name", r.get("name"));
        item.put("icon", fullUrl(toStr(r.get("icon"))));
        item.put("color", r.get("color"));
        item.put("description", r.get("description"));
        item.put("jumpMode", r.get("jump_mode"));
        item.put("schemeTemplate", r.get("scheme_template"));
        item.put("webUrlTemplate", r.get("web_url_template"));
        item.put("miniprogramAppid", r.get("miniprogram_appid"));
        item.put("miniprogramPathTemplate", r.get("miniprogram_path_template"));
        item.put("requiredParams", safeParseJson(toStr(r.get("required_params"))));
        item.put("optionalParams", safeParseJson(toStr(r.get("optional_params"))));
        item.put("jumpInfo", buildJumpInfo(r, params));
        return item;
    }

    /**
     * 构造优惠券配置项
     */
    private Map<String, Object> buildCouponConfigItem(Map<String, Object> r) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", r.get("id"));
        item.put("type", r.get("type"));
        item.put("customName", r.get("custom_name"));
        item.put("customIcon", fullUrl(toStr(r.get("custom_icon"))));
        item.put("sort", r.get("sort"));
        item.put("couponId", r.get("coupon_id"));
        item.put("couponName", r.get("coupon_name"));
        item.put("couponType", r.get("coupon_type"));
        item.put("couponValue", norm(r.get("coupon_value")));
        item.put("couponThreshold", norm(r.get("coupon_threshold")));
        item.put("couponTotalCount", r.get("coupon_total_count"));
        item.put("couponRemainCount", r.get("coupon_remain_count"));
        item.put("couponValidStart", norm(r.get("coupon_valid_start")));
        item.put("couponValidEnd", norm(r.get("coupon_valid_end")));
        item.put("couponStatus", r.get("coupon_status"));
        item.put("link", r.get("coupon_link"));
        return item;
    }

    /**
     * 构造跳转信息（用 params 填充模板占位符）
     */
    private Map<String, Object> buildJumpInfo(Map<String, Object> r, Map<String, Object> params) {
        Map<String, Object> jumpInfo = new LinkedHashMap<>();
        jumpInfo.put("jumpMode", r.get("jump_mode"));
        jumpInfo.put("scheme", fillTemplate(toStr(r.get("scheme_template")), params));
        jumpInfo.put("webUrl", fillTemplate(toStr(r.get("web_url_template")), params));
        jumpInfo.put("miniprogramAppid", r.get("miniprogram_appid"));
        jumpInfo.put("miniprogramPath", fillTemplate(toStr(r.get("miniprogram_path_template")), params));
        return jumpInfo;
    }

    /**
     * 图片 URL 补全：相对路径前补 IMG_BASE，已带 http(s):// 的原样返回
     */
    private String fullUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        if (url.startsWith("/")) {
            return IMG_BASE + url;
        }
        return IMG_BASE + "/" + url;
    }

    /**
     * 用 params 填充模板中的 {key} 占位符
     */
    private String fillTemplate(String template, Map<String, Object> params) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        if (params == null || params.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, Object> e : params.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) {
                continue;
            }
            result = result.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
        }
        return result;
    }

    /**
     * 安全解析 JSON 字符串为对象（Map 或 List），失败返回 null
     */
    private Object safeParseJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            log.warn("JSON 解析失败: {}", json);
            return null;
        }
    }

    /**
     * 解析 JSON 数组并补全图片 URL
     */
    private List<String> parseUrlArray(String json) {
        List<String> result = new ArrayList<>();
        if (json == null || json.isEmpty()) {
            return result;
        }
        try {
            List<?> list = objectMapper.readValue(json, List.class);
            for (Object o : list) {
                if (o != null) {
                    result.add(fullUrl(String.valueOf(o)));
                }
            }
        } catch (Exception e) {
            log.warn("图片数组解析失败: {}", json);
        }
        return result;
    }

    /**
     * 生成 user_code: U + yyMMdd + 4 位随机数字
     */
    private String generateUserCode() {
        String date = new SimpleDateFormat("yyMMdd").format(new Date());
        int rand = (int) (Math.random() * 9000) + 1000;
        return "U" + date + rand;
    }

    /**
     * 数值类型归一化：BigDecimal -> Double，Timestamp/Date -> 字符串，其余原样
     */
    private Object norm(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal) {
            return ((BigDecimal) v).doubleValue();
        }
        if (v instanceof Timestamp) {
            return new SimpleDateFormat(DT_PATTERN).format((Timestamp) v);
        }
        if (v instanceof java.util.Date) {
            return new SimpleDateFormat(DT_PATTERN).format((java.util.Date) v);
        }
        return v;
    }

    private Long toLong(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer toInt(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String toStr(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}
