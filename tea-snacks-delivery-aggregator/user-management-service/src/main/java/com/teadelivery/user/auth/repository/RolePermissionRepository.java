package com.teadelivery.user.auth.repository;

import com.teadelivery.user.auth.model.RolePermission;
import com.teadelivery.user.profile.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {
    
    List<RolePermission> findByRole(User.Role role);
    
    List<RolePermission> findByRoleAndIsActiveTrue(User.Role role);
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role = :role AND rp.permission.isActive = true AND rp.isActive = true")
    List<RolePermission> findActiveRolePermissionsByRole(@Param("role") User.Role role);
    
    @Query("SELECT rp.permission FROM RolePermission rp WHERE rp.role = :role AND rp.isActive = true AND rp.permission.isActive = true")
    List<com.teadelivery.user.auth.model.Permission> findPermissionsByRole(@Param("role") User.Role role);
    
    @Query("SELECT rp.permission FROM RolePermission rp WHERE rp.role IN :roles AND rp.isActive = true AND rp.permission.isActive = true")
    List<com.teadelivery.user.auth.model.Permission> findPermissionsByRoles(@Param("roles") List<User.Role> roles);
    
    boolean existsByRoleAndPermissionId(User.Role role, UUID permissionId);
} 