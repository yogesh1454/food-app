import { httpClient } from './httpClient';
import {
  ApiResponse,
  OrderResponse,
  OrderState,
  CreateOrderFromCheckoutRequest,
  CancelOrderRequest,
} from '../types/api';

/**
 * Orders API Service (Customer Perspective)
 * 
 * Handles customer-facing order operations:
 * - List customer orders
 * - Create order from checkout
 * - Get order details
 * - Cancel order
 * 
 * All requests use X-Customer-Id header (automatically injected by httpClient)
 */
export class OrdersApiService {

  /**
   * List orders for the authenticated customer
   * GET /api/v1/orders
   * 
   * @param state - Optional filter by order state
   * @returns List of orders
   */
  async listOrders(state?: OrderState): Promise<ApiResponse<OrderResponse[]>> {
    const params = state ? `?state=${state}` : '';
    const response = await httpClient.get<OrderResponse[]>(`/orders${params}`);

    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  /**
   * Create order from checkout session
   * POST /api/v1/orders
   * 
   * Second step of two-step checkout: executes payment and creates order
   * 
   * @param checkoutSessionId - Checkout session ID
   * @param paymentToken - Optional payment token
   * @returns Created order
   */
  async createOrder(
    checkoutSessionId: string,
    paymentToken?: string
  ): Promise<ApiResponse<OrderResponse>> {
    const request: CreateOrderFromCheckoutRequest = {
      checkoutSessionId,
      paymentToken,
    };

    const response = await httpClient.post<OrderResponse>('/orders', request);

    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  /**
   * Get order details by ID
   * GET /api/v1/orders/{orderId}
   * 
   * @param orderId - Order ID (UUID)
   * @returns Order details
   */
  async getOrder(orderId: string): Promise<ApiResponse<OrderResponse>> {
    const response = await httpClient.get<OrderResponse>(`/orders/${orderId}`);

    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  /**
   * Cancel an order
   * POST /api/v1/orders/{orderId}/cancel
   * 
   * Can only cancel if order is in a cancellable state
   * 
   * @param orderId - Order ID (UUID)
   * @param reason - Cancellation reason
   * @returns Updated order
   */
  async cancelOrder(
    orderId: string,
    reason: string
  ): Promise<ApiResponse<OrderResponse>> {
    const request: CancelOrderRequest = {
      reason,
      cancelledBy: 'CUSTOMER',
    };

    const response = await httpClient.post<OrderResponse>(
      `/orders/${orderId}/cancel`,
      request
    );

    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }
}

// Export singleton instance
export const ordersApiService = new OrdersApiService();
export default ordersApiService;