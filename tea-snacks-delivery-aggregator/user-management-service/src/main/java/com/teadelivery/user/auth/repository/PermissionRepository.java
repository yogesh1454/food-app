package com.teadelivery.user.auth.repository;

import com.teadelivery.user.auth.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    
    Optional<Permission> findByName(String name);
    
    Optional<Permission> findByResourceAndAction(String resource, String action);
    
    List<Permission> findByIsActiveTrue();
    
    @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.isActive = true")
    List<Permission> findActivePermissionsByResource(@Param("resource") String resource);
    
    @Query("SELECT p FROM Permission p WHERE p.action = :action AND p.isActive = true")
    List<Permission> findActivePermissionsByAction(@Param("action") String action);
    
    boolean existsByName(String name);
    
    boolean existsByResourceAndAction(String resource, String action);
} 