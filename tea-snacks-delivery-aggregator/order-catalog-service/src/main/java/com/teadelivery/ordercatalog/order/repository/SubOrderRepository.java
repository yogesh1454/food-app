package com.teadelivery.ordercatalog.order.repository;

import com.teadelivery.ordercatalog.order.fsm.SubOrderState;
import com.teadelivery.ordercatalog.order.model.SubOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubOrderRepository extends JpaRepository<SubOrder, UUID> {
    
    /**
     * Find all sub-orders for a parent order
     */
    List<SubOrder> findByParentOrderId(UUID parentOrderId);
    
    /**
     * Find all sub-orders for a restaurant
     */
    List<SubOrder> findByRestaurantId(UUID restaurantId);
    
    /**
     * Find sub-orders by state
     */
    List<SubOrder> findByState(SubOrderState state);
    
    /**
     * Find sub-orders for a parent order in a specific state
     */
    List<SubOrder> findByParentOrderIdAndState(UUID parentOrderId, SubOrderState state);
    
    /**
     * Find sub-orders for a restaurant in a specific state
     */
    List<SubOrder> findByRestaurantIdAndState(UUID restaurantId, SubOrderState state);
    
    /**
     * Count sub-orders for a parent order
     */
    long countByParentOrderId(UUID parentOrderId);
    
    /**
     * Count sub-orders for a restaurant in a specific state
     */
    long countByRestaurantIdAndState(UUID restaurantId, SubOrderState state);
    
    /**
     * Check if all sub-orders for a parent order are in a specific state
     */
    default boolean allSubOrdersInState(UUID parentOrderId, SubOrderState state) {
        List<SubOrder> subOrders = findByParentOrderId(parentOrderId);
        return !subOrders.isEmpty() && 
               subOrders.stream().allMatch(s -> s.getState() == state);
    }
    
    /**
     * Check if any sub-order for a parent order is in a specific state
     */
    default boolean anySubOrderInState(UUID parentOrderId, SubOrderState state) {
        List<SubOrder> subOrders = findByParentOrderId(parentOrderId);
        return subOrders.stream().anyMatch(s -> s.getState() == state);
    }
}
