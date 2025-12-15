/**
 * Unified API Service Index
 * 
 * Central export for all API services in the application.
 * This provides a single import point for accessing all API functionality.
 * 
 * Usage:
 * ```typescript
 * import { vendorApiService, menuApiService, vendorOrdersApiService } from '@/core/api';
 * ```
 */

// HTTP Client
export { httpClient, HttpClient, handleApiError } from './httpClient';
export type { ApiError, ApiResponse } from './httpClient';

// Vendor & Branch Management
export { vendorApiService, VendorApiService } from './vendorApiService';

// Menu Management
export { menuApiService, MenuApiService } from './menuApiService';

// Order Management (Customer perspective)
export { ordersApiService, OrdersApiService } from './ordersApiService';

// Vendor Orders (Vendor perspective)
export { vendorOrdersApiService, VendorOrdersApiService } from './vendorOrdersApiService';

// Checkout
export { checkoutApiService, CheckoutApiService } from './checkoutApiService';

// Delivery Tracking
export { deliveryApiService, DeliveryApiService } from './deliveryApiService';

// Customer Order Tracking
export { customerTrackingApiService, CustomerTrackingApiService } from './customerTrackingApiService';

// Re-export all types from api types
export * from '../types/api';

// Re-export FSM helpers
export * from '../utils/fsmHelpers';
