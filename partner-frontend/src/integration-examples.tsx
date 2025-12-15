/**
 * INTEGRATION EXAMPLE - How to Use the New API Services
 * 
 * This file contains practical examples showing how to integrate
 * the new API services into your React Native components.
 */

// ============================================================================
// 1. SETUP AUTHENTICATION (Do this once in App.tsx or LoginScreen)
// ============================================================================

import { setupVendorAuth } from './core/utils/authSetup';

// In your App.tsx or after login
React.useEffect(() => {
    // Replace '1' with your actual vendor ID from backend
    setupVendorAuth('1');
}, []);

// ============================================================================
// 2. USING VENDOR ORDERS IN A COMPONENT
// ============================================================================

import React, { useEffect } from 'react';
import { View, Text, FlatList, Button } from 'react-native';
import { useAppDispatch, useAppSelect } from './store/hooks';
import { fetchOrders, acceptOrder, rejectOrder, markOrderReady } from './store/slices/ordersSlice';
import { canAcceptOrder, getOrderStateDescription } from './core/api';

function OrdersListScreen() {
    const dispatch = useAppDispatch();
    const { orders, isLoading, error } = useAppSelector(state => state.orders);

    useEffect(() => {
        // Fetch pending orders when component mounts
        dispatch(fetchOrders());
    }, [dispatch]);

    const handleAcceptOrder = (orderId: string) => {
        dispatch(acceptOrder({
            orderId,
            estimatedPrepTime: 15 // minutes
        }));
    };

    const handleRejectOrder = (orderId: string) => {
        dispatch(rejectOrder({
            orderId,
            reason: 'Out of ingredients'
        }));
    };

    const handleMarkReady = (orderId: string) => {
        dispatch(markOrderReady(orderId));
    };

    if (isLoading) return <Text>Loading...</Text>;
    if (error) return <Text>Error: {error}</Text>;

    return (
        <FlatList
            data={orders}
            keyExtractor={item => item.orderId}
            renderItem={({ item }) => (
                <View>
                    <Text>Order ID: {item.orderId}</Text>
                    <Text>Status: {getOrderStateDescription(item.state)}</Text>
                    <Text>Total: ₹{item.totalAmount}</Text>

                    {canAcceptOrder(item.state) && (
                        <>
                            <Button
                                title="Accept Order"
                                onPress={() => handleAcceptOrder(item.orderId)}
                            />
                            <Button
                                title="Reject Order"
                                onPress={() => handleRejectOrder(item.orderId)}
                                color="red"
                            />
                        </>
                    )}

                    {item.state === 'PREPARING' && (
                        <Button
                            title="Mark Ready for Pickup"
                            onPress={() => handleMarkReady(item.orderId)}
                        />
                    )}
                </View>
            )}
        />
    );
}

// ============================================================================
// 3. USING MENU ITEMS IN A COMPONENT
// ============================================================================

import { fetchMenuItems, createMenuItem, deleteMenuItem } from './store/slices/menuSlice';

function MenuManagementScreen() {
    const dispatch = useAppDispatch();
    const { items, isLoading, currentBranchId } = useAppSelector(state => state.menu);

    useEffect(() => {
        const branchId = 1; // Use your actual branch ID
        dispatch(fetchMenuItems({ branchId }));
    }, [dispatch]);

    const handleAddItem = () => {
        const newItem = {
            name: 'Butter Chicken',
            description: 'Creamy butter chicken curry',
            price: 250,
            category: 'Main Course',
            isVeg: false,
            isAvailable: true,
        };

        dispatch(createMenuItem({
            branchId: currentBranchId!,
            menuItemData: newItem
        }));
    };

    const handleDeleteItem = (menuItemId: number) => {
        dispatch(deleteMenuItem(menuItemId));
    };

    return (
        <View>
            <Button title="Add New Item" onPress={handleAddItem} />
            <FlatList
                data={items}
                keyExtractor={item => item.menuItemId.toString()}
                renderItem={({ item }) => (
                    <View>
                        <Text>{item.name} - ₹{item.price}</Text>
                        <Text>{item.category}</Text>
                        <Button
                            title="Delete"
                            onPress={() => handleDeleteItem(item.menuItemId)}
                            color="red"
                        />
                    </View>
                )}
            />
        </View>
    );
}

// ============================================================================
// 4. DIRECT API USAGE (Without Redux)
// ============================================================================

import { vendorOrdersApiService, checkoutApiService } from './core/api';

async function directApiExample() {
    try {
        // List pending orders directly
        const ordersResponse = await vendorOrdersApiService.listPendingOrders();
        console.log('Pending orders:', ordersResponse.data);

        // Accept an order
        if (ordersResponse.data.length > 0) {
            const firstOrder = ordersResponse.data[0];
            const acceptedOrder = await vendorOrdersApiService.acceptOrder(
                firstOrder.orderId,
                20 // 20 minutes prep time
            );
            console.log('Accepted order:', acceptedOrder.data);
        }

        // Calculate checkout (for testing customer flow)
        const checkoutRequest = {
            userId: 'customer-uuid',
            vendorBranchId: 1,
            deliveryAddress: {
                addressLine1: '123 Main St',
                city: 'Bangalore',
                pincode: '560001',
            },
            items: [
                { menuItemId: 1, quantity: 2 },
                { menuItemId: 2, quantity: 1 },
            ],
            paymentMethod: 'CARD',
        };

        const checkout = await checkoutApiService.calculateCheckout(checkoutRequest);
        console.log('Checkout total:', checkout.data.pricing.totalAmount);
    } catch (error) {
        console.error('API Error:', error);
    }
}

// ============================================================================
// 5. FSM HELPERS USAGE
// ============================================================================

import {
    canAcceptOrder,
    canMarkOrderReady,
    getNextValidOrderStates,
    isOrderCancellable,
    OrderState
} from './core/api';

function OrderActionsHelper({ order }) {
    // Check what actions are available
    const canAccept = canAcceptOrder(order.state);
    const canMarkReady = canMarkOrderReady(order.state);
    const canCancel = isOrderCancellable(order.state);

    // Get next possible states
    const nextStates = getNextValidOrderStates(order.state);
    console.log('Next possible states:', nextStates);

    return (
        <View>
            {canAccept && <Button title="Accept" />}
            {canMarkReady && <Button title="Mark Ready" />}
            {canCancel && <Button title="Cancel" />}

            <Text>Current State: {order.state}</Text>
            <Text>Next States: {nextStates.join(', ')}</Text>
        </View>
    );
}

export {
    OrdersListScreen,
    MenuManagementScreen,
    directApiExample,
    OrderActionsHelper,
};
