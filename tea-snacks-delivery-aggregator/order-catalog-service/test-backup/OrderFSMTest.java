package com.teadelivery.ordercatalog.order.fsm;

import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for Order FSM
 * Tests all state transitions and triggers
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Order FSM Unit Tests")
class OrderFSMTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    private OrderFSM orderFSM;
    private Order testOrder;
    private UUID testOrderId;
    
    @BeforeEach
    void setUp() {
        orderFSM = new OrderFSM(orderRepository);
        testOrderId = UUID.randomUUID();
        testOrder = new Order();
        testOrder.setOrderId(testOrderId);
        testOrder.setState(OrderState.CREATED);
        
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
    }
    
    @Test
    @DisplayName("Should transition from CREATED to VALIDATED on VALIDATE trigger")
    void shouldTransitionFromCreatedToValidated() {
        // When
        orderFSM.fire(testOrderId, OrderTrigger.VALIDATE);
        
        // Then
        assertThat(testOrder.getState()).isEqualTo(OrderState.VALIDATED);
        verify(orderRepository).save(testOrder);
    }
    
    @Test
    @DisplayName("Should transition from VALIDATED to PAYMENT_CONFIRMED on CONFIRM_PAYMENT trigger")
    void shouldTransitionFromValidatedToPaymentConfirmed() {
        // Given
        testOrder.setState(OrderState.VALIDATED);
        
        // When
        orderFSM.fire(testOrderId, OrderTrigger.CONFIRM_PAYMENT);
        
        // Then
        assertThat(testOrder.getState()).isEqualTo(OrderState.PAYMENT_CONFIRMED);
        verify(orderRepository).save(testOrder);
    }
    
    @Test
    @DisplayName("Should transition from PAYMENT_CONFIRMED to PENDING_ACCEPTANCE on SUBMIT_TO_RESTAURANT trigger")
    void shouldTransitionFromPaymentConfirmedToPendingAcceptance() {
        // Given
        testOrder.setState(OrderState.PAYMENT_CONFIRMED);
        
        // When
        orderFSM.fire(testOrderId, OrderTrigger.SUBMIT_TO_RESTAURANT);
        
        // Then
        assertThat(testOrder.getState()).isEqualTo(OrderState.PENDING_ACCEPTANCE);
        verify(orderRepository).save(testOrder);
    }
    
    @Test
    @DisplayName("Should transition from PENDING_ACCEPTANCE to ACCEPTED on ACCEPT trigger")
    void shouldTransitionFromPendingAcceptanceToAccepted() {
        // Given
        testOrder.setState(OrderState.PENDING_ACCEPTANCE);
        
        // When
        orderFSM.fire(testOrderId, OrderTrigger.ACCEPT);
        
        // Then
        assertThat(testOrder.getState()).isEqualTo(OrderState.ACCEPTED);
        verify(orderRepository).save(testOrder);
    }
    
    @Test
    @DisplayName("Should transition from PENDING_ACCEPTANCE to REJECTED on REJECT trigger")
    void shouldTransitionFromPendingAcceptanceToRejected() {
        // Given
        testOrder.setState(OrderState.PENDING_ACCEPTANCE);
        
        // When
        orderFSM.fire(testOrderId, OrderTrigger.REJECT);
        
        // Then
        assertThat(testOrder.getState()).isEqualTo(OrderState.REJECTED);
        verify(orderRepository).save(testOrder);
    }
    
    @Test
    @DisplayName("Should transition from ACCEPTED to PREPARING on START_PREPARATION trigger")
    void shouldTransitionFromAcceptedToPreparing() {
        // Given
        testOrder.setState(OrderState.ACCEPTED);
        
        // When
        orderFSM.fire(testOrderId, OrderTrigger.START_PREPARATION);
        
        // Then
        assertThat(testOrder.getState()).isEqualTo(OrderState.PREPARING);
        verify(orderRepository).save(testOrder);
    }
    
    @Test
    @DisplayName("Should transition from PREPARING to READY_FOR_PICKUP on MARK_READY trigger")
    void shouldTransitionFromPreparingToReadyForPickup() {
        // Given
        testOrder.setState(OrderState.PREPARING);
        
        // When
        orderFSM.fire(testOrderId, OrderTrigger.MARK_READY);
        
        // Then
        assertThat(testOrder.getState()).isEqualTo(OrderState.READY_FOR_PICKUP);
        verify(orderRepository).save(testOrder);
    }
    
    @Test
    @DisplayName("Should transition from READY_FOR_PICKUP to ASSIGNED_TO_RIDER on ASSIGN_RIDER trigger")
    void shouldTransitionFromReadyForPickupToAssignedToRider() {
        // Given
        testOrder.setState(OrderState.READY_FOR_PICKUP);
        
        // When
        orderFSM.fire(testOrderId, OrderTrigger.ASSIGN_RIDER);
        
        // Then
        assertThat(testOrder.getState()).isEqualTo(OrderState.ASSIGNED_TO_RIDER);
        verify(orderRepository).save(testOrder);
    }
    
    @Test
    @DisplayName("Should transition from ASSIGNED_TO_RIDER to PICKED_UP on RIDER_PICKUP trigger")
    void shouldTransitionFromAssignedToRiderToPickedUp() {
        // Given
        testOrder.setState(OrderState.ASSIGNED_TO_RIDER);
        
        // When
        orderFSM.fire(testOrderId, OrderTrigger.RIDER_PICKUP);
        
        // Then
        assertThat(testOrder.getState()).isEqualTo(OrderState.PICKED_UP);
        verify(orderRepository).save(testOrder);
    }
    
    @Test
    @DisplayName("Should transition from PICKED_UP to DELIVERED on DELIVER_ORDER trigger")
    void shouldTransitionFromPickedUpToDelivered() {
        // Given
        testOrder.setState(OrderState.PICKED_UP);
        
        // When
        orderFSM.fire(testOrderId, OrderTrigger.DELIVER_ORDER);
        
        // Then
        assertThat(testOrder.getState()).isEqualTo(OrderState.DELIVERED);
        verify(orderRepository).save(testOrder);
    }
    
    @Test
    @DisplayName("Should transition from DELIVERED to CLOSED on CLOSE trigger")
    void shouldTransitionFromDeliveredToClosed() {
        // Given
        testOrder.setState(OrderState.DELIVERED);
        
        // When
        orderFSM.fire(testOrderId, OrderTrigger.CLOSE);
        
        // Then
        assertThat(testOrder.getState()).isEqualTo(OrderState.CLOSED);
        verify(orderRepository).save(testOrder);
    }
    
    @Test
    @DisplayName("Should transition from VALIDATED to CANCELLED on CANCEL trigger")
    void shouldTransitionFromValidatedToCancelled() {
        // Given
        testOrder.setState(OrderState.VALIDATED);
        
        // When
        orderFSM.fire(testOrderId, OrderTrigger.CANCEL);
        
        // Then
        assertThat(testOrder.getState()).isEqualTo(OrderState.CANCELLED);
        verify(orderRepository).save(testOrder);
    }
    
    @Test
    @DisplayName("Should transition from ACCEPTED to CANCELLED on CANCEL trigger")
    void shouldTransitionFromAcceptedToCancelled() {
        // Given
        testOrder.setState(OrderState.ACCEPTED);
        
        // When
        orderFSM.fire(testOrderId, OrderTrigger.CANCEL);
        
        // Then
        assertThat(testOrder.getState()).isEqualTo(OrderState.CANCELLED);
        verify(orderRepository).save(testOrder);
    }
    
    @Test
    @DisplayName("Should throw exception for invalid state transition")
    void shouldThrowExceptionForInvalidTransition() {
        // Given
        testOrder.setState(OrderState.DELIVERED);
        
        // When/Then
        assertThatThrownBy(() -> orderFSM.fire(testOrderId, OrderTrigger.VALIDATE))
            .isInstanceOf(IllegalStateException.class);
    }
    
    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        // Given
        UUID nonExistentOrderId = UUID.randomUUID();
        when(orderRepository.findById(nonExistentOrderId)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> orderFSM.fire(nonExistentOrderId, OrderTrigger.VALIDATE))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Order not found");
    }
    
    @Test
    @DisplayName("Should get current state")
    void shouldGetCurrentState() {
        // Given
        testOrder.setState(OrderState.PREPARING);
        
        // When
        OrderState currentState = orderFSM.getState(testOrderId);
        
        // Then
        assertThat(currentState).isEqualTo(OrderState.PREPARING);
    }
    
    @Test
    @DisplayName("Should check if transition is permitted")
    void shouldCheckIfTransitionIsPermitted() {
        // Given
        testOrder.setState(OrderState.CREATED);
        
        // When
        boolean canValidate = orderFSM.canFire(testOrderId, OrderTrigger.VALIDATE);
        boolean canDeliver = orderFSM.canFire(testOrderId, OrderTrigger.DELIVER_ORDER);
        
        // Then
        assertThat(canValidate).isTrue();
        assertThat(canDeliver).isFalse();
    }
    
    @Test
    @DisplayName("Should handle complete order lifecycle")
    void shouldHandleCompleteOrderLifecycle() {
        // Given - Order starts in CREATED state
        assertThat(testOrder.getState()).isEqualTo(OrderState.CREATED);
        
        // When/Then - Execute full lifecycle
        orderFSM.fire(testOrderId, OrderTrigger.VALIDATE);
        assertThat(testOrder.getState()).isEqualTo(OrderState.VALIDATED);
        
        orderFSM.fire(testOrderId, OrderTrigger.CONFIRM_PAYMENT);
        assertThat(testOrder.getState()).isEqualTo(OrderState.PAYMENT_CONFIRMED);
        
        orderFSM.fire(testOrderId, OrderTrigger.SUBMIT_TO_RESTAURANT);
        assertThat(testOrder.getState()).isEqualTo(OrderState.PENDING_ACCEPTANCE);
        
        orderFSM.fire(testOrderId, OrderTrigger.ACCEPT);
        assertThat(testOrder.getState()).isEqualTo(OrderState.ACCEPTED);
        
        orderFSM.fire(testOrderId, OrderTrigger.START_PREPARATION);
        assertThat(testOrder.getState()).isEqualTo(OrderState.PREPARING);
        
        orderFSM.fire(testOrderId, OrderTrigger.MARK_READY);
        assertThat(testOrder.getState()).isEqualTo(OrderState.READY_FOR_PICKUP);
        
        orderFSM.fire(testOrderId, OrderTrigger.ASSIGN_RIDER);
        assertThat(testOrder.getState()).isEqualTo(OrderState.ASSIGNED_TO_RIDER);
        
        orderFSM.fire(testOrderId, OrderTrigger.RIDER_PICKUP);
        assertThat(testOrder.getState()).isEqualTo(OrderState.PICKED_UP);
        
        orderFSM.fire(testOrderId, OrderTrigger.DELIVER_ORDER);
        assertThat(testOrder.getState()).isEqualTo(OrderState.DELIVERED);
        
        orderFSM.fire(testOrderId, OrderTrigger.CLOSE);
        assertThat(testOrder.getState()).isEqualTo(OrderState.CLOSED);
        
        // Verify all transitions were saved
        verify(orderRepository, times(10)).save(testOrder);
    }
}
