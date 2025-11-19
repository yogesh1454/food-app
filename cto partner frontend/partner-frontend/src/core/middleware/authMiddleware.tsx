import React from 'react';
import { useSelector } from 'react-redux';
import { RootState } from '@/store';

// Define route types
export type RootStackParamList = {
  Welcome: undefined;
  Onboarding: undefined;
  Main: undefined;
  UploadTest: undefined;
};

export type TabParamList = {
  Dashboard: undefined;
  Menu: undefined;
  Orders: undefined;
  Profile: undefined;
};

export type ProtectedRoute = keyof TabParamList;
export type PublicRoute = keyof RootStackParamList;

// Route protection logic
export const useAuthMiddleware = () => {
  const { isAuthenticated, isFirstTime, user } = useSelector(
    (state: RootState) => state.auth
  );

  /**
   * Check if user can access a protected route
   */
  const canAccessRoute = (routeName: ProtectedRoute): boolean => {
    // If user is in onboarding flow, deny access to protected routes
    if (isFirstTime) {
      return false;
    }

    // For authenticated users, allow access to all main app routes
    if (isAuthenticated && user) {
      return true;
    }

    // For non-authenticated users who completed onboarding, deny access
    return false;
  };

  /**
   * Get appropriate redirect route for unauthenticated users
   */
  const getRedirectRoute = (): PublicRoute => {
    if (isFirstTime) {
      return 'Welcome';
    }
    if (!isAuthenticated) {
      return 'Welcome';
    }
    return 'Main';
  };

  /**
   * Check if current route should be protected
   */
  const isProtectedRoute = (routeName: string): boolean => {
    const protectedRoutes: ProtectedRoute[] = [
      'Dashboard',
      'Menu', 
      'Orders',
      'Profile'
    ];
    
    return protectedRoutes.includes(routeName as ProtectedRoute);
  };

  /**
   * Validate route access based on current auth state
   */
  const validateRouteAccess = (routeName: string): {
    canAccess: boolean;
    shouldRedirect: boolean;
    redirectRoute?: PublicRoute;
  } => {
    const isProtected = isProtectedRoute(routeName);
    
    if (!isProtected) {
      return { canAccess: true, shouldRedirect: false };
    }

    const canAccess = canAccessRoute(routeName as ProtectedRoute);
    
    if (!canAccess) {
      return {
        canAccess: false,
        shouldRedirect: true,
        redirectRoute: getRedirectRoute()
      };
    }

    return { canAccess: true, shouldRedirect: false };
  };

  return {
    canAccessRoute,
    getRedirectRoute,
    isProtectedRoute,
    validateRouteAccess,
    isAuthenticated,
    isFirstTime,
    user
  };
};

// Component for protected routes
interface ProtectedRouteProps {
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  fallback = null
}) => {
  const { isFirstTime, isAuthenticated } = useAuthMiddleware();
  
  // Don't render children if user is in onboarding or not authenticated
  if (isFirstTime || !isAuthenticated) {
    return <>{fallback}</>;
  }
  
  return <>{children}</>;
};

// Hook for navigation guards
export const useNavigationGuard = (navigation: any) => {
  const { validateRouteAccess } = useAuthMiddleware();
  
  const navigateWithGuard = (routeName: string, params?: any) => {
    const validation = validateRouteAccess(routeName);
    
    if (validation.shouldRedirect && validation.redirectRoute) {
      navigation.navigate(validation.redirectRoute);
      return false;
    }
    
    if (validation.canAccess) {
      navigation.navigate(routeName, params);
      return true;
    }
    
    return false;
  };
  
  return { navigateWithGuard };
};