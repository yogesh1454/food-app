import { useCallback } from 'react';
import { Alert, AlertButton } from 'react-native';

// Alert types for better type safety
export type AlertType = 'default' | 'success' | 'error' | 'warning' | 'info';

export interface AlertConfig {
  title: string;
  message: string;
  type?: AlertType;
  buttons?: AlertButton[];
  dismissable?: boolean;
}

// Icon mapping for different alert types
const getAlertIcon = (type: AlertType = 'default'): string => {
  switch (type) {
    case 'success':
      return '✓';
    case 'error':
      return '✕';
    case 'warning':
      return '⚠';
    case 'info':
      return 'ℹ';
    default:
      return '';
  }
};

// Alert button configurations
const getDefaultButtons = (dismissable: boolean = true): AlertButton[] => {
  if (dismissable) {
    return [{ text: 'OK', style: 'default' }];
  }
  return [];
};

// Alert hook for consistent alert handling across the app
export const useAlert = () => {
  const showAlert = useCallback((config: AlertConfig) => {
    const { title, message, type = 'default', buttons, dismissable = true } = config;
    
    const titleWithIcon = type !== 'default' 
      ? `${getAlertIcon(type)} ${title}`
      : title;
    
    Alert.alert(
      titleWithIcon,
      message,
      buttons || getDefaultButtons(dismissable)
    );
  }, []);

  const showSuccess = useCallback((title: string, message: string) => {
    showAlert({ title, message, type: 'success' });
  }, [showAlert]);

  const showError = useCallback((title: string, message: string) => {
    showAlert({ title, message, type: 'error' });
  }, [showAlert]);

  const showWarning = useCallback((title: string, message: string) => {
    showAlert({ title, message, type: 'warning' });
  }, [showAlert]);

  const showInfo = useCallback((title: string, message: string) => {
    showAlert({ title, message, type: 'info' });
  }, [showAlert]);

  const showConfirmation = useCallback((
    title: string,
    message: string,
    onConfirm: () => void,
    onCancel?: () => void
  ) => {
    Alert.alert(
      title,
      message,
      [
        { text: 'Cancel', style: 'cancel', onPress: onCancel },
        { text: 'OK', style: 'default', onPress: onConfirm }
      ]
    );
  }, []);

  const showDeleteConfirmation = useCallback((
    itemName: string,
    onConfirm: () => void,
    onCancel?: () => void
  ) => {
    Alert.alert(
      'Confirm Delete',
      `Are you sure you want to delete "${itemName}"? This action cannot be undone.`,
      [
        { text: 'Cancel', style: 'cancel', onPress: onCancel },
        { text: 'Delete', style: 'destructive', onPress: onConfirm }
      ]
    );
  }, []);

  const showLoadingAlert = useCallback((title: string, message: string) => {
    // For loading states, we'll use a different approach since Alert is blocking
    showAlert({ 
      title, 
      message, 
      type: 'info',
      dismissable: false 
    });
  }, [showAlert]);

  return {
    showAlert,
    showSuccess,
    showError,
    showWarning,
    showInfo,
    showConfirmation,
    showDeleteConfirmation,
    showLoadingAlert,
  };
};

// Export default hook
export default useAlert;

// Helper function for quick alerts (without hook)
export const quickAlert = {
  success: (title: string, message: string) => {
    Alert.alert(`✓ ${title}`, message);
  },
  error: (title: string, message: string) => {
    Alert.alert(`✕ ${title}`, message);
  },
  warning: (title: string, message: string) => {
    Alert.alert(`⚠ ${title}`, message);
  },
  info: (title: string, message: string) => {
    Alert.alert(`ℹ ${title}`, message);
  },
  default: (title: string, message: string) => {
    Alert.alert(title, message);
  },
};