import { httpClient } from './httpClient';
import {
    ApiResponse,
    OrderResponse,
    AcceptOrderRequest,
    RejectOrderRequest,
} from '../types/api';

/**
 * Vendor Orders API Service
 * 
 * Handles vendor-specific order operations:
 * - List pending orders
 * - Accept orders
 * - Reject orders
 * - Mark orders as ready for pickup
 * 
 * All requests use X-Vendor-Id header (automatically injected by httpClient)
 */
export class VendorOrdersApiService {

    /**
     * List pending orders for the vendor
     * GET /api/v1/vendor/orders
     * 
     * @returns List of orders pending acceptance
     */
    async listPendingOrders(): Promise<ApiResponse<OrderResponse[]>> {
        const response = await httpClient.get<OrderResponse[]>('/vendor/orders');
        return {
            data: response.data,
            success: true,
            status: response.status,
        };
    }

    /**
     * Accept an order and start preparation
     * POST /api/v1/orders/{orderId}/accept
     * 
     * @param orderId - Order ID (UUID)
     * @param estimatedPrepTime - Estimated preparation time in minutes (5-120)
     * @returns Updated order
     */
    async acceptOrder(
        orderId: string,
        estimatedPrepTime: number
    ): Promise<ApiResponse<OrderResponse>> {
        const request: AcceptOrderRequest = {
            estimatedPrepTime,
        };

        const response = await httpClient.post<OrderResponse>(
            `/orders/${orderId}/accept`,
            request
        );

        return {
            data: response.data,
            success: true,
            status: response.status,
        };
    }

    /**
     * Reject an order with a reason
     * POST /api/v1/orders/{orderId}/reject
     * 
     * @param orderId - Order ID (UUID)
     * @param reason - Rejection reason
     * @returns Updated order
     */
    async rejectOrder(
        orderId: string,
        reason: string
    ): Promise<ApiResponse<OrderResponse>> {
        const request: RejectOrderRequest = {
            reason,
        };

        const response = await httpClient.post<OrderResponse>(
            `/orders/${orderId}/reject`,
            request
        );

        return {
            data: response.data,
            success: true,
            status: response.status,
        };
    }

    /**
     * Mark order as ready for pickup
     * POST /api/v1/orders/{orderId}/ready
     * 
     * @param orderId - Order ID (UUID)
     * @returns Updated order
     */
    async markOrderReady(orderId: string): Promise<ApiResponse<OrderResponse>> {
        const response = await httpClient.post<OrderResponse>(
            `/orders/${orderId}/ready`
        );

        return {
            data: response.data,
            success: true,
            status: response.status,
        };
    }

    /**
     * Helper: Get order by ID (vendor context)
     * This uses the general orders endpoint but with vendor auth
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
     * Register device push token
     * POST /api/v1/vendor/push-token
     */
    async registerPushToken(token: string): Promise<void> {
        try {
            await httpClient.post('/vendor/push-token', { token });
        } catch (error) {
            console.error('Failed to register push token:', error);
            // Fail silently as this is not critical for app function
        }
    }
}

// Export singleton instance
export const vendorOrdersApiService = new VendorOrdersApiService();
export default vendorOrdersApiService;
