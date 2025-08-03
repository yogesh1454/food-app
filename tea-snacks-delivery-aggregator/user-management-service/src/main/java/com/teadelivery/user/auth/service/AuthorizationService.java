package com.teadelivery.user.auth.service;

import com.teadelivery.user.auth.model.Permission;
import com.teadelivery.user.auth.repository.PermissionRepository;
import com.teadelivery.user.auth.repository.RolePermissionRepository;
import com.teadelivery.user.profile.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.teadelivery.user.auth.model.RolePermission;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {
    
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    
    /**
     * Check if the current user has a specific permission
     */
    public boolean hasPermission(String resource, String action) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("User not authenticated for permission check: {}:{}", resource, action);
            return false;
        }
        
        String username = authentication.getName();
        log.debug("Checking permission for user {}: {}:{}", username, resource, action);
        
        // Get user's role from authentication
        User.Role userRole = getUserRoleFromAuthentication(authentication);
        if (userRole == null) {
            log.warn("No role found for user: {}", username);
            return false;
        }
        
        return hasPermissionByRole(userRole, resource, action);
    }
    
    /**
     * Check if a role has a specific permission
     */
    public boolean hasPermissionByRole(User.Role role, String resource, String action) {
        List<Permission> permissions = rolePermissionRepository.findPermissionsByRole(role);
        
        boolean hasPermission = permissions.stream()
                .anyMatch(permission -> 
                    permission.getResource().equals(resource) && 
                    permission.getAction().equals(action) &&
                    permission.getIsActive()
                );
        
        log.debug("Role {} permission check for {}:{} = {}", role, resource, action, hasPermission);
        return hasPermission;
    }
    
    /**
     * Check if user can access a specific resource
     */
    public boolean canAccessResource(String resource, String action, UUID resourceOwnerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // Check basic permission first
        if (!hasPermission(resource, action)) {
            return false;
        }
        
        // For admin role, allow access to all resources
        User.Role userRole = getUserRoleFromAuthentication(authentication);
        if (userRole == User.Role.ADMIN) {
            return true;
        }
        
        // For other roles, check resource ownership
        return isResourceOwner(authentication, resourceOwnerId);
    }
    
    /**
     * Check if user is the owner of a resource
     */
    public boolean isResourceOwner(Authentication authentication, UUID resourceOwnerId) {
        if (resourceOwnerId == null) {
            return false;
        }
        
        // Extract user ID from authentication
        String userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return false;
        }
        
        return userId.equals(resourceOwnerId.toString());
    }
    
    /**
     * Get all permissions for a role
     */
    public Set<String> getPermissionsForRole(User.Role role) {
        List<Permission> permissions = rolePermissionRepository.findPermissionsByRole(role);
        return permissions.stream()
                .map(Permission::getFullPermission)
                .collect(Collectors.toSet());
    }
    
    /**
     * Get all permissions for the current user
     */
    public Set<String> getCurrentUserPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Set.of();
        }
        
        User.Role userRole = getUserRoleFromAuthentication(authentication);
        if (userRole == null) {
            return Set.of();
        }
        
        return getPermissionsForRole(userRole);
    }
    
    /**
     * Extract user role from authentication
     */
    private User.Role getUserRoleFromAuthentication(Authentication authentication) {
        try {
            // Extract role from JWT claims or user details
            // This would be set during JWT token generation
            String roleString = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElse("CUSTOMER");
            
            return User.Role.valueOf(roleString.replace("ROLE_", ""));
        } catch (Exception e) {
            log.error("Error extracting role from authentication", e);
            return User.Role.CUSTOMER; // Default fallback
        }
    }
    
    /**
     * Extract user ID from authentication
     */
    private String getUserIdFromAuthentication(Authentication authentication) {
        try {
            // Extract user ID from JWT claims
            // This would be set during JWT token generation
            return authentication.getName(); // For now, using name as ID
        } catch (Exception e) {
            log.error("Error extracting user ID from authentication", e);
            return null;
        }
    }
    
    /**
     * Initialize default permissions for all roles
     */
    public void initializeDefaultPermissions() {
        log.info("Initializing default permissions for all roles");
        
        // Create default permissions
        createDefaultPermissions();
        
        // Assign permissions to roles
        assignDefaultRolePermissions();
    }
    
    /**
     * Create default permissions
     */
    private void createDefaultPermissions() {
        // Admin permissions
        createPermissionIfNotExists("manage_users", "Manage all users", "users", "manage");
        createPermissionIfNotExists("manage_vendors", "Manage all vendors", "vendors", "manage");
        createPermissionIfNotExists("manage_delivery_partners", "Manage delivery partners", "delivery_partners", "manage");
        createPermissionIfNotExists("view_reports", "View all reports", "reports", "view");
        createPermissionIfNotExists("manage_system", "Manage system settings", "system", "manage");
        
        // Vendor permissions
        createPermissionIfNotExists("manage_own_profile", "Manage own profile", "profile", "manage");
        createPermissionIfNotExists("manage_menu", "Manage menu items", "menu", "manage");
        createPermissionIfNotExists("manage_orders", "Manage orders", "orders", "manage");
        createPermissionIfNotExists("view_own_reports", "View own reports", "reports", "view_own");
        
        // Delivery partner permissions
        createPermissionIfNotExists("manage_deliveries", "Manage deliveries", "deliveries", "manage");
        createPermissionIfNotExists("update_location", "Update location", "location", "update");
        
        // Customer permissions
        createPermissionIfNotExists("place_orders", "Place orders", "orders", "place");
        createPermissionIfNotExists("view_order_history", "View order history", "orders", "view_history");
    }
    
    /**
     * Create permission if it doesn't exist
     */
    private void createPermissionIfNotExists(String name, String description, String resource, String action) {
        if (!permissionRepository.existsByName(name)) {
            Permission permission = Permission.builder()
                    .name(name)
                    .description(description)
                    .resource(resource)
                    .action(action)
                    .isActive(true)
                    .build();
            permissionRepository.save(permission);
            log.debug("Created permission: {}", name);
        }
    }
    
    /**
     * Assign default permissions to roles
     */
    private void assignDefaultRolePermissions() {
        // Admin gets all permissions
        assignPermissionsToRole(User.Role.ADMIN, List.of(
                "manage_users", "manage_vendors", "manage_delivery_partners", 
                "view_reports", "manage_system", "manage_own_profile"
        ));
        
        // Vendor permissions
        assignPermissionsToRole(User.Role.VENDOR, List.of(
                "manage_own_profile", "manage_menu", "manage_orders", "view_own_reports"
        ));
        
        // Delivery partner permissions
        assignPermissionsToRole(User.Role.DELIVERY_PARTNER, List.of(
                "manage_own_profile", "manage_deliveries", "update_location"
        ));
        
        // Customer permissions
        assignPermissionsToRole(User.Role.CUSTOMER, List.of(
                "manage_own_profile", "place_orders", "view_order_history"
        ));
    }
    
    /**
     * Assign permissions to a role
     */
    private void assignPermissionsToRole(User.Role role, List<String> permissionNames) {
        for (String permissionName : permissionNames) {
            Permission permission = permissionRepository.findByName(permissionName).orElse(null);
            if (permission != null && !rolePermissionRepository.existsByRoleAndPermissionId(role, permission.getId())) {
                RolePermission rolePermission = RolePermission.builder()
                        .role(role)
                        .permission(permission)
                        .isActive(true)
                        .build();
                rolePermissionRepository.save(rolePermission);
                log.debug("Assigned permission {} to role {}", permissionName, role);
            }
        }
    }
} 