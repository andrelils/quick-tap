package com.quicktap.mapper;

import com.quicktap.entity.SystemSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统设置 Mapper
 */
@Mapper
public interface SystemSettingMapper {

    /**
     * 查询全部系统设置
     */
    List<SystemSetting> selectAll();

    /**
     * 按 key 查询单条配置
     */
    SystemSetting selectByKey(@Param("keyName") String keyName);

    /**
     * 新增配置
     */
    int insert(@Param("keyName") String keyName, @Param("value") String value);

    /**
     * 按 key 更新 value
     */
    int updateByKey(@Param("keyName") String keyName, @Param("value") String value);
}
