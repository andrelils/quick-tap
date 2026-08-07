package com.quicktap.mapper;

import com.quicktap.entity.AiConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 配置 Mapper (XML配置)
 */
@Mapper
public interface AiConfigMapper {

    /**
     * 插入 AI 配置
     */
    int insert(AiConfig config);

    /**
     * 按 ID 查询
     */
    AiConfig selectById(Long id);

    /**
     * 查询全局配置（merchant_id IS NULL）
     */
    AiConfig selectGlobalConfig();

    /**
     * 查询商户级配置
     */
    AiConfig selectByMerchantId(Long merchantId);

    /**
     * 分页查询所有商户级配置（merchant_id IS NOT NULL）
     */
    List<AiConfig> selectMerchantPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 统计商户级配置总数
     */
    long countMerchantConfigs();

    /**
     * 更新 AI 配置
     */
    int update(AiConfig config);

    /**
     * 删除 AI 配置
     */
    int deleteById(Long id);

    /**
     * 按商户 ID 删除
     */
    int deleteByMerchantId(Long merchantId);

    /**
     * 检查商户是否有配置
     */
    int countByMerchantId(Long merchantId);
}
