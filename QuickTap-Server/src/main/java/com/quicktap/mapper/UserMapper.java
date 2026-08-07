package com.quicktap.mapper;

import com.quicktap.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    User selectById(@Param("id") Long id);
    User selectByUsername(@Param("username") String username);
    User selectByPhone(@Param("phone") String phone);
    User selectByOpenid(@Param("openid") String openid);
    User selectByUnionid(@Param("unionid") String unionid);
    List<User> selectAll();
    List<User> selectPage(@Param("offset") int offset, @Param("pageSize") int pageSize);
    List<User> selectByKeyword(@Param("params") Map<String, Object> params, @Param("offset") int offset, @Param("pageSize") int pageSize);
    long countByKeyword(@Param("params") Map<String, Object> params);
    int insert(User user);
    int update(User user);
    int delete(@Param("id") Long id);
    int deleteById(@Param("id") Long id);
    long countAll();
    long countByMerchantId(@Param("merchantId") Integer merchantId);
    List<java.util.Map<String, Object>> selectNewUsersGroupedByDate(
        @Param("startDateTime") java.time.LocalDateTime startDateTime,
        @Param("endDateTime") java.time.LocalDateTime endDateTime);
}
