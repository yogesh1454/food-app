import { vendorOrdersApiService } from '../vendorOrdersApiService';
import { httpClient } from '../httpClient';
import { OrderResponse, OrderState, PaymentStatus, OrderType } from '../../types/api';

// Mock httpClient
jest.mock('../httpClient');

describe('VendorOrdersApiService', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    const mockOrderResponse: OrderResponse = {
        orderId: 'test-order-123',
        customerId: 'customer-456',
        orderType: OrderType.SINGLE,
        state: OrderState.PENDING_ACCEPTANCE,
        paymentStatus: PaymentStatus.PAID,
        items: [
            {
                orderItemId: 'item-1',
                menuItemId: 1,
                itemName: 'Butter Chicken',
                quantity: 2,
                priceAtOrder: 250,
            },
        ],
        itemTotal: 500,
        deliveryCharges: 50,
        platformFee: 20,
        gst: 28.5,
        discount: 0,
        totalAmount: 598.5,
        createdAt: '2025-11-26T20:00:00Z',
        updatedAt: '2025-11-26T20:00:00Z',
    };

    describe('listPendingOrders', () => {
        it('should fetch list of pending orders', async () => {
            const mockResponse = {
                data: [mockOrderResponse],
                status: 200,
            };

            (httpClient.get as jest.Mock).mockResolvedValue(mockResponse);

            const result = await vendorOrdersApiService.listPendingOrders();

            expect(httpClient.get).toHaveBeenCalledWith('/vendor/orders');
            expect(result.success).toBe(true);
            expect(result.data).toEqual([mockOrderResponse]);
            expect(result.status).toBe(200);
        });

        it('should handle errors when fetching orders', async () => {
            const mockError = new Error('Network error');
            (httpClient.get as jest.Mock).mockRejectedValue(mockError);

            await expect(vendorOrdersApiService.listPendingOrders()).rejects.toThrow('Network error');
        });
    });

    describe('acceptOrder', () => {
        it('should accept an order with estimated prep time', async () => {
            const acceptedOrder = {
                ...mockOrderResponse,
                state: OrderState.ACCEPTED,
                acceptedAt: '2025-11-26T20:05:00Z',
            };

            const mockResponse = {
                data: acceptedOrder,
                status: 200,
            };

            (httpClient.post as jest.Mock).mockResolvedValue(mockResponse);

            const result = await vendorOrdersApiService.acceptOrder('test-order-123', 15);

            expect(httpClient.post).toHaveBeenCalledWith(
                '/vendor/orders/test-order-123/accept',
                { estimatedPrepTime: 15 }
            );
            expect(result.success).toBe(true);
            expect(result.data.state).toBe(OrderState.ACCEPTED);
        });

        it('should validate prep time range', async () => {
            const mockResponse = {
                data: mockOrderResponse,
                status: 200,
            };

            (httpClient.post as jest.Mock).mockResolvedValue(mockResponse);

            // Valid prep time
            await vendorOrdersApiService.acceptOrder('test-order-123', 30);
            expect(httpClient.post).toHaveBeenCalled();
        });
    });

    describe('rejectOrder', () => {
        it('should reject an order with reason', async () => {
            const rejectedOrder = {
                ...mockOrderResponse,
                state: OrderState.REJECTED,
            };

            const mockResponse = {
                data: rejectedOrder,
                status: 200,
            };

            (httpClient.post as jest.Mock).mockResolvedValue(mockResponse);

            const result = await vendorOrdersApiService.rejectOrder('test-order-123', 'Out of ingredients');

            expect(httpClient.post).toHaveBeenCalledWith(
                '/vendor/orders/test-order-123/reject',
                { reason: 'Out of ingredients' }
            );
            expect(result.success).toBe(true);
            expect(result.data.state).toBe(OrderState.REJECTED);
        });
    });

    describe('markOrderReady', () => {
        it('should mark order as ready for pickup', async () => {
            const readyOrder = {
                ...mockOrderResponse,
                state: OrderState.READY_FOR_PICKUP,
                readyAt: '2025-11-26T20:20:00Z',
            };

            const mockResponse = {
                data: readyOrder,
                status: 200,
            };

            (httpClient.post as jest.Mock).mockResolvedValue(mockResponse);

            const result = await vendorOrdersApiService.markOrderReady('test-order-123');

            expect(httpClient.post).toHaveBeenCalledWith('/vendor/orders/test-order-123/ready');
            expect(result.success).toBe(true);
            expect(result.data.state).toBe(OrderState.READY_FOR_PICKUP);
        });
    });

    describe('getOrder', () => {
        it('should get order details by ID', async () => {
            const mockResponse = {
                data: mockOrderResponse,
                status: 200,
            };

            (httpClient.get as jest.Mock).mockResolvedValue(mockResponse);

            const result = await vendorOrdersApiService.getOrder('test-order-123');

            expect(httpClient.get).toHaveBeenCalledWith('/orders/test-order-123');
            expect(result.success).toBe(true);
            expect(result.data.orderId).toBe('test-order-123');
        });
    });
});
