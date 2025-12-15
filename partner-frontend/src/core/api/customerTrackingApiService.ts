import { httpClient } from './httpClient';
import {
    ApiResponse,
    CustomerStatusResponseDTO,
} from '../types/api';

/**
 * Customer Order Tracking API Service
 * 
 * Handles customer-facing order tracking:
 * - Get simplified order status with ETA and progress
 */
export class CustomerTrackingApiService {

    /**
     * Get order status
     * GET /api/v1/customers/{customerId}/orders/{orderId}/status
     * 
     * Get simplified order status with ETA, progress, and rider info
     * 
     * @param customerId - Customer ID (UUID)
     * @param orderId - Order ID (UUID)
     * @returns Customer-facing order status
     */
    async getOrderStatus(
        customerId: string,
        orderId: string
    ): Promise<ApiResponse<CustomerStatusResponseDTO>> {
        const response = await httpClient.get<CustomerStatusResponseDTO>(
            `/customers/${customerId}/orders/${orderId}/status`
        );

        return {
            data: response.data,
            success: true,
            status: response.status,
        };
    }
}

// Export singleton instance
export const customerTrackingApiService = new CustomerTrackingApiService();
export default customerTrackingApiService;
