package com.teadelivery.ordercatalog.order.controller;

import com.teadelivery.ordercatalog.order.fsm.OrderState;
import com.teadelivery.ordercatalog.order.dto.AcceptOrderRequest;
import com.teadelivery.ordercatalog.order.dto.OrderResponse;
import com.teadelivery.ordercatalog.order.dto.RejectOrderRequest;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Restaurant Order Controller
 * REST API endpoints for restaurant order management
 */
@RestController
@RequestMapping("/api/v1/restaurant/orders")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Restaurant Orders", description = "Restaurant order management APIs")
public class RestaurantOrderController {
    
    private final OrderService orderService;
    
    /**
     * List pending orders for restaurant
     */
    @GetMapping
    @Operation(summary = "List pending orders", description = "List orders pending acceptance for the restaurant")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    })
    public ResponseEntity<List<OrderResponse>> listPendingOrders(
        @RequestHeader(value = "X-Restaurant-Id", required = false) String restaurantIdHeader
    ) {
        log.info("Listing pending orders for restaurant: {}", restaurantIdHeader);
        
        // Get orders in PENDING_ACCEPTANCE state
        List<Order> orders = orderService.getOrdersByState(OrderState.PENDING_ACCEPTANCE);
        
        // In production, filter by restaurant ID
        // Note: Restaurant filtering would require additional logic to map menu items to restaurants
        if (restaurantIdHeader != null) {
            // TODO: Implement restaurant filtering based on menu item ownership
        }
        
        List<OrderResponse> response = orders.stream()
            .map(OrderResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Accept order
     */
    @PostMapping("/{orderId}/accept")
    @Operation(summary = "Accept order", description = "Accept an order and start preparation")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order accepted successfully"),
        @ApiResponse(responseCode = "400", description = "Order cannot be accepted"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> acceptOrder(
        @PathVariable UUID orderId,
        @RequestBody @Valid AcceptOrderRequest request,
        @RequestHeader(value = "X-Restaurant-Id", required = false) String restaurantIdHeader
    ) {
        UUID restaurantId = restaurantIdHeader != null ? 
            UUID.fromString(restaurantIdHeader) : null;
        
        log.info("Accepting order: orderId={}, restaurantId={}, prepTime={}", 
            orderId, restaurantId, request.getEstimatedPrepTime());
        
        Order order = orderService.acceptOrder(orderId, restaurantId);
        
        // In production, store estimated prep time
        // order.setEstimatedPrepTime(request.getEstimatedPrepTime());
        
        return ResponseEntity.ok(OrderResponse.from(order));
    }
    
    /**
     * Reject order
     */
    @PostMapping("/{orderId}/reject")
    @Operation(summary = "Reject order", description = "Reject an order with reason")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order rejected successfully"),
        @ApiResponse(responseCode = "400", description = "Order cannot be rejected"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> rejectOrder(
        @PathVariable UUID orderId,
        @RequestBody @Valid RejectOrderRequest request,
        @RequestHeader(value = "X-Restaurant-Id", required = false) String restaurantIdHeader
    ) {
        UUID restaurantId = restaurantIdHeader != null ? 
            UUID.fromString(restaurantIdHeader) : null;
        
        log.info("Rejecting order: orderId={}, restaurantId={}, reason={}", 
            orderId, restaurantId, request.getReason());
        
        Order order = orderService.rejectOrder(orderId, restaurantId, request.getReason());
        
        return ResponseEntity.ok(OrderResponse.from(order));
    }
    
    /**
     * Mark order ready
     */
    @PostMapping("/{orderId}/ready")
    @Operation(summary = "Mark order ready", description = "Mark order as ready for pickup")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order marked ready successfully"),
        @ApiResponse(responseCode = "400", description = "Order cannot be marked ready"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> markOrderReady(
        @PathVariable UUID orderId,
        @RequestHeader(value = "X-Restaurant-Id", required = false) String restaurantIdHeader
    ) {
        UUID restaurantId = restaurantIdHeader != null ? 
            UUID.fromString(restaurantIdHeader) : null;
        
        log.info("Marking order ready: orderId={}, restaurantId={}", orderId, restaurantId);
        
        Order order = orderService.markReady(orderId, restaurantId);
        
        return ResponseEntity.ok(OrderResponse.from(order));
    }
}
