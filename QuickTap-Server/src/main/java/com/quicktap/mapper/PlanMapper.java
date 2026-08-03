package com.quicktap.mapper;

import com.quicktap.entity.Plan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PlanMapper {
    Plan selectById(@Param("id") Integer id);
    List<Plan> selectByLevel(@Param("level") String level);
    List<Plan> selectByStatus(@Param("status") Integer status);
    List<Plan> selectAll();
    List<Plan> selectPage(@Param("offset") int offset, @Param("pageSize") int pageSize);
    List<Plan> selectRecommended();
    int insert(Plan plan);
    int update(Plan plan);
    int deleteById(@Param("id") Integer id);
    long countAll();
}
