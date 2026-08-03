package com.quicktap.mapper;

import com.quicktap.entity.AiGenerateRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AiGenerateRecordMapper {
    AiGenerateRecord selectById(@Param("id") Integer id);
    AiGenerateRecord selectByRecordId(@Param("recordId") String recordId);
    List<AiGenerateRecord> selectByMerchantId(@Param("merchantId") Integer merchantId);
    List<AiGenerateRecord> selectByType(@Param("type") String type);
    List<AiGenerateRecord> selectAll();
    List<AiGenerateRecord> selectPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 按商户和类型过滤分页查询
     */
    List<AiGenerateRecord> selectPageByMerchantAndType(
        @Param("merchantId") Integer merchantId,
        @Param("type") String type,
        @Param("offset") int offset,
        @Param("pageSize") int pageSize);

    /**
     * 按商户和类型统计记录数
     */
    long countByMerchantAndType(
        @Param("merchantId") Integer merchantId,
        @Param("type") String type);

    int insert(AiGenerateRecord record);
    int update(AiGenerateRecord record);
    int deleteById(@Param("id") Integer id);
    int countAll();

    /**
     * 按状态统计记录数
     */
    long countByStatus(@Param("status") Integer status);

    /**
     * 按类型和状态统计记录数
     */
    long countByTypeAndStatus(@Param("type") String type, @Param("status") Integer status);
}
