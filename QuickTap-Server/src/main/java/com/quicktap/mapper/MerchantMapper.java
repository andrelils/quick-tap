package com.quicktap.mapper;

import com.quicktap.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MerchantMapper {
    Merchant selectById(@Param("id") Integer id);
    List<Merchant> selectAll();
    List<Merchant> selectByStatus(@Param("status") Integer status);
    List<Merchant> selectByAuditStatus(@Param("auditStatus") Integer auditStatus);
    /**
     * 分页查询审核状态商户列表
     * @param auditStatus 审核状态
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 商户列表
     */
    List<Merchant> selectByAuditStatusPage(@Param("auditStatus") Integer auditStatus, @Param("offset") int offset, @Param("limit") int limit);
    List<Merchant> selectPage(@Param("offset") int offset, @Param("limit") int limit);
    int insert(Merchant merchant);
    int update(Merchant merchant);
    int deleteById(@Param("id") Integer id);
    int countAll();
    int countByStatus(@Param("status") Integer status);
    /**
     * 按创建时间倒序查询启用的商户列表
     */
    List<Merchant> selectByStatusOrderByCreatedAtDesc(@Param("status") Integer status);
    /**
     * 根据推荐人代码查询商户
     */
    Merchant selectByReferrerCode(@Param("referrerCode") String referrerCode);

    /**
     * 获取商户状态
     */
    Integer getStatusByMerchantId(@Param("merchantId") Integer merchantId);

    /**
     * 按收入排名查询顶级商户
     */
    List<java.util.Map<String, Object>> selectTopMerchantsByRevenue(@Param("limit") Integer limit);

    /**
     * 按订单数排名查询顶级商户
     */
    List<java.util.Map<String, Object>> selectTopMerchantsByOrders(@Param("limit") Integer limit);
}
