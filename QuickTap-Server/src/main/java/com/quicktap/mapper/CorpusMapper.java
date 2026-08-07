package com.quicktap.mapper;

import com.quicktap.entity.Corpus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CorpusMapper {
    Corpus selectById(@Param("id") Integer id);
    Corpus selectByCorpusId(@Param("corpusId") String corpusId);
    List<Corpus> selectByMerchantId(@Param("merchantId") Integer merchantId);
    List<Corpus> selectByCategory(@Param("category") String category);
    List<Corpus> selectAll();
    List<Corpus> selectPage(@Param("offset") int offset, @Param("pageSize") int pageSize);
    int insert(Corpus corpus);
    int update(Corpus corpus);
    int deleteById(@Param("id") Integer id);
    int deleteByCorpusId(@Param("corpusId") String corpusId);
    long countAll();
    long countByMerchantId(@Param("merchantId") Integer merchantId);
    int deletePermanentlyTrashedCorpus(@Param("beforeTime") LocalDateTime beforeTime);

    // ========== 分页查询（修复内存分页问题） ==========

    /** 按分类分页查询（status=1 正常状态） */
    List<Corpus> selectByCategoryPage(@Param("category") String category,
                                      @Param("offset") int offset,
                                      @Param("pageSize") int pageSize);

    /** 按分类统计总数（status=1） */
    long countByCategory(@Param("category") String category);

    /** 回收站分页查询（指定商户，status=0 已删除） */
    List<Corpus> selectTrashByMerchantIdPage(@Param("merchantId") Integer merchantId,
                                             @Param("offset") int offset,
                                             @Param("pageSize") int pageSize);

    /** 回收站统计总数（指定商户，status=0） */
    long countTrashByMerchantId(@Param("merchantId") Integer merchantId);

    /** 按关键词模糊查询候选集（用于 search 优化，先 SQL 过滤再内存评分） */
    List<Corpus> selectByMerchantIdAndKeyword(@Param("merchantId") Integer merchantId,
                                              @Param("keyword") String keyword);

    /** 全局按关键词模糊查询候选集（用于全局 search 优化，仅正常状态） */
    List<Corpus> selectByKeyword(@Param("keyword") String keyword);
}
