import React, { useEffect } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Button } from './Button';
import { colors, spacing } from '../constants';

interface ErrorHandlerProps {
  error: string | null;
  onRetry?: () => void;
  onDismiss?: () => void;
  showRetry?: boolean;
  showDismiss?: boolean;
}

export const ErrorHandler: React.FC<ErrorHandlerProps> = ({
  error,
  onRetry,
  onDismiss,
  showRetry = true,
  showDismiss = true,
}) => {
  if (!error) return null;

  return (
    <View style={styles.container}>
      <View style={styles.errorBox}>
        <Text style={styles.errorTitle}>Error</Text>
        <Text style={styles.errorMessage}>{error}</Text>
        
        <View style={styles.buttonContainer}>
          {showRetry && onRetry && (
            <Button
              title="Retry"
              variant="outline"
              onPress={onRetry}
              style={styles.button}
            />
          )}
          {showDismiss && onDismiss && (
            <Button
              title="Dismiss"
              variant="ghost"
              onPress={onDismiss}
              style={styles.button}
            />
          )}
        </View>
      </View>
    </View>
  );
};

// Hook for handling API errors consistently
export const useErrorHandler = () => {
  const handleError = (error: any, customMessage?: string): string => {
    if (typeof error === 'string') {
      return error;
    }

    if (error?.message) {
      return error.message;
    }

    if (error?.response?.data?.message) {
      return error.response.data.message;
    }

    if (error?.response?.data?.errors?.length > 0) {
      return error.response.data.errors.join(', ');
    }

    return customMessage || 'An unexpected error occurred. Please try again.';
  };

  const isNetworkError = (error: any): boolean => {
    return error?.code === 'NETWORK_ERROR' || !error?.response;
  };

  const isAuthError = (error: any): boolean => {
    return error?.status === 401 || error?.response?.status === 401;
  };

  const isValidationError = (error: any): boolean => {
    return error?.status === 422 || error?.response?.status === 422;
  };

  const isServerError = (error: any): boolean => {
    const status = error?.status || error?.response?.status;
    return status >= 500;
  };

  return {
    handleError,
    isNetworkError,
    isAuthError,
    isValidationError,
    isServerError,
  };
};

const styles = StyleSheet.create({
  container: {
    padding: spacing.md,
  },
  errorBox: {
    backgroundColor: colors.errorLight,
    borderColor: colors.error,
    borderWidth: 1,
    borderRadius: spacing.md,
    padding: spacing.md,
  },
  errorTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.error,
    marginBottom: spacing.sm,
  },
  errorMessage: {
    fontSize: 14,
    color: colors.text,
    marginBottom: spacing.md,
    lineHeight: 20,
  },
  buttonContainer: {
    flexDirection: 'row',
    gap: spacing.sm,
  },
  button: {
    flex: 1,
  },
});

export default ErrorHandler;