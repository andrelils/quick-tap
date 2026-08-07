package com.quicktap.controller;

import com.quicktap.dto.ApiResponse;
import com.quicktap.dto.PageResponse;
import com.quicktap.dto.OrderCreateRequest;
import com.quicktap.entity.Order;
import com.quicktap.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 订单管理控制器
 * IMPORTANT: Static routes must be defined BEFORE dynamic routes
 */
@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Validated
public class OrderController {
    private final OrderService orderService;

    // ============================================================================
    // STATIC ROUTES - All static routes must come before dynamic routes
    // ============================================================================

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<PageResponse<Order>> listOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<Order> data = orderService.getOrderList(pageNum, pageSize);
        return ApiResponse.success("获取成功", data);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    public ApiResponse<Order> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        Order order = new Order();
        order.setMerchantId(request.getMerchantId());
        order.setPlanId(request.getPlanId());
        order.setAmount(request.getAmount());
        Order created = orderService.createOrder(order);
        return ApiResponse.success("创建成功", created);
    }

    // ============================================================================
    // DYNAMIC ROUTES - All dynamic routes must come AFTER static routes
    // ============================================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    public ApiResponse<Order> getOrder(@PathVariable @NotNull Integer id) {
        Order order = orderService.getOrderById(id);
        return ApiResponse.success("获取成功", order);
    }

    @GetMapping("/merchant/{merchantId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    public ApiResponse<PageResponse<Order>> getMerchantOrders(
            @PathVariable @NotNull Integer merchantId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<Order> data = orderService.getMerchantOrderList(merchantId, pageNum, pageSize);
        return ApiResponse.success("获取成功", data);
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MERCHANT')")
    public ApiResponse<Order> payOrder(@PathVariable @NotNull Integer id) {
        Order order = orderService.payOrder(id);
        return ApiResponse.success("支付成功", order);
    }

    /**
     * 订单退款
     * 前端 marketing.js#refundOrder 调用，body 可携带 { reason }
     */
    @PutMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Order> refundOrder(
            @PathVariable @NotNull Integer id,
            @RequestBody(required = false) java.util.Map<String, Object> body) {
        String reason = body != null ? (String) body.get("reason") : null;
        Order order = orderService.refundOrder(id, reason);
        return ApiResponse.success("退款成功", order);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ApiResponse<Void> deleteOrder(@PathVariable @NotNull Integer id) {
        orderService.deleteOrder(id);
        return ApiResponse.success("删除成功");
    }
}
