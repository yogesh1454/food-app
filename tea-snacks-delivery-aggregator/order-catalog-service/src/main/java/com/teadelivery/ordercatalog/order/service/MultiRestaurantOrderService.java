package com.teadelivery.ordercatalog.order.service;

import com.teadelivery.ordercatalog.delivery.service.DeliveryBatchingService;
import com.teadelivery.ordercatalog.delivery.service.DeliveryService;
import com.teadelivery.ordercatalog.order.fsm.OrderState;
import com.teadelivery.ordercatalog.order.fsm.OrderType;
import com.teadelivery.ordercatalog.order.fsm.SubOrderState;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.model.SubOrder;
import com.teadelivery.ordercatalog.order.repository.OrderRepository;
import com.teadelivery.ordercatalog.order.repository.SubOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Multi-Restaurant Order Service
 * Handles creation and management of orders from multiple restaurants
 * As per Multi-Restaurant Design (05_MULTI_RESTAURANT_DESIGN.md)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MultiRestaurantOrderService {
    
    private final OrderRepository orderRepository;
    private final SubOrderRepository subOrderRepository;
    private final StateAggregationService stateAggregationService;
    private final DeliveryBatchingService deliveryBatchingService;
    private final DeliveryService deliveryService;
    
    /**
     * Create a multi-restaurant order
     * Creates parent order and sub-orders for each restaurant
     */
    @Transactional
    public Order createMultiRestaurantOrder(
        UUID customerId,
        List<RestaurantItems> restaurantItems,
        Map<String, Object> deliveryAddress,
        Point deliveryLocation
    ) {
        log.info("Creating multi-restaurant order for customer: {}, restaurants: {}", 
                customerId, restaurantItems.size());
        
        // Create parent order
        Order parentOrder = new Order();
        parentOrder.setOrderId(UUID.randomUUID());
        parentOrder.setCustomerId(customerId);
        parentOrder.setOrderType(OrderType.MULTI_RESTAURANT);
        parentOrder.setState(OrderState.CREATED);
        parentOrder.setDeliveryAddress(deliveryAddress);
        
        // Calculate totals
        BigDecimal itemTotal = BigDecimal.ZERO;
        
        // Create sub-orders
        for (RestaurantItems items : restaurantItems) {
            SubOrder subOrder = createSubOrder(parentOrder, items);
            subOrderRepository.save(subOrder);
            itemTotal = itemTotal.add(subOrder.getItemTotal());
        }
        
        // Calculate delivery charges
        BigDecimal deliveryCharges = calculateDeliveryCharges(restaurantItems.size());
        
        // Calculate platform fee and GST
        BigDecimal platformFee = new BigDecimal("5.00");
        BigDecimal gst = itemTotal.add(deliveryCharges)
            .multiply(new BigDecimal("0.05"));
        
        // Set totals
        parentOrder.setTotalAmount(
            itemTotal.add(deliveryCharges).add(platformFee).add(gst)
        );
        
        // Save parent order
        orderRepository.save(parentOrder);
        
        log.info("Created multi-restaurant order: {}, total: {}", 
                parentOrder.getOrderId(), parentOrder.getTotalAmount());
        
        return parentOrder;
    }
    
    /**
     * Create a sub-order for a restaurant
     */
    private SubOrder createSubOrder(Order parentOrder, RestaurantItems items) {
        SubOrder subOrder = new SubOrder();
        subOrder.setSubOrderId(UUID.randomUUID());
        subOrder.setParentOrderId(parentOrder.getOrderId());
        subOrder.setRestaurantId(items.getRestaurantId());
        subOrder.setState(SubOrderState.PENDING_ACCEPTANCE);
        subOrder.setItems(items.getItems());
        subOrder.setItemTotal(items.getItemTotal());
        subOrder.setSpecialInstructions(items.getSpecialInstructions());
        subOrder.setEstimatedPrepTimeMinutes(items.getEstimatedPrepTime());
        
        return subOrder;
    }
    
    /**
     * Calculate delivery charges based on number of restaurants
     * Base: ₹20, Additional restaurant: ₹10 each
     */
    private BigDecimal calculateDeliveryCharges(int restaurantCount) {
        BigDecimal baseFee = new BigDecimal("20.00");
        BigDecimal additionalFee = new BigDecimal("10.00")
            .multiply(new BigDecimal(restaurantCount - 1));
        
        return baseFee.add(additionalFee);
    }
    
    /**
     * Update parent order state based on sub-order states
     * Called whenever a sub-order state changes
     */
    @Transactional
    public void updateParentOrderState(UUID parentOrderId) {
        log.info("Updating parent order state: {}", parentOrderId);
        
        Order parentOrder = orderRepository.findById(parentOrderId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Parent order not found: " + parentOrderId));
        
        List<SubOrder> subOrders = subOrderRepository.findByParentOrderId(parentOrderId);
        
        if (subOrders.isEmpty()) {
            log.warn("No sub-orders found for parent order: {}", parentOrderId);
            return;
        }
        
        // Calculate aggregated state
        OrderState newState = stateAggregationService.calculateParentState(subOrders);
        OrderState oldState = parentOrder.getState();
        
        if (newState != oldState) {
            parentOrder.setState(newState);
            orderRepository.save(parentOrder);
            
            log.info("Parent order state updated: {} -> {}", oldState, newState);
            
            // Trigger delivery if all sub-orders ready
            if (stateAggregationService.shouldTriggerDelivery(subOrders)) {
                triggerDeliveryAssignment(parentOrder, subOrders);
            }
        }
    }
    
    /**
     * Trigger delivery assignment for multi-restaurant order
     * Creates batched deliveries based on proximity and timing
     */
    private void triggerDeliveryAssignment(Order parentOrder, List<SubOrder> subOrders) {
        log.info("Triggering delivery assignment for parent order: {}", 
                parentOrder.getOrderId());
        
        // Get customer location
        Point customerLocation = getCustomerLocation(parentOrder);
        
        // Create delivery batches
        List<DeliveryBatchingService.DeliveryBatch> batches = 
            deliveryBatchingService.createBatches(subOrders, customerLocation);
        
        // Create delivery for each batch
        for (DeliveryBatchingService.DeliveryBatch batch : batches) {
            createBatchedDelivery(parentOrder, batch, customerLocation);
        }
        
        log.info("Created {} deliveries for parent order: {}", 
                batches.size(), parentOrder.getOrderId());
    }
    
    /**
     * Create a batched delivery
     */
    private void createBatchedDelivery(
        Order parentOrder,
        DeliveryBatchingService.DeliveryBatch batch,
        Point customerLocation
    ) {
        // For now, create a simple delivery
        // TODO: Enhance to support batched deliveries with multiple pickups
        
        String pickupLocation = "Multiple Restaurants"; // Placeholder
        Map<String, Object> deliveryAddress = parentOrder.getDeliveryAddress();
        String deliveryLocation = deliveryAddress != null ? deliveryAddress.toString() : "Unknown";
        BigDecimal deliveryFee = new BigDecimal("50.00"); // Placeholder
        
        deliveryService.createDelivery(
            parentOrder.getOrderId(),
            pickupLocation,
            deliveryLocation,
            deliveryFee
        );
        
        log.info("Created batched delivery for {} sub-orders", batch.size());
        
        // Start rider search by order ID
        deliveryService.startRiderSearchByOrderId(parentOrder.getOrderId());
    }
    
    /**
     * Get customer location from order
     * TODO: Replace with actual location lookup
     */
    private Point getCustomerLocation(Order parentOrder) {
        // Placeholder - should fetch from customer service or order metadata
        org.locationtech.jts.geom.GeometryFactory geometryFactory = 
            new org.locationtech.jts.geom.GeometryFactory();
        return geometryFactory.createPoint(
            new org.locationtech.jts.geom.Coordinate(77.5946, 12.9716)
        );
    }
    
    /**
     * Cancel a sub-order
     * Updates parent order state accordingly
     */
    @Transactional
    public void cancelSubOrder(UUID subOrderId, String reason) {
        log.info("Cancelling sub-order: {}, reason: {}", subOrderId, reason);
        
        SubOrder subOrder = subOrderRepository.findById(subOrderId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Sub-order not found: " + subOrderId));
        
        if (!subOrder.isCancellable()) {
            throw new IllegalStateException(
                "Sub-order cannot be cancelled in state: " + subOrder.getState());
        }
        
        subOrder.setState(SubOrderState.CANCELLED);
        subOrder.updateStateTimestamp(SubOrderState.CANCELLED);
        subOrderRepository.save(subOrder);
        
        // Update parent order state
        updateParentOrderState(subOrder.getParentOrderId());
        
        log.info("Sub-order cancelled: {}", subOrderId);
    }
    
    /**
     * Get all sub-orders for a parent order
     */
    public List<SubOrder> getSubOrders(UUID parentOrderId) {
        return subOrderRepository.findByParentOrderId(parentOrderId);
    }
    
    /**
     * Check if order is multi-restaurant
     */
    public boolean isMultiRestaurantOrder(UUID orderId) {
        return orderRepository.findById(orderId)
            .map(order -> order.getOrderType() == OrderType.MULTI_RESTAURANT)
            .orElse(false);
    }
    
    /**
     * Restaurant Items DTO
     * Represents items from a single restaurant
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RestaurantItems {
        private UUID restaurantId;
        private Map<String, Object> items;
        private BigDecimal itemTotal;
        private String specialInstructions;
        private Integer estimatedPrepTime;
    }
}
