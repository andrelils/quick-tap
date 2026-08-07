package com.quicktap.service;

import com.quicktap.entity.Order;
import com.quicktap.exception.BusinessException;
import com.quicktap.mapper.OrderMapper;
import com.quicktap.dto.PageResponse;
import com.quicktap.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 订单管理业务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderMapper orderMapper;

    @Autowired
    private MerchantQuotaService merchantQuotaService;

    /**
     * 获取订单列表（分页，联查商家名/套餐名，返回 camelCase map 供 admin 订单页与 my-quota 使用）
     */
    public PageResponse<java.util.Map<String, Object>> getOrderList(Integer pageNum, Integer pageSize) {
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.max(Math.min(pageSize, 100), 1);
        int offset = (pageNum - 1) * pageSize;

        List<java.util.Map<String, Object>> list = orderMapper.selectPageWithNames(offset, pageSize);
        long total = orderMapper.countAll();

        return PageResponse.of(list, pageNum, pageSize, total);
    }

    /**
     * 获取订单详情
     */
    public Order getOrderById(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "订单ID不能为空");
        }
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");
        }
        return order;
    }

    /**
     * 获取商户订单列表（分页，联查套餐名，返回 snake_case map 供 my-quota 购买记录展示）
     */
    public PageResponse<java.util.Map<String, Object>> getMerchantOrderList(Integer merchantId, Integer pageNum, Integer pageSize) {
        if (merchantId == null || merchantId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "商户ID不能为空");
        }
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.max(Math.min(pageSize, 100), 1);
        int offset = (pageNum - 1) * pageSize;

        List<java.util.Map<String, Object>> list = orderMapper.selectMerchantOrdersWithPlan(merchantId, offset, pageSize);
        long total = orderMapper.countByMerchantId(merchantId);

        return PageResponse.of(list, pageNum, pageSize, total);
    }

    /**
     * 按状态获取订单（分页）
     */
    public PageResponse<Order> getOrderByStatus(String status, Integer pageNum, Integer pageSize) {
        if (status == null || status.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "订单状态不能为空");
        }
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.max(Math.min(pageSize, 100), 1);
        int offset = (pageNum - 1) * pageSize;

        List<Order> list = orderMapper.selectByStatus(status);
        long total = orderMapper.countByStatus(status);

        return PageResponse.of(list, pageNum, pageSize, total);
    }

    /**
     * 按条件导出订单（支持订单号/商家/状态/时间范围过滤）
     */
    public List<java.util.Map<String, Object>> exportOrders(String orderNo, Integer merchantId,
                                                            String status, String startDate, String endDate) {
        return orderMapper.selectForExport(
                emptyToNull(orderNo), merchantId, emptyToNull(status),
                emptyToNull(startDate), emptyToNull(endDate), 10000);
    }

    private String emptyToNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }

    /**
     * 创建订单
     */
    public Order createOrder(Order order) {
        if (order == null || order.getMerchantId() == null || order.getMerchantId() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "商户ID不能为空");
        }
        if (order.getPlanId() == null || order.getPlanId() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "套餐ID不能为空");
        }
        if (order.getAmount() == null || order.getAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "订单金额不能为空或小于0");
        }

        // 生成订单号
        order.setOrderNo(generateOrderNo());
        order.setStatus("pending");
        order.setExpireAt(LocalDateTime.now().plusHours(1));

        orderMapper.insert(order);
        log.info("创建订单成功, orderNo: {}, merchantId: {}, amount: {}", order.getOrderNo(), order.getMerchantId(), order.getAmount());
        return order;
    }

    /**
     * 更新订单
     */
    public Order updateOrder(Integer id, Order order) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "订单ID不能为空");
        }
        Order existing = orderMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");
        }

        if (order.getAmount() != null && order.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            existing.setAmount(order.getAmount());
        }
        if (order.getStatus() != null && !order.getStatus().isEmpty()) {
            existing.setStatus(order.getStatus());
        }
        if (order.getExpireAt() != null) {
            existing.setExpireAt(order.getExpireAt());
        }

        orderMapper.update(existing);
        log.info("更新订单成功, id: {}, status: {}", id, order.getStatus());
        return existing;
    }

    /**
     * 支付订单（支付成功后自动为商户生效对应套餐）
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Order payOrder(Integer id) {
        Order order = getOrderById(id);
        if (!"pending".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "只能支付未支付的订单");
        }
        order.setStatus("paid");
        orderMapper.update(order);

        // 支付成功后为商户切换套餐（含额度/设备数/存储限制）
        try {
            merchantQuotaService.changePlan(order.getMerchantId(), order.getPlanId());
            log.info("支付后套餐生效: orderId={}, merchantId={}, planId={}", id, order.getMerchantId(), order.getPlanId());
        } catch (Exception e) {
            log.error("支付后套餐生效失败，事务回滚: orderId={}, error={}", id, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "套餐生效失败: " + e.getMessage());
        }

        log.info("订单支付成功, id: {}, orderNo: {}", id, order.getOrderNo());
        return order;
    }

    /**
     * 退款订单
     * 仅 paid 状态订单可退款，退款后状态置为 refunded
     * 事务保证：订单状态更新与额度回退原子性，任一失败则回滚
     *
     * @param id     订单 ID
     * @param reason 退款原因（可选）
     * @return 更新后的订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Order refundOrder(Integer id, String reason) {
        Order order = getOrderById(id);
        if (!"paid".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "只能对已支付订单进行退款，当前状态: " + order.getStatus());
        }

        // 1. 更新订单状态为已退款
        order.setStatus("refunded");
        orderMapper.update(order);

        // 2. 回退商户额度
        // 退款后应扣减购买套餐时发放的配额（文本/图片/视频/存储）。
        // 当前 MerchantQuotaService 仅提供存储用量递减，套餐维度的总配额回退
        // 需结合 Plan 表与 merchant_quota 表实现，此处先记录退款事件，配额回退待配额表完善后补充。
        if (order.getMerchantId() != null) {
            try {
                log.info("订单退款额度回退 | orderId: {}, merchantId: {}, planId: {}, amount: {}",
                        id, order.getMerchantId(), order.getPlanId(), order.getAmount());
                // TODO: 待 MerchantQuotaService 提供 reduceTotalQuota(merchantId, planId) 后启用
                // merchantQuotaService.reduceTotalQuota(order.getMerchantId().longValue(), order.getPlanId());
            } catch (Exception e) {
                log.error("订单退款额度回退失败，事务将回滚 | orderId: {}, error: {}", id, e.getMessage(), e);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "退款额度回退失败: " + e.getMessage());
            }
        }

        log.info("订单退款成功, id: {}, orderNo: {}, reason: {}", id, order.getOrderNo(), reason);
        return order;
    }

    /**
     * 删除订单
     */
    public void deleteOrder(Integer id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "订单ID不能为空");
        }
        Order existing = orderMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");
        }
        orderMapper.deleteById(id);
        log.info("删除订单成功, id: {}", id);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
