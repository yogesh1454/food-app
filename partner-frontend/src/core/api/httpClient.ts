import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import { Alert } from 'react-native';

// Configuration constants
const API_BASE_URL = 'https://api.nashtto.com'; // Replace with actual API URL
const DEFAULT_TIMEOUT = 30000; // 30 seconds

// Error types for better error handling
export interface ApiError {
  message: string;
  code?: string;
  status?: number;
  data?: any;
}

export interface ApiResponse<T = any> {
  data: T;
  success: boolean;
  message?: string;
  status?: number;
}

// HTTP Client class
class HttpClient {
  private instance: AxiosInstance;

  constructor() {
    this.instance = axios.create({
      baseURL: API_BASE_URL,
      timeout: DEFAULT_TIMEOUT,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor
    this.instance.interceptors.request.use(
      (config) => {
        // Add auth token if available
        const token = this.getAuthToken();
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        
        console.log(`[HTTP] ${config.method?.toUpperCase()} ${config.url}`);
        return config;
      },
      (error) => {
        console.error('[HTTP] Request Error:', error);
        return Promise.reject(error);
      }
    );

    // Response interceptor
    this.instance.interceptors.response.use(
      (response: AxiosResponse) => {
        console.log(`[HTTP] Response: ${response.status} ${response.config.url}`);
        return response;
      },
      (error: AxiosError) => {
        console.error('[HTTP] Response Error:', error.response?.status, error.message);
        
        // Handle common HTTP errors
        this.handleHttpError(error);
        
        return Promise.reject(error);
      }
    );
  }

  private getAuthToken(): string | null {
    // TODO: Implement token retrieval from secure storage
    // This is a mock implementation
    return null;
  }

  private handleHttpError(error: AxiosError) {
    if (!error.response) {
      // Network error or timeout
      Alert.alert(
        'Connection Error',
        'Please check your internet connection and try again.'
      );
      return;
    }

    const { status, data } = error.response;

    switch (status) {
      case 401:
        // Unauthorized - redirect to login
        this.handleUnauthorized();
        break;
      
      case 403:
        // Forbidden
        Alert.alert(
          'Access Denied',
          'You do not have permission to perform this action.'
        );
        break;
      
      case 404:
        // Not Found
        Alert.alert(
          'Not Found',
          'The requested resource was not found.'
        );
        break;
      
      case 422:
        // Validation Error
        if (data && typeof data === 'object' && 'errors' in data) {
          const errors = (data as any).errors;
          const errorMessage = Array.isArray(errors) 
            ? errors.join('\n') 
            : 'Validation failed';
          Alert.alert('Validation Error', errorMessage);
        } else {
          Alert.alert('Validation Error', 'Please check your input.');
        }
        break;
      
      case 429:
        // Too Many Requests
        Alert.alert(
          'Rate Limited',
          'Too many requests. Please try again later.'
        );
        break;
      
      case 500:
      case 502:
      case 503:
      case 504:
        // Server errors
        Alert.alert(
          'Server Error',
          'Something went wrong on our end. Please try again later.'
        );
        break;
      
      default:
        // Generic error
        const errorData = data as any;
        Alert.alert(
          'Error',
          errorData?.message || 'An unexpected error occurred.'
        );
    }
  }

  private handleUnauthorized() {
    // TODO: Implement logout and redirect to login
    // This is a mock implementation
    Alert.alert(
      'Session Expired',
      'Please log in again to continue.'
    );
  }

  // Generic HTTP methods
  async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.instance.get(url, config);
  }

  async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.instance.post(url, data, config);
  }

  async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.instance.put(url, data, config);
  }

  async patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.instance.patch(url, data, config);
  }

  async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.instance.delete(url, config);
  }

  // Utility methods for common API patterns
  async getApiResponse<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.get<T>(url, config);
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  async postApiResponse<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const response = await this.post<T>(url, data, config);
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  // File upload methods
  async uploadFile<T = any>(
    url: string, 
    file: { uri: string; name: string; type: string }, 
    additionalData?: Record<string, string>
  ): Promise<AxiosResponse<T>> {
    const formData = new FormData();
    formData.append('file', {
      uri: file.uri,
      name: file.name,
      type: file.type,
    } as any);

    // Add additional data if provided
    if (additionalData) {
      Object.entries(additionalData).forEach(([key, value]) => {
        formData.append(key, value);
      });
    }

    return this.instance.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  }

  // Health check method
  async healthCheck(): Promise<boolean> {
    try {
      await this.get('/health');
      return true;
    } catch {
      return false;
    }
  }
}

// Export singleton instance
export const httpClient = new HttpClient();

// Export the class for testing or multiple instances
export { HttpClient };

// Helper function to handle API errors in components
export const handleApiError = (error: any): ApiError => {
  if (error.response) {
    // Server responded with error status
    return {
      message: error.response.data?.message || 'Server error occurred',
      code: error.response.data?.code,
      status: error.response.status,
      data: error.response.data,
    };
  } else if (error.request) {
    // Network error
    return {
      message: 'Network error - please check your connection',
      code: 'NETWORK_ERROR',
    };
  } else {
    // Other error
    return {
      message: error.message || 'An unexpected error occurred',
      code: 'UNKNOWN_ERROR',
    };
  }
};