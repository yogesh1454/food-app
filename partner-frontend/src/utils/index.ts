import { Dimensions, Platform, PixelRatio } from 'react-native';

const { width: screenWidth, height: screenHeight } = Dimensions.get('window');

// Screen dimensions
export const SCREEN_WIDTH = screenWidth;
export const SCREEN_HEIGHT = screenHeight;

// Check if device is tablet
export const isTablet = () => {
  const pixelDensity = PixelRatio.get();
  const adjustedWidth = screenWidth * pixelDensity;
  const adjustedHeight = screenHeight * pixelDensity;

  return Math.sqrt(adjustedWidth ** 2 + adjustedHeight ** 2) >= 1400;
};

// Check if device is small screen
export const isSmallScreen = () => screenWidth < 375;

// Platform utilities
export const isIOS = Platform.OS === 'ios';
export const isAndroid = Platform.OS === 'android';

// Format currency
export const formatCurrency = (amount: number, currency: string = '₹') => {
  return `${currency}${amount.toLocaleString()}`;
};

// Format date
export const formatDate = (date: string | Date, options?: Intl.DateTimeFormatOptions) => {
  const defaultOptions: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  };

  return new Date(date).toLocaleDateString('en-IN', { ...defaultOptions, ...options });
};

// Format time ago
export const formatTimeAgo = (date: string | Date) => {
  const now = new Date();
  const past = new Date(date);
  const diffInSeconds = Math.floor((now.getTime() - past.getTime()) / 1000);

  if (diffInSeconds < 60) return 'Just now';
  if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} min ago`;
  if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)} hour ago`;
  if (diffInSeconds < 2592000) return `${Math.floor(diffInSeconds / 86400)} day ago`;

  return formatDate(date);
};

// Validate email
export const isValidEmail = (email: string) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

// Validate phone number (Indian format)
export const isValidPhoneNumber = (phone: string) => {
  const phoneRegex = /^[6-9]\d{9}$/;
  return phoneRegex.test(phone.replace(/\D/g, ''));
};

// Generate order ID
export const generateOrderId = () => {
  return `ORD${Date.now()}${Math.floor(Math.random() * 1000)}`;
};

// Debounce function
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let timeout: NodeJS.Timeout;

  return (...args: Parameters<T>) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  };
};

// Throttle function
export const throttle = <T extends (...args: any[]) => any>(
  func: T,
  limit: number
): ((...args: Parameters<T>) => void) => {
  let inThrottle: boolean;

  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      func(...args);
      inThrottle = true;
      setTimeout(() => (inThrottle = false), limit);
    }
  };
};

// Haptic feedback (if available)
export const triggerHapticFeedback = (type: 'light' | 'medium' | 'heavy' = 'light') => {
  // Implementation would depend on expo-haptics or react-native-haptic-feedback
  // For now, just a placeholder
  console.log(`Haptic feedback: ${type}`);
};

// Show toast message
export const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  // Implementation would depend on expo-toast or similar library
  console.log(`Toast: ${type} - ${message}`);
};

// Responsive font size
export const responsiveFontSize = (size: number) => {
  const scale = screenWidth / 375; // Based on iPhone 6/7/8 width
  return Math.round(size * scale);
};

// Responsive spacing
export const responsiveSpacing = (size: number) => {
  const scale = screenWidth / 375;
  return Math.round(size * scale);
};

// Check if user is online
export const isOnline = () => {
  // Implementation would depend on netinfo library
  return true; // Placeholder
};

// Storage utilities (using AsyncStorage)
export const storage = {
  setItem: async (key: string, value: string) => {
    // Implementation would use AsyncStorage
    console.log(`Storage set: ${key} = ${value}`);
  },
  getItem: async (key: string) => {
    // Implementation would use AsyncStorage
    return null;
  },
  removeItem: async (key: string) => {
    // Implementation would use AsyncStorage
    console.log(`Storage remove: ${key}`);
  },
};

// Error handler
export const handleError = (error: any, context?: string) => {
  console.error(`Error${context ? ` in ${context}` : ''}:`, error);

  // In production, you might want to send this to a logging service
  // showToast('Something went wrong. Please try again.', 'error');
};

// Loading state helper
export const createLoadingState = () => {
  return {
    isLoading: false,
    error: null as string | null,
    setLoading: (loading: boolean) => {},
    setError: (error: string | null) => {},
  };
};