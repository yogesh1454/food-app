package com.teadelivery.ordercatalog.order.status.service;

import com.teadelivery.ordercatalog.delivery.model.Delivery;
import com.teadelivery.ordercatalog.delivery.repository.DeliveryRepository;
import com.teadelivery.ordercatalog.delivery.fsm.DeliveryState;
import com.teadelivery.ordercatalog.order.fsm.OrderState;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.status.model.CustomerStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Status Mapper Service
 * Maps internal FSM states (Order + Delivery) to customer-facing status
 * As per BE-004-27
 */
@Service
@Slf4j
public class StatusMapperService {
    
    private final DeliveryRepository deliveryRepository;
    
    public StatusMapperService(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }
    
    /**
     * Map Order and Delivery states to Customer Status
     */
    public CustomerStatus mapToCustomerStatus(Order order) {
        OrderState orderState = order.getState();
        
        // Get delivery if exists
        Optional<Delivery> deliveryOpt = deliveryRepository.findByOrderId(order.getOrderId());
        DeliveryState deliveryState = deliveryOpt.map(Delivery::getState).orElse(null);
        
        return mapStates(orderState, deliveryState);
    }
    
    /**
     * Map Order and Delivery states to Customer Status
     */
    public CustomerStatus mapToCustomerStatus(UUID orderId, OrderState orderState, DeliveryState deliveryState) {
        return mapStates(orderState, deliveryState);
    }
    
    /**
     * Core mapping logic
     */
    private CustomerStatus mapStates(OrderState orderState, DeliveryState deliveryState) {
        // Handle cancelled orders
        if (orderState == OrderState.CANCELLED) {
            return CustomerStatus.CANCELLED;
        }
        
        // Handle delivered orders
        if (orderState == OrderState.DELIVERED && 
            (deliveryState == null || deliveryState == DeliveryState.DELIVERED)) {
            return CustomerStatus.DELIVERED;
        }
        
        // ORDER_PLACED: Initial states before restaurant acceptance
        if (orderState == OrderState.CREATED ||
            orderState == OrderState.VALIDATED ||
            orderState == OrderState.PAYMENT_CONFIRMED ||
            orderState == OrderState.PENDING_ACCEPTANCE) {
            return CustomerStatus.ORDER_PLACED;
        }
        
        // ORDER_CONFIRMED: Restaurant accepted
        if (orderState == OrderState.ACCEPTED) {
            return CustomerStatus.ORDER_CONFIRMED;
        }
        
        // PREPARING: Food being prepared
        if (orderState == OrderState.PREPARING) {
            // If delivery is being searched or assigned, still show PREPARING
            if (deliveryState == null ||
                deliveryState == DeliveryState.PENDING ||
                deliveryState == DeliveryState.SEARCHING_RIDER ||
                deliveryState == DeliveryState.RIDER_ASSIGNED) {
                return CustomerStatus.PREPARING;
            }
            
            // If rider accepted, show RIDER_ASSIGNED
            if (deliveryState == DeliveryState.RIDER_ACCEPTED ||
                deliveryState == DeliveryState.AT_RESTAURANT) {
                return CustomerStatus.RIDER_ASSIGNED;
            }
        }
        
        // READY_FOR_PICKUP: Food ready, waiting for rider
        if (orderState == OrderState.READY_FOR_PICKUP) {
            if (deliveryState == null ||
                deliveryState == DeliveryState.PENDING ||
                deliveryState == DeliveryState.SEARCHING_RIDER ||
                deliveryState == DeliveryState.RIDER_ASSIGNED) {
                return CustomerStatus.READY_FOR_PICKUP;
            }
            
            if (deliveryState == DeliveryState.RIDER_ACCEPTED ||
                deliveryState == DeliveryState.AT_RESTAURANT) {
                return CustomerStatus.RIDER_ASSIGNED;
            }
        }
        
        // RIDER_ASSIGNED: Rider heading to restaurant or at restaurant
        if (orderState == OrderState.ASSIGNED_TO_RIDER) {
            if (deliveryState == DeliveryState.RIDER_ACCEPTED ||
                deliveryState == DeliveryState.AT_RESTAURANT) {
                return CustomerStatus.RIDER_ASSIGNED;
            }
        }
        
        // OUT_FOR_DELIVERY: Rider picked up and delivering
        if (orderState == OrderState.PICKED_UP ||
            (deliveryState != null && 
             (deliveryState == DeliveryState.PICKED_UP ||
              deliveryState == DeliveryState.OUT_FOR_DELIVERY))) {
            return CustomerStatus.OUT_FOR_DELIVERY;
        }
        
        // DELIVERED: Order delivered
        if (orderState == OrderState.DELIVERED) {
            return CustomerStatus.DELIVERED;
        }
        
        // Failed delivery
        if (deliveryState == DeliveryState.FAILED) {
            return CustomerStatus.CANCELLED;
        }
        
        // Default fallback
        log.warn("Unmapped state combination: orderState={}, deliveryState={}", 
                 orderState, deliveryState);
        return CustomerStatus.ORDER_PLACED;
    }
}
