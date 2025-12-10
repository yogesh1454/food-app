import React, { Component, ReactNode, ErrorInfo } from 'react';
import { View, Text, StyleSheet, Alert } from 'react-native';
import Button from './Button';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
  showDetails?: boolean;
  resetOnPropsChange?: boolean;
}

interface State {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
}

export class ErrorBoundary extends Component<Props, State> {
  private retryTimeoutId: NodeJS.Timeout | null = null;

  constructor(props: Props) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
  }

  static getDerivedStateFromError(error: Error): State {
    // Update state so the next render will show the fallback UI
    return {
      hasError: true,
      error,
      errorInfo: null,
    };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    // Log the error to console and external service
    console.error('ErrorBoundary caught an error:', error, errorInfo);
    
    // Update state with error info
    this.setState({
      error,
      errorInfo,
    });

    // Call custom error handler if provided
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }

    // Send error to monitoring service (e.g., Sentry, Crashlytics)
    this.reportError(error, errorInfo);

    // Auto-retry after delay
    this.scheduleRetry();
  }

  componentWillUnmount() {
    if (this.retryTimeoutId) {
      clearTimeout(this.retryTimeoutId);
    }
  }

  private scheduleRetry = () => {
    // Auto-retry after 5 seconds
    this.retryTimeoutId = setTimeout(() => {
      this.handleRetry();
    }, 5000);
  };

  private reportError = (error: Error, errorInfo: ErrorInfo) => {
    // In a real app, you would send this to your error monitoring service
    const errorReport = {
      message: error.message,
      stack: error.stack,
      componentStack: errorInfo.componentStack,
      timestamp: new Date().toISOString(),
      userAgent: navigator.userAgent,
      url: window.location.href,
    };

    // Example: Send to external service
    if (typeof window !== 'undefined') {
      // Log to localStorage for debugging
      try {
        const existingErrors = JSON.parse(localStorage.getItem('errorReports') || '[]');
        existingErrors.push(errorReport);
        localStorage.setItem('errorReports', JSON.stringify(existingErrors));
      } catch (e) {
        console.error('Failed to store error report:', e);
      }
    }

    // Example: Send to monitoring service
    // Sentry.captureException(error, { contexts: { react: errorInfo } });
    // crashlytics().recordError(error);
  };

  private handleRetry = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
    });
    
    if (this.retryTimeoutId) {
      clearTimeout(this.retryTimeoutId);
      this.retryTimeoutId = null;
    }
  };

  private handleReport = () => {
    const { error, errorInfo } = this.state;
    if (error && errorInfo) {
      Alert.alert(
        'Report Error',
        'Error details have been copied to clipboard for support.',
        [
          { text: 'OK', style: 'default' },
          { 
            text: 'Copy Details', 
            onPress: () => {
              const errorDetails = `Error: ${error.message}\n\nStack: ${error.stack}\n\nComponent Stack: ${errorInfo.componentStack}`;
              if (navigator.clipboard) {
                navigator.clipboard.writeText(errorDetails);
              }
            }
          }
        ]
      );
    }
  };

  render() {
    if (this.state.hasError) {
      // Custom fallback UI
      if (this.props.fallback) {
        return this.props.fallback;
      }

      // Default error UI
      return (
        <View style={styles.container}>
          <View style={styles.content}>
            <Text style={styles.title}>Oops! Something went wrong</Text>
            <Text style={styles.description}>
              We encountered an unexpected error. Don't worry, this has been reported automatically.
            </Text>
            
            <View style={styles.buttonContainer}>
              <Button
                variant="primary"
                onPress={this.handleRetry}
                style={styles.button}
              >
                Try Again
              </Button>
              
              <Button
                variant="outline"
                onPress={this.handleReport}
                style={styles.button}
              >
                Report Issue
              </Button>
            </View>

            {this.props.showDetails && __DEV__ && (
              <View style={styles.detailsContainer}>
                <Text style={styles.detailsTitle}>Error Details (Development)</Text>
                <Text style={styles.errorText}>{this.state.error?.message}</Text>
                {this.state.errorInfo?.componentStack && (
                  <Text style={styles.componentStack}>
                    Component Stack: {this.state.errorInfo.componentStack}
                  </Text>
                )}
              </View>
            )}
          </View>
        </View>
      );
    }

    return this.props.children;
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#ffffff',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  content: {
    maxWidth: 400,
    alignItems: 'center',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#1e293b',
    textAlign: 'center',
    marginBottom: 12,
  },
  description: {
    fontSize: 16,
    color: '#64748b',
    textAlign: 'center',
    marginBottom: 24,
    lineHeight: 24,
  },
  buttonContainer: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 20,
  },
  button: {
    minWidth: 120,
  },
  detailsContainer: {
    marginTop: 20,
    padding: 16,
    backgroundColor: '#f1f5f9',
    borderRadius: 8,
    width: '100%',
  },
  detailsTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#374151',
    marginBottom: 8,
  },
  errorText: {
    fontSize: 12,
    color: '#dc2626',
    fontFamily: 'monospace',
    marginBottom: 8,
  },
  componentStack: {
    fontSize: 11,
    color: '#6b7280',
    fontFamily: 'monospace',
  },
});

// Higher-order component version
export const withErrorBoundary = <P extends object>(
  Component: React.ComponentType<P>,
  errorBoundaryProps?: Omit<Props, 'children'>
) => {
  const WrappedComponent = (props: P) => (
    <ErrorBoundary {...errorBoundaryProps}>
      <Component {...props} />
    </ErrorBoundary>
  );

  WrappedComponent.displayName = `withErrorBoundary(${Component.displayName || Component.name})`;

  return WrappedComponent;
};

// Hook for error reporting
export const useErrorReporting = () => {
  const reportError = React.useCallback((error: Error, context?: Record<string, any>) => {
    const errorReport = {
      message: error.message,
      stack: error.stack,
      context,
      timestamp: new Date().toISOString(),
    };

    // Log to console
    console.error('Reported Error:', errorReport);

    // Store in localStorage for debugging
    try {
      const existingErrors = JSON.parse(localStorage.getItem('errorReports') || '[]');
      existingErrors.push(errorReport);
      // Keep only last 50 errors
      if (existingErrors.length > 50) {
        existingErrors.splice(0, existingErrors.length - 50);
      }
      localStorage.setItem('errorReports', JSON.stringify(existingErrors));
    } catch (e) {
      console.error('Failed to store error report:', e);
    }

    // In a real app, send to monitoring service
    // Sentry.captureException(error, { extra: context });
  }, []);

  return { reportError };
};