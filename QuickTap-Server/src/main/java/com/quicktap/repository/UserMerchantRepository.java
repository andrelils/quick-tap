package com.quicktap.repository;

import com.quicktap.entity.UserMerchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户商户关系Repository
 */
@Repository
public interface UserMerchantRepository extends JpaRepository<UserMerchant, Long> {
    
    /**
     * 根据用户ID查询用户商户关系
     */
    Optional<UserMerchant> findByUserId(Long userId);

    /**
     * 检查用户是否已关联商户
     */
    boolean existsByUserId(Long userId);

    /**
     * 根据用户ID删除用户商户关系
     */
    void deleteByUserId(Long userId);
}
