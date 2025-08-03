package com.teadelivery.user.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for permission-based authorization
 * Usage: @HasPermission(resource = "users", action = "manage")
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasPermission {
    
    /**
     * The resource being accessed
     */
    String resource();
    
    /**
     * The action being performed
     */
    String action();
    
    /**
     * Whether to check resource ownership
     */
    boolean checkOwnership() default false;
    
    /**
     * The parameter name containing the resource owner ID
     */
    String ownerIdParam() default "";
} 