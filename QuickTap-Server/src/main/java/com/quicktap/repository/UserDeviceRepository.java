package com.quicktap.repository;

import com.quicktap.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户设备关系Repository
 */
@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    
    /**
     * 检查用户是否已绑定设备
     */
    boolean existsByUserIdAndDeviceId(Long userId, Long deviceId);

    /**
     * 根据用户ID查询绑定的所有设备
     */
    List<UserDevice> findByUserId(Long userId);

    /**
     * 根据设备ID查询绑定的所有用户
     */
    List<UserDevice> findByDeviceId(Long deviceId);

    /**
     * 根据用户ID和设备ID查询绑定关系
     */
    Optional<UserDevice> findByUserIdAndDeviceId(Long userId, Long deviceId);

    /**
     * 删除指定用户的所有设备绑定
     */
    void deleteByUserId(Long userId);

    /**
     * 删除指定设备的所有用户绑定
     */
    void deleteByDeviceId(Long deviceId);
}
