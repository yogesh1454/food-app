import { httpClient } from './httpClient';
import {
    ApiResponse,
    DeliveryResponseDTO,
    LocationDTO,
} from '../types/api';

/**
 * Delivery Tracking API Service
 * 
 * Handles delivery tracking operations:
 * - Get delivery details
 * - Get delivery by order ID
 * - Get rider location
 */
export class DeliveryApiService {

    /**
     * Get delivery details
     * GET /api/v1/deliveries/{deliveryId}
     * 
     * Get complete delivery information including status, timestamps, and locations
     * 
     * @param deliveryId - Delivery ID (UUID)
     * @returns Delivery details
     */
    async getDelivery(deliveryId: string): Promise<ApiResponse<DeliveryResponseDTO>> {
        const response = await httpClient.get<DeliveryResponseDTO>(`/deliveries/${deliveryId}`);

        return {
            data: response.data,
            success: true,
            status: response.status,
        };
    }

    /**
     * Get delivery by order ID
     * GET /api/v1/orders/{orderId}/delivery
     * 
     * Get delivery information for a specific order
     * 
     * @param orderId - Order ID (UUID)
     * @returns Delivery details
     */
    async getDeliveryByOrderId(orderId: string): Promise<ApiResponse<DeliveryResponseDTO>> {
        const response = await httpClient.get<DeliveryResponseDTO>(`/orders/${orderId}/delivery`);

        return {
            data: response.data,
            success: true,
            status: response.status,
        };
    }

    /**
     * Get rider location
     * GET /api/v1/deliveries/{deliveryId}/location
     * 
     * Get real-time location of the rider assigned to this delivery
     * 
     * @param deliveryId - Delivery ID (UUID)
     * @returns Rider location
     */
    async getRiderLocation(deliveryId: string): Promise<ApiResponse<LocationDTO>> {
        const response = await httpClient.get<LocationDTO>(`/deliveries/${deliveryId}/location`);

        return {
            data: response.data,
            success: true,
            status: response.status,
        };
    }
}

// Export singleton instance
export const deliveryApiService = new DeliveryApiService();
export default deliveryApiService;
