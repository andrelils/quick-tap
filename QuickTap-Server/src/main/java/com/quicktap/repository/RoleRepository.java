package com.quicktap.repository;

import com.quicktap.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Role Repository
 *
 * Provides database access for Role entity
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find role by code
     */
    Optional<Role> findByCode(String code);

    /**
     * Find all active roles
     */
    @Query("SELECT r FROM Role r WHERE r.status = 1 ORDER BY r.code")
    List<Role> findAllActive();

    /**
     * Find all system roles
     */
    @Query("SELECT r FROM Role r WHERE r.isSystem = true AND r.status = 1")
    List<Role> findAllSystem();

    /**
     * Check if role exists by code
     */
    boolean existsByCode(String code);

    /**
     * Count active roles
     */
    @Query("SELECT COUNT(r) FROM Role r WHERE r.status = 1")
    long countActive();
}
