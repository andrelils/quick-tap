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
}
