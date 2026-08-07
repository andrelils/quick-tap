package com.quicktap.mapper;

import com.quicktap.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AdminMapper {
    Admin selectById(@Param("id") Integer id);
    Admin selectByUsername(@Param("username") String username);
    List<Admin> selectAll();
    List<Admin> selectByMerchantId(@Param("merchantId") Integer merchantId);
    /**
     * 分页查询管理员列表
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 管理员列表
     */
    List<Admin> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 按关键词/角色/状态分页查询管理员列表
     * @param params 查询条件（keyword/role/status）
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 管理员列表
     */
    List<Admin> selectByKeyword(@Param("params") java.util.Map<String, Object> params, @Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 按关键词/角色/状态统计管理员总数
     */
    long countByKeyword(@Param("params") java.util.Map<String, Object> params);
    int insert(Admin admin);
    int update(Admin admin);
    int deleteById(@Param("id") Integer id);
    int countAll();
}
