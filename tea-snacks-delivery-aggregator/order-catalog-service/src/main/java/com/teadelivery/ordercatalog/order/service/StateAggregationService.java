package com.teadelivery.ordercatalog.order.service;

import com.teadelivery.ordercatalog.fsm.OrderState;
import com.teadelivery.ordercatalog.fsm.SubOrderState;
import com.teadelivery.ordercatalog.order.model.SubOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * State Aggregation Service
 * Calculates parent order state based on sub-order states
 * As per Multi-Restaurant Design (05_MULTI_RESTAURANT_DESIGN.md)
 */
@Service
@Slf4j
public class StateAggregationService {
    
    /**
     * Calculate parent order state from sub-order states
     * 
     * State Aggregation Logic:
     * - All DELIVERED → DELIVERED
     * - All CANCELLED → CANCELLED
     * - Any CANCELLED → PARTIALLY_CANCELLED (custom handling needed)
     * - All READY_FOR_PICKUP → READY_FOR_PICKUP (trigger delivery)
     * - Any PREPARING → PREPARING
     * - Any ACCEPTED → ACCEPTED
     * - Any PENDING_ACCEPTANCE → PENDING_ACCEPTANCE
     * - Default → CREATED
     */
    public OrderState calculateParentState(List<SubOrder> subOrders) {
        if (subOrders == null || subOrders.isEmpty()) {
            log.warn("No sub-orders provided for state aggregation");
            return OrderState.CREATED;
        }
        
        // Terminal states
        if (allInState(subOrders, SubOrderState.DELIVERED)) {
            return OrderState.DELIVERED;
        }
        
        if (allInState(subOrders, SubOrderState.CANCELLED)) {
            return OrderState.CANCELLED;
        }
        
        // Partial cancellation - return most advanced active state
        if (anyInState(subOrders, SubOrderState.CANCELLED)) {
            log.info("Partial cancellation detected for parent order");
            // Return state based on remaining active sub-orders
            List<SubOrder> activeSubOrders = subOrders.stream()
                .filter(s -> s.getState() != SubOrderState.CANCELLED)
                .toList();
            
            if (activeSubOrders.isEmpty()) {
                return OrderState.CANCELLED;
            }
            
            return calculateParentState(activeSubOrders);
        }
        
        // Ready for pickup - all sub-orders ready
        if (allInState(subOrders, SubOrderState.READY_FOR_PICKUP)) {
            return OrderState.READY_FOR_PICKUP;
        }
        
        // Active states - return most advanced state
        if (anyInState(subOrders, SubOrderState.READY_FOR_PICKUP)) {
            return OrderState.PREPARING; // Some ready, some still preparing
        }
        
        if (anyInState(subOrders, SubOrderState.PREPARING)) {
            return OrderState.PREPARING;
        }
        
        if (anyInState(subOrders, SubOrderState.ACCEPTED)) {
            return OrderState.ACCEPTED;
        }
        
        if (anyInState(subOrders, SubOrderState.PENDING_ACCEPTANCE)) {
            return OrderState.PENDING_ACCEPTANCE;
        }
        
        if (anyInState(subOrders, SubOrderState.REJECTED)) {
            // If some rejected but others active, handle based on active ones
            List<SubOrder> activeSubOrders = subOrders.stream()
                .filter(s -> s.getState() != SubOrderState.REJECTED)
                .toList();
            
            if (activeSubOrders.isEmpty()) {
                return OrderState.REJECTED;
            }
            
            return calculateParentState(activeSubOrders);
        }
        
        return OrderState.CREATED;
    }
    
    /**
     * Check if all sub-orders are in a specific state
     */
    private boolean allInState(List<SubOrder> subOrders, SubOrderState state) {
        return subOrders.stream().allMatch(s -> s.getState() == state);
    }
    
    /**
     * Check if any sub-order is in a specific state
     */
    private boolean anyInState(List<SubOrder> subOrders, SubOrderState state) {
        return subOrders.stream().anyMatch(s -> s.getState() == state);
    }
    
    /**
     * Check if parent order should trigger delivery assignment
     * This happens when all sub-orders are READY_FOR_PICKUP
     */
    public boolean shouldTriggerDelivery(List<SubOrder> subOrders) {
        return allInState(subOrders, SubOrderState.READY_FOR_PICKUP);
    }
    
    /**
     * Check if parent order is partially cancelled
     */
    public boolean isPartiallyCancelled(List<SubOrder> subOrders) {
        long cancelledCount = subOrders.stream()
            .filter(s -> s.getState() == SubOrderState.CANCELLED)
            .count();
        
        return cancelledCount > 0 && cancelledCount < subOrders.size();
    }
    
    /**
     * Get count of active (non-cancelled, non-rejected) sub-orders
     */
    public long getActiveSubOrderCount(List<SubOrder> subOrders) {
        return subOrders.stream()
            .filter(s -> s.getState() != SubOrderState.CANCELLED && 
                        s.getState() != SubOrderState.REJECTED)
            .count();
    }
    
    /**
     * Get estimated total preparation time
     * Returns the maximum estimated prep time among all sub-orders
     */
    public Integer getEstimatedTotalPrepTime(List<SubOrder> subOrders) {
        return subOrders.stream()
            .filter(s -> s.getEstimatedPrepTimeMinutes() != null)
            .map(SubOrder::getEstimatedPrepTimeMinutes)
            .max(Integer::compareTo)
            .orElse(null);
    }
}
