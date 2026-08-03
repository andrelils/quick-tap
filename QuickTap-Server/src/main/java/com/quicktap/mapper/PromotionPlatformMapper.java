package com.quicktap.mapper;

import com.quicktap.entity.PromotionPlatform;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PromotionPlatformMapper {
    PromotionPlatform selectById(@Param("id") Integer id);
    PromotionPlatform selectByCode(@Param("code") String code);
    List<PromotionPlatform> selectAll();
    List<PromotionPlatform> selectByStatus(@Param("status") Integer status);
    List<PromotionPlatform> selectPage(@Param("offset") int offset, @Param("limit") int limit);
    int insert(PromotionPlatform platform);
    int update(PromotionPlatform platform);
    int deleteById(@Param("id") Integer id);
    int countAll();
    /**
     * 按排序号倒序查询启用的推广平台列表
     */
    List<PromotionPlatform> selectByStatusOrderBySort(@Param("status") Integer status);
}
