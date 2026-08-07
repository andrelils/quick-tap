package com.quicktap.mapper;

import com.quicktap.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DeviceMapper {
    Device selectById(@Param("id") Integer id);
    Device selectByDeviceNo(@Param("deviceNo") String deviceNo);
    List<Device> selectByMerchantId(@Param("merchantId") Integer merchantId);
    List<Device> selectByMerchantIdAndStatus(@Param("merchantId") Integer merchantId, @Param("status") Integer status);
    List<Device> selectByMerchantIdAndPage(@Param("merchantId") Integer merchantId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    Device selectByBindQrCodeId(@Param("bindQrCodeId") Long bindQrCodeId);
    List<Device> selectPage(@Param("offset") int offset, @Param("pageSize") int pageSize);
    int insert(Device device);
    int insertBatch(List<Device> devices);
    int update(Device device);
    int updateStatusByIds(@Param("ids") List<Integer> ids, @Param("status") Integer status);
    int deleteById(@Param("id") Integer id);
    int deleteByMerchantId(@Param("merchantId") Integer merchantId);
    long countByMerchantId(@Param("merchantId") Integer merchantId);
    long countAll();
    long countSets();
}
