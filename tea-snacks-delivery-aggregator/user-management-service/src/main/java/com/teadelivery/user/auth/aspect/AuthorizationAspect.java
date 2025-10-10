package com.teadelivery.user.auth.aspect;

import com.teadelivery.user.auth.annotation.HasPermission;
import com.teadelivery.user.auth.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationAspect {
    
    private final AuthorizationService authorizationService;
    
    @Before("@annotation(hasPermission)")
    public void checkPermission(JoinPoint joinPoint, HasPermission hasPermission) {
        String resource = hasPermission.resource();
        String action = hasPermission.action();
        
        log.debug("Checking permission for method: {}:{}", resource, action);
        
        // Check basic permission
        if (!authorizationService.hasPermission(resource, action)) {
            log.warn("Permission denied for {}:{}", resource, action);
            throw new AccessDeniedException("Insufficient permissions for " + resource + ":" + action);
        }
        
        // Check resource ownership if required
        if (hasPermission.checkOwnership()) {
            UUID resourceOwnerId = extractResourceOwnerId(joinPoint, hasPermission.ownerIdParam());
            if (!authorizationService.canAccessResource(resource, action, resourceOwnerId)) {
                log.warn("Resource ownership check failed for {}:{}", resource, action);
                throw new AccessDeniedException("Access denied to resource");
            }
        }
        
        log.debug("Permission check passed for {}:{}", resource, action);
    }
    
    /**
     * Extract resource owner ID from method parameters
     */
    private UUID extractResourceOwnerId(JoinPoint joinPoint, String ownerIdParam) {
        try {
            // If no specific parameter name is provided, try to find UUID parameters
            if (ownerIdParam.isEmpty()) {
                Object[] args = joinPoint.getArgs();
                for (Object arg : args) {
                    if (arg instanceof UUID) {
                        return (UUID) arg;
                    } else if (arg instanceof String) {
                        try {
                            return UUID.fromString((String) arg);
                        } catch (IllegalArgumentException e) {
                            // Not a valid UUID, continue
                        }
                    }
                }
                return null;
            }
            
            // Extract by parameter name using reflection
            Method method = getMethod(joinPoint);
            String[] paramNames = getParameterNames(method);
            Object[] args = joinPoint.getArgs();
            
            for (int i = 0; i < paramNames.length; i++) {
                if (paramNames[i].equals(ownerIdParam)) {
                    Object arg = args[i];
                    if (arg instanceof UUID) {
                        return (UUID) arg;
                    } else if (arg instanceof String) {
                        try {
                            return UUID.fromString((String) arg);
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid UUID format in parameter: {}", arg);
                            return null;
                        }
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error extracting resource owner ID", e);
            return null;
        }
    }
    
    /**
     * Get method from join point
     */
    private Method getMethod(JoinPoint joinPoint) {
        try {
            return joinPoint.getTarget().getClass()
                    .getMethod(joinPoint.getSignature().getName(),
                            ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getParameterTypes());
        } catch (NoSuchMethodException e) {
            log.error("Error getting method from join point", e);
            return null;
        }
    }
    
    /**
     * Get parameter names from method
     */
    private String[] getParameterNames(Method method) {
        if (method == null) {
            return new String[0];
        }
        
        try {
            // This is a simplified approach - in a real implementation,
            // you might want to use a more sophisticated parameter name resolution
            return java.util.Arrays.stream(method.getParameters())
                    .map(java.lang.reflect.Parameter::getName)
                    .toArray(String[]::new);
        } catch (Exception e) {
            log.warn("Could not get parameter names for method: {}", method.getName());
            return new String[0];
        }
    }
} 