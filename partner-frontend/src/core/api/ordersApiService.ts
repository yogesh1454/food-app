import { httpClient } from './httpClient';
import {
  ApiResponse,
  Order,
  OrderStatusUpdateRequest,
  DashboardStats,
  TopItem
} from '../types/api';

// Orders API Service - NOT IMPLEMENTED YET
// This is a placeholder service for future order management implementation
export class OrdersApiService {
  
  // NOTE: Order management is NOT implemented in the backend yet
  // All methods will throw errors until backend implementation is complete

  async getOrders(
    branchId: number,
    options?: {
      status?: Order['status'];
      page?: number;
      size?: number;
      dateFrom?: string;
      dateTo?: string;
    }
  ): Promise<ApiResponse<any>> {
    throw new Error('Order management is not implemented in backend yet. This feature will be available in future updates.');
  }

  async getOrder(orderId: string): Promise<ApiResponse<Order>> {
    throw new Error('Order management is not implemented in backend yet. This feature will be available in future updates.');
  }

  async updateOrderStatus(
    orderId: string,
    statusData: OrderStatusUpdateRequest
  ): Promise<ApiResponse<Order>> {
    throw new Error('Order management is not implemented in backend yet. This feature will be available in future updates.');
  }

  async getDashboardStats(
    branchId: number,
    dateRange?: string
  ): Promise<ApiResponse<DashboardStats>> {
    throw new Error('Order analytics is not implemented in backend yet. This feature will be available in future updates.');
  }

  async getTopItems(
    branchId: number,
    options?: {
      period?: string;
      limit?: number;
    }
  ): Promise<ApiResponse<TopItem[]>> {
    throw new Error('Order analytics is not implemented in backend yet. This feature will be available in future updates.');
  }

  // Placeholder methods for future implementation
  async cancelOrder(orderId: string, reason?: string): Promise<ApiResponse<Order>> {
    throw new Error('Order cancellation is not implemented in backend yet.');
  }

  async assignDeliveryPartner(orderId: string, partnerId: string): Promise<ApiResponse<Order>> {
    throw new Error('Delivery assignment is not implemented in backend yet.');
  }

  async updateDeliveryTime(orderId: string, estimatedTime: string): Promise<ApiResponse<Order>> {
    throw new Error('Delivery time updates are not implemented in backend yet.');
  }

  async acceptOrder(orderId: string, estimatedPrepTime?: number): Promise<ApiResponse<Order>> {
    throw new Error('Order acceptance is not implemented in backend yet.');
  }

  async rejectOrder(orderId: string, reason: string): Promise<ApiResponse<Order>> {
    throw new Error('Order rejection is not implemented in backend yet.');
  }

  async markOrderReady(orderId: string): Promise<ApiResponse<Order>> {
    throw new Error('Order status updates are not implemented in backend yet.');
  }

  async bulkUpdateOrderStatus(orderIds: string[], status: Order['status']): Promise<ApiResponse<Order[]>> {
    throw new Error('Bulk order updates are not implemented in backend yet.');
  }

  async searchOrders(
    branchId: number,
    query: string,
    options?: {
      page?: number;
      size?: number;
    }
  ): Promise<ApiResponse<any>> {
    throw new Error('Order search is not implemented in backend yet.');
  }
}

// Export singleton instance for future use
export const ordersApiService = new OrdersApiService();
export default ordersApiService;

// NOTE: This service is intentionally throwing errors for all methods
// because the order management system is not implemented in the backend yet.
// When the backend implements order management, this service should be updated
// to match the actual API endpoints.