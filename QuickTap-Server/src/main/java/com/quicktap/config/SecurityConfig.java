package com.quicktap.config;

import com.quicktap.security.JwtAuthenticationEntryPoint;
import com.quicktap.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 安全配置
 *
 * 安全特性:
 * - 启用 CSRF 防护（使用 CookieCsrfTokenRepository）
 * - 限制 CORS 到明确白名单的源
 * - JWT Token 认证
 * - 基于角色的访问控制
 * - 无状态会话策略
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true
)
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${allowed.origins:http://localhost:3000,http://127.0.0.1:3000}")
    private String allowedOriginsStr;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 解析允许的源列表
        String[] allowedOrigins = allowedOriginsStr.split(",");
        List<String> originsList = new ArrayList<>();
        for (String origin : allowedOrigins) {
            String trimmed = origin.trim();
            if (!trimmed.isEmpty()) {
                originsList.add(trimmed);
            }
        }

        if (originsList.isEmpty()) {
            log.warn("未配置允许的源，将默认只允许 localhost");
            originsList.add("http://localhost:3000");
            originsList.add("http://127.0.0.1:3000");
        }

        log.info("配置 CORS 允许的源: {}", originsList);
        configuration.setAllowedOrigins(originsList);

        // 仅允许必需的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 仅允许必需的请求头
        configuration.setAllowedHeaders(Arrays.asList(
            "Origin",
            "Accept",
            "X-Requested-With",
            "Content-Type",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "Authorization",
            "X-CSRF-TOKEN"
        ));

        // 允许浏览器访问 Authorization 响应头
        configuration.setExposedHeaders(Arrays.asList("Authorization", "X-CSRF-TOKEN"));

        // 启用凭证传递（CSRF Token, 认证信息等）
        configuration.setAllowCredentials(true);

        // 设置缓存时间（1小时）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ========== CSRF 防护配置（JWT 无状态 API，禁用 CSRF）==========
            .csrf(csrf -> csrf.disable())

            // ========== CORS 配置 ==========
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ========== 异常处理 ==========
            .exceptionHandling(eh -> eh.authenticationEntryPoint(jwtAuthenticationEntryPoint))

            // ========== 会话配置 ==========
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ========== 授权规则（顺序敏感：具体规则在前，宽泛规则在后）==========
            .authorizeHttpRequests(auth -> auth
                // 公开接口
                .requestMatchers("/api/health", "/api/info", "/api/time").permitAll()

                // 认证端点
                .requestMatchers("/admin/auth/login", "/api/admin/auth/login").permitAll()
                .requestMatchers("/user/login", "/api/user/login").permitAll()
                .requestMatchers("/user/auth/wechat-mini", "/api/user/auth/wechat-mini").permitAll()
                .requestMatchers("/user/register", "/api/user/register").permitAll()
                .requestMatchers("/user/register-bind", "/api/user/register-bind").permitAll()
                .requestMatchers("/logout", "/api/logout").permitAll()
                .requestMatchers("/refresh-token", "/api/refresh-token").permitAll()
                .requestMatchers("/validate-token", "/api/validate-token").permitAll()
                .requestMatchers("/csrf-token", "/api/csrf-token").permitAll()

                // Swagger 文档接口
                .requestMatchers("/doc.html", "/webjars/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-resources/**").permitAll()

                // C端公开查询接口（必须在角色规则之前声明）
                .requestMatchers("/api/promotion/**").permitAll()
                .requestMatchers("/api/coupon/**").permitAll()
                .requestMatchers("/api/plan/**").permitAll()
                .requestMatchers("/api/merchant/check-bind").permitAll()
                .requestMatchers("/api/merchant/info/**").permitAll()
                .requestMatchers("/api/merchant/promotion/**").permitAll()

                // 小程序端（/api/miniapp）公开接口：放行无需登录的 C 端查询/入驻接口，
                // 其余 miniapp 接口（如 coupon/claim、coupon/my、device/**、user/register-bind、user/send-sms）
                // 不在此声明，将落入下方 anyRequest().authenticated() 强制认证。
                .requestMatchers(HttpMethod.GET, "/api/miniapp/merchant/check-bind").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/miniapp/merchant/info/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/miniapp/merchant/promotion").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/miniapp/merchant/wifi").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/miniapp/merchant/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/miniapp/coupon/list").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/miniapp/promotion/platforms").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/miniapp/promotion/platform/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/miniapp/promotion/log").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/miniapp/user/referrer/list").permitAll()

                // 管理员接口
                .requestMatchers(HttpMethod.GET, "/api/admin/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/admin/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/admin/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/admin/**").hasAnyRole("SUPER_ADMIN", "ADMIN")

                // 商户接口（同时给 ADMIN 访问，因为管理后台查看/操作商户数据也要过这些路径）
                .requestMatchers(HttpMethod.GET, "/api/merchant/**").hasAnyRole("MERCHANT", "SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/merchant/**").hasAnyRole("MERCHANT", "SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/merchant/**").hasAnyRole("MERCHANT", "SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/merchant/**").hasAnyRole("MERCHANT", "SUPER_ADMIN", "ADMIN")

                // 设备/订单接口：给 ADMIN 和 MERCHANT 角色访问（与各自 Controller 内部的 @PreAuthorize 进一步细粒度校验配合）
                .requestMatchers(HttpMethod.GET, "/api/device/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "MERCHANT")
                .requestMatchers(HttpMethod.POST, "/api/device/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "MERCHANT")
                .requestMatchers(HttpMethod.PUT, "/api/device/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "MERCHANT")
                .requestMatchers(HttpMethod.DELETE, "/api/device/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "MERCHANT")

                .requestMatchers(HttpMethod.GET, "/api/order/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "MERCHANT")
                .requestMatchers(HttpMethod.POST, "/api/order/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "MERCHANT")
                .requestMatchers(HttpMethod.PUT, "/api/order/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "MERCHANT")
                .requestMatchers(HttpMethod.DELETE, "/api/order/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "MERCHANT")

                // 用户接口
                .requestMatchers("/api/user/**").hasAnyRole("USER", "SUPER_ADMIN")

                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            )

            // ========== JWT 过滤器 ==========
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
