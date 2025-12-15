import { httpClient } from './httpClient';
import {
    ApiResponse,
    CheckoutRequest,
    CheckoutResponse,
    CommitCheckoutRequest,
    Order,
} from '../types/api';

/**
 * Checkout API Service
 * 
 * Handles checkout calculations and order creation:
 * - Calculate checkout (validate cart, calculate pricing)
 * - Commit checkout (create order)
 * - Get checkout session
 * - Health check
 */
export class CheckoutApiService {

    /**
     * Calculate checkout
     * POST /api/v1/checkout/calculate
     * 
     * Validates cart items, calculates pricing, and creates a checkout session.
     * This is an idempotent operation.
     * 
     * @param request - Checkout request with cart items and delivery details
     * @returns Checkout response with pricing and validation results
     */
    async calculateCheckout(request: CheckoutRequest): Promise<ApiResponse<CheckoutResponse>> {
        const response = await httpClient.post<CheckoutResponse>('/checkout/calculate', request);

        return {
            data: response.data,
            success: true,
            status: response.status,
        };
    }

    /**
     * Commit checkout
     * POST /api/v1/checkout/commit
     * 
     * Commit a checkout session to create an order.
     * This converts the session into an actual order.
     * 
     * @param request - Commit request with session ID and payment details
     * @returns Created order
     */
    async commitCheckout(request: CommitCheckoutRequest): Promise<ApiResponse<Order>> {
        const response = await httpClient.post<Order>('/checkout/commit', request);

        return {
            data: response.data,
            success: true,
            status: response.status,
        };
    }

    /**
     * Get checkout session
     * GET /api/v1/checkout/session/{sessionId}
     * 
     * Retrieve an existing checkout session by ID
     * 
     * @param sessionId - Checkout session ID
     * @returns Checkout session details
     */
    async getCheckoutSession(sessionId: string): Promise<ApiResponse<CheckoutResponse>> {
        const response = await httpClient.get<CheckoutResponse>(`/checkout/session/${sessionId}`);

        return {
            data: response.data,
            success: true,
            status: response.status,
        };
    }

    /**
     * Health check
     * GET /api/v1/checkout/health
     * 
     * Check if checkout service is healthy
     * 
     * @returns Health status message
     */
    async healthCheck(): Promise<ApiResponse<string>> {
        const response = await httpClient.get<string>('/checkout/health');

        return {
            data: response.data,
            success: true,
            status: response.status,
        };
    }
}

// Export singleton instance
export const checkoutApiService = new CheckoutApiService();
export default checkoutApiService;
