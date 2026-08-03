package com.quicktap.mapper;

import com.quicktap.entity.CorpusCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 语料库分类 Mapper (XML配置)
 */
@Mapper
public interface CorpusCategoryMapper {

    /**
     * 插入分类
     */
    int insert(CorpusCategory category);

    /**
     * 按 ID 查询
     */
    CorpusCategory selectById(Long id, Long merchantId);

    /**
     * 按商户 ID 查询所有分类（排序）
     */
    List<CorpusCategory> selectByMerchantId(Long merchantId);

    /**
     * 按名称查询（防止重复）
     */
    CorpusCategory selectByName(String name, Long merchantId);

    /**
     * 更新分类
     */
    int update(CorpusCategory category);

    /**
     * 删除分类（检查是否有关联语料）
     */
    int deleteById(Long id, Long merchantId);

    /**
     * 增加该分类下的语料计数
     */
    int incrementCorpusCount(Long id);

    /**
     * 减少该分类下的语料计数
     */
    int decrementCorpusCount(Long id);

    /**
     * 获取分类总数
     */
    int countByMerchantId(Long merchantId);
}
