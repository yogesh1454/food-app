import { OrderState, DeliveryState } from '../types/api';

/**
 * FSM Helper Functions for Order and Delivery State Machines
 * 
 * Order FSM: 13 states
 * Delivery FSM: 9 states
 */

// ============================================================================
// ORDER FSM HELPERS
// ============================================================================

/**
 * Terminal states - orders that cannot transition to other states
 */
const ORDER_TERMINAL_STATES: OrderState[] = [
    OrderState.DELIVERED,
    OrderState.CLOSED,
    OrderState.CANCELLED,
    OrderState.REJECTED,
];

/**
 * States from which an order can be cancelled by the customer
 */
const ORDER_CANCELLABLE_STATES: OrderState[] = [
    OrderState.CREATED,
    OrderState.VALIDATED,
    OrderState.PAYMENT_CONFIRMED,
    OrderState.PENDING_ACCEPTANCE,
];

/**
 * Check if an order is in a terminal state
 */
export function isOrderTerminal(state: OrderState): boolean {
    return ORDER_TERMINAL_STATES.includes(state);
}

/**
 * Check if an order can be cancelled by the customer
 */
export function isOrderCancellable(state: OrderState): boolean {
    return ORDER_CANCELLABLE_STATES.includes(state);
}

/**
 * Check if an order can be accepted by the vendor
 */
export function canAcceptOrder(state: OrderState): boolean {
    return state === OrderState.PENDING_ACCEPTANCE;
}

/**
 * Check if an order can be rejected by the vendor
 */
export function canRejectOrder(state: OrderState): boolean {
    return state === OrderState.PENDING_ACCEPTANCE;
}

/**
 * Check if an order can be marked as ready for pickup
 */
export function canMarkOrderReady(state: OrderState): boolean {
    return state === OrderState.PREPARING;
}

/**
 * Check if an order can be picked up by rider
 */
export function canPickupOrder(state: OrderState): boolean {
    return state === OrderState.READY_FOR_PICKUP;
}

/**
 * Check if an order can be marked as delivered
 */
export function canDeliverOrder(state: OrderState): boolean {
    return state === OrderState.PICKED_UP;
}

/**
 * Get the next valid states from the current order state
 */
export function getNextValidOrderStates(currentState: OrderState): OrderState[] {
    const transitions: Record<OrderState, OrderState[]> = {
        [OrderState.CREATED]: [OrderState.VALIDATED, OrderState.CANCELLED],
        [OrderState.VALIDATED]: [OrderState.PAYMENT_CONFIRMED, OrderState.CANCELLED],
        [OrderState.PAYMENT_CONFIRMED]: [OrderState.PENDING_ACCEPTANCE, OrderState.CANCELLED],
        [OrderState.PENDING_ACCEPTANCE]: [OrderState.ACCEPTED, OrderState.REJECTED, OrderState.CANCELLED],
        [OrderState.ACCEPTED]: [OrderState.PREPARING, OrderState.CANCELLED],
        [OrderState.PREPARING]: [OrderState.READY_FOR_PICKUP, OrderState.CANCELLED],
        [OrderState.READY_FOR_PICKUP]: [OrderState.ASSIGNED_TO_RIDER, OrderState.PICKED_UP],
        [OrderState.ASSIGNED_TO_RIDER]: [OrderState.PICKED_UP],
        [OrderState.PICKED_UP]: [OrderState.DELIVERED],
        [OrderState.DELIVERED]: [OrderState.CLOSED],
        [OrderState.CLOSED]: [],
        [OrderState.CANCELLED]: [],
        [OrderState.REJECTED]: [],
    };

    return transitions[currentState] || [];
}

/**
 * Check if a state transition is valid
 */
export function isValidOrderTransition(from: OrderState, to: OrderState): boolean {
    const validStates = getNextValidOrderStates(from);
    return validStates.includes(to);
}

/**
 * Get human-readable state description for vendors
 */
export function getOrderStateDescription(state: OrderState): string {
    const descriptions: Record<OrderState, string> = {
        [OrderState.CREATED]: 'Order created',
        [OrderState.VALIDATED]: 'Order validated',
        [OrderState.PAYMENT_CONFIRMED]: 'Payment confirmed',
        [OrderState.PENDING_ACCEPTANCE]: 'Waiting for acceptance',
        [OrderState.ACCEPTED]: 'Accepted - preparing soon',
        [OrderState.PREPARING]: 'Being prepared',
        [OrderState.READY_FOR_PICKUP]: 'Ready for pickup',
        [OrderState.ASSIGNED_TO_RIDER]: 'Rider assigned',
        [OrderState.PICKED_UP]: 'Out for delivery',
        [OrderState.DELIVERED]: 'Delivered',
        [OrderState.CLOSED]: 'Completed',
        [OrderState.CANCELLED]: 'Cancelled',
        [OrderState.REJECTED]: 'Rejected',
    };

    return descriptions[state] || state;
}

// ============================================================================
// DELIVERY FSM HELPERS
// ============================================================================

/**
 * Terminal delivery states
 */
const DELIVERY_TERMINAL_STATES: DeliveryState[] = [
    DeliveryState.DELIVERED,
    DeliveryState.FAILED,
];

/**
 * Active delivery states (rider can update location)
 */
const DELIVERY_ACTIVE_STATES: DeliveryState[] = [
    DeliveryState.RIDER_ACCEPTED,
    DeliveryState.AT_RESTAURANT,
    DeliveryState.PICKED_UP,
    DeliveryState.OUT_FOR_DELIVERY,
];

/**
 * Check if delivery is in a terminal state
 */
export function isDeliveryTerminal(state: DeliveryState): boolean {
    return DELIVERY_TERMINAL_STATES.includes(state);
}

/**
 * Check if delivery is in an active state (ongoing)
 */
export function isDeliveryActive(state: DeliveryState): boolean {
    return DELIVERY_ACTIVE_STATES.includes(state);
}

/**
 * Check if rider can accept the delivery
 */
export function canAcceptDelivery(state: DeliveryState): boolean {
    return state === DeliveryState.RIDER_ASSIGNED;
}

/**
 * Check if rider can reject the delivery
 */
export function canRejectDelivery(state: DeliveryState): boolean {
    return state === DeliveryState.RIDER_ASSIGNED;
}

/**
 * Check if delivery status can be updated to the given status
 */
export function canUpdateDeliveryStatus(
    currentState: DeliveryState,
    newStatus: 'REACHED_RESTAURANT' | 'PICKED_UP' | 'OUT_FOR_DELIVERY' | 'DELIVERED'
): boolean {
    const statusToState: Record<string, DeliveryState> = {
        REACHED_RESTAURANT: DeliveryState.AT_RESTAURANT,
        PICKED_UP: DeliveryState.PICKED_UP,
        OUT_FOR_DELIVERY: DeliveryState.OUT_FOR_DELIVERY,
        DELIVERED: DeliveryState.DELIVERED,
    };

    const targetState = statusToState[newStatus];
    if (!targetState) return false;

    return isValidDeliveryTransition(currentState, targetState);
}

/**
 * Get the next valid states from the current delivery state
 */
export function getNextValidDeliveryStates(currentState: DeliveryState): DeliveryState[] {
    const transitions: Record<DeliveryState, DeliveryState[]> = {
        [DeliveryState.PENDING]: [DeliveryState.SEARCHING_RIDER],
        [DeliveryState.SEARCHING_RIDER]: [DeliveryState.RIDER_ASSIGNED, DeliveryState.FAILED],
        [DeliveryState.RIDER_ASSIGNED]: [DeliveryState.RIDER_ACCEPTED, DeliveryState.SEARCHING_RIDER],
        [DeliveryState.RIDER_ACCEPTED]: [DeliveryState.AT_RESTAURANT, DeliveryState.FAILED],
        [DeliveryState.AT_RESTAURANT]: [DeliveryState.PICKED_UP, DeliveryState.FAILED],
        [DeliveryState.PICKED_UP]: [DeliveryState.OUT_FOR_DELIVERY, DeliveryState.FAILED],
        [DeliveryState.OUT_FOR_DELIVERY]: [DeliveryState.DELIVERED, DeliveryState.FAILED],
        [DeliveryState.DELIVERED]: [],
        [DeliveryState.FAILED]: [],
    };

    return transitions[currentState] || [];
}

/**
 * Check if a delivery state transition is valid
 */
export function isValidDeliveryTransition(from: DeliveryState, to: DeliveryState): boolean {
    const validStates = getNextValidDeliveryStates(from);
    return validStates.includes(to);
}

/**
 * Get human-readable delivery state description
 */
export function getDeliveryStateDescription(state: DeliveryState): string {
    const descriptions: Record<DeliveryState, string> = {
        [DeliveryState.PENDING]: 'Delivery pending',
        [DeliveryState.SEARCHING_RIDER]: 'Searching for rider',
        [DeliveryState.RIDER_ASSIGNED]: 'Rider assigned',
        [DeliveryState.RIDER_ACCEPTED]: 'Rider accepted',
        [DeliveryState.AT_RESTAURANT]: 'Rider at restaurant',
        [DeliveryState.PICKED_UP]: 'Order picked up',
        [DeliveryState.OUT_FOR_DELIVERY]: 'Out for delivery',
        [DeliveryState.DELIVERED]: 'Delivered',
        [DeliveryState.FAILED]: 'Delivery failed',
    };

    return descriptions[state] || state;
}

/**
 * Calculate estimated delivery time remaining (in minutes)
 * Based on current delivery state and typical timings
 */
export function getEstimatedTimeRemaining(state: DeliveryState): number | null {
    const estimates: Partial<Record<DeliveryState, number | null>> = {
        [DeliveryState.PENDING]: 30,
        [DeliveryState.SEARCHING_RIDER]: 25,
        [DeliveryState.RIDER_ASSIGNED]: 20,
        [DeliveryState.RIDER_ACCEPTED]: 18,
        [DeliveryState.AT_RESTAURANT]: 15,
        [DeliveryState.PICKED_UP]: 10,
        [DeliveryState.OUT_FOR_DELIVERY]: 5,
        [DeliveryState.DELIVERED]: 0,
        [DeliveryState.FAILED]: null,
    };

    const estimate = estimates[state];
    return estimate !== undefined ? estimate : null;
}
