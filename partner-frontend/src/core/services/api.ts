import { httpClient } from '../api/httpClient';

export interface ApiResponse<T = any> {
  data: T;
  message: string;
  success: boolean;
}

export interface PaginatedResponse<T = any> {
  data: T[];
  total: number;
  page: number;
  limit: number;
  hasNext: boolean;
  hasPrev: boolean;
}

class ApiService {
  private baseURL: string;

  constructor(baseURL: string = 'https://api.nashtto.com') {
    this.baseURL = baseURL;
  }

  // Auth endpoints
  async login(credentials: { email: string; password: string }): Promise<ApiResponse<{ token: string; user: any }>> {
    const response = await httpClient.post(`${this.baseURL}/auth/login`, credentials);
    return response.data;
  }

  async register(userData: { name: string; email: string; password: string }): Promise<ApiResponse<{ token: string; user: any }>> {
    const response = await httpClient.post(`${this.baseURL}/auth/register`, userData);
    return response.data;
  }

  async logout(): Promise<ApiResponse> {
    const response = await httpClient.post(`${this.baseURL}/auth/logout`);
    return response.data;
  }

  // Restaurant endpoints
  async getRestaurant(id: string): Promise<ApiResponse<any>> {
    const response = await httpClient.get(`${this.baseURL}/restaurants/${id}`);
    return response.data;
  }

  async updateRestaurant(id: string, data: any): Promise<ApiResponse<any>> {
    const response = await httpClient.put(`${this.baseURL}/restaurants/${id}`, data);
    return response.data;
  }

  // Menu endpoints
  async getMenuItems(restaurantId: string): Promise<ApiResponse<any[]>> {
    const response = await httpClient.get(`${this.baseURL}/restaurants/${restaurantId}/menu`);
    return response.data;
  }

  async createMenuItem(restaurantId: string, item: any): Promise<ApiResponse<any>> {
    const response = await httpClient.post(`${this.baseURL}/restaurants/${restaurantId}/menu`, item);
    return response.data;
  }

  async updateMenuItem(restaurantId: string, itemId: string, item: any): Promise<ApiResponse<any>> {
    const response = await httpClient.put(`${this.baseURL}/restaurants/${restaurantId}/menu/${itemId}`, item);
    return response.data;
  }

  async deleteMenuItem(restaurantId: string, itemId: string): Promise<ApiResponse> {
    const response = await httpClient.delete(`${this.baseURL}/restaurants/${restaurantId}/menu/${itemId}`);
    return response.data;
  }

  // Order endpoints
  async getOrders(restaurantId: string, params?: { page?: number; limit?: number; status?: string }): Promise<PaginatedResponse<any>> {
    const response = await httpClient.get(`${this.baseURL}/restaurants/${restaurantId}/orders`, { params });
    return response.data;
  }

  async getOrder(restaurantId: string, orderId: string): Promise<ApiResponse<any>> {
    const response = await httpClient.get(`${this.baseURL}/restaurants/${restaurantId}/orders/${orderId}`);
    return response.data;
  }

  async updateOrderStatus(restaurantId: string, orderId: string, status: string): Promise<ApiResponse<any>> {
    const response = await httpClient.patch(`${this.baseURL}/restaurants/${restaurantId}/orders/${orderId}/status`, { status });
    return response.data;
  }

  // Profile endpoints
  async getProfile(): Promise<ApiResponse<any>> {
    const response = await httpClient.get(`${this.baseURL}/profile`);
    return response.data;
  }

  async updateProfile(data: any): Promise<ApiResponse<any>> {
    const response = await httpClient.put(`${this.baseURL}/profile`, data);
    return response.data;
  }

  async changePassword(data: { currentPassword: string; newPassword: string }): Promise<ApiResponse> {
    const response = await httpClient.post(`${this.baseURL}/profile/change-password`, data);
    return response.data;
  }
}

export const apiService = new ApiService();
export default apiService;