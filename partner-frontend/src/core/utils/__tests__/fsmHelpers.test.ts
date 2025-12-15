import {
    // Order FSM helpers
    isOrderTerminal,
    isOrderCancellable,
    canAcceptOrder,
    canRejectOrder,
    canMarkOrderReady,
    canPickupOrder,
    canDeliverOrder,
    getNextValidOrderStates,
    isValidOrderTransition,
    getOrderStateDescription,
    // Delivery FSM helpers
    isDeliveryTerminal,
    isDeliveryActive,
    canAcceptDelivery,
    canRejectDelivery,
    canUpdateDeliveryStatus,
    getNextValidDeliveryStates,
    isValidDeliveryTransition,
    getDeliveryStateDescription,
    getEstimatedTimeRemaining,
} from '../fsmHelpers';
import { OrderState, DeliveryState } from '../../types/api';

describe('Order FSM Helpers', () => {
    describe('isOrderTerminal', () => {
        it('should return true for terminal states', () => {
            expect(isOrderTerminal(OrderState.DELIVERED)).toBe(true);
            expect(isOrderTerminal(OrderState.CLOSED)).toBe(true);
            expect(isOrderTerminal(OrderState.CANCELLED)).toBe(true);
            expect(isOrderTerminal(OrderState.REJECTED)).toBe(true);
        });

        it('should return false for non-terminal states', () => {
            expect(isOrderTerminal(OrderState.CREATED)).toBe(false);
            expect(isOrderTerminal(OrderState.PENDING_ACCEPTANCE)).toBe(false);
            expect(isOrderTerminal(OrderState.PREPARING)).toBe(false);
        });
    });

    describe('isOrderCancellable', () => {
        it('should return true for cancellable states', () => {
            expect(isOrderCancellable(OrderState.CREATED)).toBe(true);
            expect(isOrderCancellable(OrderState.VALIDATED)).toBe(true);
            expect(isOrderCancellable(OrderState.PAYMENT_CONFIRMED)).toBe(true);
            expect(isOrderCancellable(OrderState.PENDING_ACCEPTANCE)).toBe(true);
        });

        it('should return false for non-cancellable states', () => {
            expect(isOrderCancellable(OrderState.ACCEPTED)).toBe(false);
            expect(isOrderCancellable(OrderState.PREPARING)).toBe(false);
            expect(isOrderCancellable(OrderState.DELIVERED)).toBe(false);
        });
    });

    describe('canAcceptOrder', () => {
        it('should return true only for PENDING_ACCEPTANCE', () => {
            expect(canAcceptOrder(OrderState.PENDING_ACCEPTANCE)).toBe(true);
            expect(canAcceptOrder(OrderState.CREATED)).toBe(false);
            expect(canAcceptOrder(OrderState.ACCEPTED)).toBe(false);
        });
    });

    describe('canMarkOrderReady', () => {
        it('should return true only for PREPARING', () => {
            expect(canMarkOrderReady(OrderState.PREPARING)).toBe(true);
            expect(canMarkOrderReady(OrderState.ACCEPTED)).toBe(false);
            expect(canMarkOrderReady(OrderState.READY_FOR_PICKUP)).toBe(false);
        });
    });

    describe('getNextValidOrderStates', () => {
        it('should return correct next states for CREATED', () => {
            const nextStates = getNextValidOrderStates(OrderState.CREATED);
            expect(nextStates).toContain(OrderState.VALIDATED);
            expect(nextStates).toContain(OrderState.CANCELLED);
        });

        it('should return correct next states for PENDING_ACCEPTANCE', () => {
            const nextStates = getNextValidOrderStates(OrderState.PENDING_ACCEPTANCE);
            expect(nextStates).toContain(OrderState.ACCEPTED);
            expect(nextStates).toContain(OrderState.REJECTED);
            expect(nextStates).toContain(OrderState.CANCELLED);
        });

        it('should return empty array for terminal states', () => {
            expect(getNextValidOrderStates(OrderState.DELIVERED)).toEqual([]);
            expect(getNextValidOrderStates(OrderState.CANCELLED)).toEqual([]);
            expect(getNextValidOrderStates(OrderState.CLOSED)).toEqual([]);
        });
    });

    describe('isValidOrderTransition', () => {
        it('should validate correct transitions', () => {
            expect(isValidOrderTransition(OrderState.CREATED, OrderState.VALIDATED)).toBe(true);
            expect(isValidOrderTransition(OrderState.PENDING_ACCEPTANCE, OrderState.ACCEPTED)).toBe(true);
            expect(isValidOrderTransition(OrderState.PREPARING, OrderState.READY_FOR_PICKUP)).toBe(true);
        });

        it('should reject invalid transitions', () => {
            expect(isValidOrderTransition(OrderState.CREATED, OrderState.DELIVERED)).toBe(false);
            expect(isValidOrderTransition(OrderState.DELIVERED, OrderState.PREPARING)).toBe(false);
            expect(isValidOrderTransition(OrderState.CANCELLED, OrderState.ACCEPTED)).toBe(false);
        });
    });

    describe('getOrderStateDescription', () => {
        it('should return human-readable descriptions', () => {
            expect(getOrderStateDescription(OrderState.PENDING_ACCEPTANCE)).toBe('Waiting for acceptance');
            expect(getOrderStateDescription(OrderState.PREPARING)).toBe('Being prepared');
            expect(getOrderStateDescription(OrderState.READY_FOR_PICKUP)).toBe('Ready for pickup');
        });
    });
});

describe('Delivery FSM Helpers', () => {
    describe('isDeliveryTerminal', () => {
        it('should return true for terminal states', () => {
            expect(isDeliveryTerminal(DeliveryState.DELIVERED)).toBe(true);
            expect(isDeliveryTerminal(DeliveryState.FAILED)).toBe(true);
        });

        it('should return false for non-terminal states', () => {
            expect(isDeliveryTerminal(DeliveryState.PENDING)).toBe(false);
            expect(isDeliveryTerminal(DeliveryState.RIDER_ASSIGNED)).toBe(false);
        });
    });

    describe('isDeliveryActive', () => {
        it('should return true for active states', () => {
            expect(isDeliveryActive(DeliveryState.RIDER_ACCEPTED)).toBe(true);
            expect(isDeliveryActive(DeliveryState.AT_RESTAURANT)).toBe(true);
            expect(isDeliveryActive(DeliveryState.PICKED_UP)).toBe(true);
            expect(isDeliveryActive(DeliveryState.OUT_FOR_DELIVERY)).toBe(true);
        });

        it('should return false for inactive states', () => {
            expect(isDeliveryActive(DeliveryState.PENDING)).toBe(false);
            expect(isDeliveryActive(DeliveryState.DELIVERED)).toBe(false);
        });
    });

    describe('canUpdateDeliveryStatus', () => {
        it('should allow valid status updates', () => {
            expect(canUpdateDeliveryStatus(DeliveryState.RIDER_ACCEPTED, 'REACHED_RESTAURANT')).toBe(true);
            expect(canUpdateDeliveryStatus(DeliveryState.AT_RESTAURANT, 'PICKED_UP')).toBe(true);
            expect(canUpdateDeliveryStatus(DeliveryState.PICKED_UP, 'OUT_FOR_DELIVERY')).toBe(true);
        });

        it('should reject invalid status updates', () => {
            expect(canUpdateDeliveryStatus(DeliveryState.PENDING, 'DELIVERED')).toBe(false);
            expect(canUpdateDeliveryStatus(DeliveryState.DELIVERED, 'PICKED_UP')).toBe(false);
        });
    });

    describe('getNextValidDeliveryStates', () => {
        it('should return correct next states', () => {
            const nextStates = getNextValidDeliveryStates(DeliveryState.RIDER_ASSIGNED);
            expect(nextStates).toContain(DeliveryState.RIDER_ACCEPTED);
            expect(nextStates).toContain(DeliveryState.SEARCHING_RIDER);
        });

        it('should return empty array for terminal states', () => {
            expect(getNextValidDeliveryStates(DeliveryState.DELIVERED)).toEqual([]);
            expect(getNextValidDeliveryStates(DeliveryState.FAILED)).toEqual([]);
        });
    });

    describe('getEstimatedTimeRemaining', () => {
        it('should return correct estimates for active states', () => {
            expect(getEstimatedTimeRemaining(DeliveryState.PENDING)).toBe(30);
            expect(getEstimatedTimeRemaining(DeliveryState.PICKED_UP)).toBe(10);
            expect(getEstimatedTimeRemaining(DeliveryState.OUT_FOR_DELIVERY)).toBe(5);
        });

        it('should return 0 for delivered', () => {
            expect(getEstimatedTimeRemaining(DeliveryState.DELIVERED)).toBe(0);
        });

        it('should return null for failed', () => {
            expect(getEstimatedTimeRemaining(DeliveryState.FAILED)).toBeNull();
        });
    });
});
