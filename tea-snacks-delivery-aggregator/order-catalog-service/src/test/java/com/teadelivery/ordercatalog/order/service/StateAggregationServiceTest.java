package com.teadelivery.ordercatalog.order.service;

import com.teadelivery.ordercatalog.fsm.OrderState;
import com.teadelivery.ordercatalog.fsm.SubOrderState;
import com.teadelivery.ordercatalog.order.model.SubOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit Tests for State Aggregation Service
 * Tests parent order state calculation from sub-order states
 */
@DisplayName("State Aggregation Service Unit Tests")
class StateAggregationServiceTest {
    
    private StateAggregationService stateAggregationService;
    private UUID parentOrderId;
    
    @BeforeEach
    void setUp() {
        stateAggregationService = new StateAggregationService();
        parentOrderId = UUID.randomUUID();
    }
    
    @Test
    @DisplayName("Should return DELIVERED when all sub-orders are DELIVERED")
    void shouldReturnDeliveredWhenAllDelivered() {
        // Given
        List<SubOrder> subOrders = createSubOrders(
            SubOrderState.DELIVERED,
            SubOrderState.DELIVERED,
            SubOrderState.DELIVERED
        );
        
        // When
        OrderState parentState = stateAggregationService.calculateParentState(subOrders);
        
        // Then
        assertThat(parentState).isEqualTo(OrderState.DELIVERED);
    }
    
    @Test
    @DisplayName("Should return CANCELLED when all sub-orders are CANCELLED")
    void shouldReturnCancelledWhenAllCancelled() {
        // Given
        List<SubOrder> subOrders = createSubOrders(
            SubOrderState.CANCELLED,
            SubOrderState.CANCELLED
        );
        
        // When
        OrderState parentState = stateAggregationService.calculateParentState(subOrders);
        
        // Then
        assertThat(parentState).isEqualTo(OrderState.CANCELLED);
    }
    
    @Test
    @DisplayName("Should return READY_FOR_PICKUP when all sub-orders are READY_FOR_PICKUP")
    void shouldReturnReadyForPickupWhenAllReady() {
        // Given
        List<SubOrder> subOrders = createSubOrders(
            SubOrderState.READY_FOR_PICKUP,
            SubOrderState.READY_FOR_PICKUP,
            SubOrderState.READY_FOR_PICKUP
        );
        
        // When
        OrderState parentState = stateAggregationService.calculateParentState(subOrders);
        
        // Then
        assertThat(parentState).isEqualTo(OrderState.READY_FOR_PICKUP);
    }
    
    @Test
    @DisplayName("Should return PREPARING when any sub-order is PREPARING")
    void shouldReturnPreparingWhenAnyPreparing() {
        // Given
        List<SubOrder> subOrders = createSubOrders(
            SubOrderState.ACCEPTED,
            SubOrderState.PREPARING,
            SubOrderState.READY_FOR_PICKUP
        );
        
        // When
        OrderState parentState = stateAggregationService.calculateParentState(subOrders);
        
        // Then
        assertThat(parentState).isEqualTo(OrderState.PREPARING);
    }
    
    @Test
    @DisplayName("Should return ACCEPTED when any sub-order is ACCEPTED and none PREPARING")
    void shouldReturnAcceptedWhenAnyAccepted() {
        // Given
        List<SubOrder> subOrders = createSubOrders(
            SubOrderState.ACCEPTED,
            SubOrderState.ACCEPTED,
            SubOrderState.PENDING_ACCEPTANCE
        );
        
        // When
        OrderState parentState = stateAggregationService.calculateParentState(subOrders);
        
        // Then
        assertThat(parentState).isEqualTo(OrderState.ACCEPTED);
    }
    
    @Test
    @DisplayName("Should return PENDING_ACCEPTANCE when any sub-order is PENDING_ACCEPTANCE")
    void shouldReturnPendingAcceptanceWhenAnyPending() {
        // Given
        List<SubOrder> subOrders = createSubOrders(
            SubOrderState.PENDING_ACCEPTANCE,
            SubOrderState.PENDING_ACCEPTANCE
        );
        
        // When
        OrderState parentState = stateAggregationService.calculateParentState(subOrders);
        
        // Then
        assertThat(parentState).isEqualTo(OrderState.PENDING_ACCEPTANCE);
    }
    
    @Test
    @DisplayName("Should handle partial cancellation - return state of active sub-orders")
    void shouldHandlePartialCancellation() {
        // Given
        List<SubOrder> subOrders = createSubOrders(
            SubOrderState.CANCELLED,
            SubOrderState.PREPARING,
            SubOrderState.ACCEPTED
        );
        
        // When
        OrderState parentState = stateAggregationService.calculateParentState(subOrders);
        
        // Then
        assertThat(parentState).isEqualTo(OrderState.PREPARING);
    }
    
    @Test
    @DisplayName("Should handle partial rejection - return state of active sub-orders")
    void shouldHandlePartialRejection() {
        // Given
        List<SubOrder> subOrders = createSubOrders(
            SubOrderState.REJECTED,
            SubOrderState.ACCEPTED,
            SubOrderState.PREPARING
        );
        
        // When
        OrderState parentState = stateAggregationService.calculateParentState(subOrders);
        
        // Then
        assertThat(parentState).isEqualTo(OrderState.PREPARING);
    }
    
    @Test
    @DisplayName("Should return REJECTED when all sub-orders are REJECTED")
    void shouldReturnRejectedWhenAllRejected() {
        // Given
        List<SubOrder> subOrders = createSubOrders(
            SubOrderState.REJECTED,
            SubOrderState.REJECTED
        );
        
        // When
        OrderState parentState = stateAggregationService.calculateParentState(subOrders);
        
        // Then
        assertThat(parentState).isEqualTo(OrderState.REJECTED);
    }
    
    @Test
    @DisplayName("Should return CREATED when sub-orders list is empty")
    void shouldReturnCreatedWhenNoSubOrders() {
        // Given
        List<SubOrder> subOrders = new ArrayList<>();
        
        // When
        OrderState parentState = stateAggregationService.calculateParentState(subOrders);
        
        // Then
        assertThat(parentState).isEqualTo(OrderState.CREATED);
    }
    
    @Test
    @DisplayName("Should return true when should trigger delivery")
    void shouldReturnTrueWhenShouldTriggerDelivery() {
        // Given
        List<SubOrder> subOrders = createSubOrders(
            SubOrderState.READY_FOR_PICKUP,
            SubOrderState.READY_FOR_PICKUP
        );
        
        // When
        boolean shouldTrigger = stateAggregationService.shouldTriggerDelivery(subOrders);
        
        // Then
        assertThat(shouldTrigger).isTrue();
    }
    
    @Test
    @DisplayName("Should return false when should not trigger delivery")
    void shouldReturnFalseWhenShouldNotTriggerDelivery() {
        // Given
        List<SubOrder> subOrders = createSubOrders(
            SubOrderState.READY_FOR_PICKUP,
            SubOrderState.PREPARING
        );
        
        // When
        boolean shouldTrigger = stateAggregationService.shouldTriggerDelivery(subOrders);
        
        // Then
        assertThat(shouldTrigger).isFalse();
    }
    
    @Test
    @DisplayName("Should detect partial cancellation")
    void shouldDetectPartialCancellation() {
        // Given
        List<SubOrder> subOrders = createSubOrders(
            SubOrderState.CANCELLED,
            SubOrderState.PREPARING,
            SubOrderState.ACCEPTED
        );
        
        // When
        boolean isPartial = stateAggregationService.isPartiallyCancelled(subOrders);
        
        // Then
        assertThat(isPartial).isTrue();
    }
    
    @Test
    @DisplayName("Should not detect partial cancellation when all cancelled")
    void shouldNotDetectPartialCancellationWhenAllCancelled() {
        // Given
        List<SubOrder> subOrders = createSubOrders(
            SubOrderState.CANCELLED,
            SubOrderState.CANCELLED
        );
        
        // When
        boolean isPartial = stateAggregationService.isPartiallyCancelled(subOrders);
        
        // Then
        assertThat(isPartial).isFalse();
    }
    
    @Test
    @DisplayName("Should count active sub-orders correctly")
    void shouldCountActiveSubOrders() {
        // Given
        List<SubOrder> subOrders = createSubOrders(
            SubOrderState.CANCELLED,
            SubOrderState.PREPARING,
            SubOrderState.ACCEPTED,
            SubOrderState.REJECTED
        );
        
        // When
        long activeCount = stateAggregationService.getActiveSubOrderCount(subOrders);
        
        // Then
        assertThat(activeCount).isEqualTo(2); // PREPARING and ACCEPTED
    }
    
    @Test
    @DisplayName("Should get estimated total prep time as maximum")
    void shouldGetEstimatedTotalPrepTime() {
        // Given
        List<SubOrder> subOrders = new ArrayList<>();
        subOrders.add(createSubOrder(SubOrderState.ACCEPTED, 15));
        subOrders.add(createSubOrder(SubOrderState.ACCEPTED, 25));
        subOrders.add(createSubOrder(SubOrderState.ACCEPTED, 20));
        
        // When
        Integer totalPrepTime = stateAggregationService.getEstimatedTotalPrepTime(subOrders);
        
        // Then
        assertThat(totalPrepTime).isEqualTo(25); // Maximum prep time
    }
    
    @Test
    @DisplayName("Should return null when no prep times available")
    void shouldReturnNullWhenNoPrepTimes() {
        // Given
        List<SubOrder> subOrders = createSubOrders(
            SubOrderState.ACCEPTED,
            SubOrderState.PREPARING
        );
        
        // When
        Integer totalPrepTime = stateAggregationService.getEstimatedTotalPrepTime(subOrders);
        
        // Then
        assertThat(totalPrepTime).isNull();
    }
    
    // Helper methods
    
    private List<SubOrder> createSubOrders(SubOrderState... states) {
        List<SubOrder> subOrders = new ArrayList<>();
        for (SubOrderState state : states) {
            subOrders.add(createSubOrder(state, null));
        }
        return subOrders;
    }
    
    private SubOrder createSubOrder(SubOrderState state, Integer prepTime) {
        SubOrder subOrder = new SubOrder();
        subOrder.setSubOrderId(UUID.randomUUID());
        subOrder.setParentOrderId(parentOrderId);
        subOrder.setRestaurantId(UUID.randomUUID());
        subOrder.setState(state);
        subOrder.setItemTotal(new BigDecimal("100.00"));
        subOrder.setEstimatedPrepTimeMinutes(prepTime);
        return subOrder;
    }
}
