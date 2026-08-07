package com.quicktap.repository;

import com.quicktap.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Permission Repository
 *
 * Provides database access for Permission entity
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Find permission by code
     */
    Optional<Permission> findByCode(String code);

    /**
     * Find all active permissions
     */
    @Query("SELECT p FROM Permission p WHERE p.status = 1 ORDER BY p.resource, p.action")
    List<Permission> findAllActive();

    /**
     * Find permissions by resource
     */
    @Query("SELECT p FROM Permission p WHERE p.resource = ?1 AND p.status = 1 ORDER BY p.action")
    List<Permission> findByResource(String resource);

    /**
     * Find permissions by category
     */
    @Query("SELECT p FROM Permission p WHERE p.category = ?1 AND p.status = 1")
    List<Permission> findByCategory(String category);

    /**
     * Find all unique resources
     */
    @Query("SELECT DISTINCT p.resource FROM Permission p WHERE p.status = 1 ORDER BY p.resource")
    List<String> findAllResources();

    /**
     * Check if permission exists by code
     */
    boolean existsByCode(String code);
}
