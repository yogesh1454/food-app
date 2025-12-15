/**
 * Authentication Setup Helper
 * 
 * This module provides utilities for setting up authentication headers
 * for the Order Catalog & Delivery Management Service API.
 * 
 * Usage: Call setupVendorAuth() when the app starts or when the vendor logs in.
 */

import { httpClient } from '../api/httpClient';

/**
 * Setup authentication for a vendor/restaurant
 * This should be called when the vendor logs in or on app startup
 * 
 * @param vendorId - The vendor/restaurant ID (use your vendorId from backend)
 * @example
 * ```typescript
 * import { setupVendorAuth } from '@/core/utils/authSetup';
 * 
 * // In your login screen or App.tsx
 * setupVendorAuth('1'); // Your vendor ID from backend
 * ```
 */
export function setupVendorAuth(vendorId: string) {
    httpClient.setRestaurantId(vendorId);
    console.log(`[Auth] Vendor authentication set for ID: ${vendorId}`);
}

/**
 * Setup authentication for a customer
 * This should be called when a customer logs in
 * 
 * @param customerId - The customer ID (UUID)
 */
export function setupCustomerAuth(customerId: string) {
    httpClient.setCustomerId(customerId);
    console.log(`[Auth] Customer authentication set for ID: ${customerId}`);
}

/**
 * Setup authentication for a rider
 * This should be called when a rider logs in
 * 
 * @param riderId - The rider ID (UUID)
 */
export function setupRiderAuth(riderId: string) {
    httpClient.setRiderId(riderId);
    console.log('[Auth] Rider authentication set for ID: ${ riderId}');
}

/**
 * Clear all authentication headers
 * Call this when a user logs out
 */
export function clearAuth() {
    httpClient.clearAuthHeaders();
    console.log('[Auth] All authentication headers cleared');
}

/**
 * Get quick start instructions for testing
 */
export function getQuickStartInstructions() {
    return `
  Quick Start - Testing Vendor Orders API:
  
  1. Set your vendor/restaurant ID:
     import { setupVendorAuth } from '@/core/utils/authSetup';
     setupVendorAuth('1'); // Use your actual vendor ID
  
  2. Dispatch the fetchOrders action:
     import { fetchOrders } from '@/store/slices/ordersSlice';
     dispatch(fetchOrders());
  
  3. Accept an order:
     import { acceptOrder } from '@/store/slices/ordersSlice';
     dispatch(acceptOrder({ orderId: 'order-uuid', estimatedPrepTime: 15 }));
  
  4. Mark order ready:
     import { markOrderReady } from '@/store/slices/ordersSlice';
     dispatch(markOrderReady('order-uuid'));
  `;
}
