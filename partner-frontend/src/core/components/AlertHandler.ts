import { Alert } from 'react-native';

// Simple alert context for global alert handling
interface AlertContextType {
  showAlert: (title: string, message: string, type?: 'success' | 'error' | 'warning' | 'info') => void;
  showSuccess: (title: string, message: string) => void;
  showError: (title: string, message: string) => void;
  showWarning: (title: string, message: string) => void;
  showInfo: (title: string, message: string) => void;
}

// Alert context and provider for global alert handling
class AlertContextClass {
  showAlert = (title: string, message: string, type: 'success' | 'error' | 'warning' | 'info' = 'info') => {
    const icon = type === 'success' ? '✓ ' : type === 'error' ? '✕ ' : type === 'warning' ? '⚠ ' : 'ℹ ';
    Alert.alert(`${icon}${title}`, message);
  };

  showSuccess = (title: string, message: string) => {
    this.showAlert(title, message, 'success');
  };

  showError = (title: string, message: string) => {
    this.showAlert(title, message, 'error');
  };

  showWarning = (title: string, message: string) => {
    this.showAlert(title, message, 'warning');
  };

  showInfo = (title: string, message: string) => {
    this.showAlert(title, message, 'info');
  };
}

// Create a singleton instance
export const alertContext = new AlertContextClass();

// Convenience hooks for different alert types
export const useAlert = () => alertContext;

// Utility function for quick alerts without context
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

export default alertContext;