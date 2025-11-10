package com.teadelivery.ordercatalog.order.controller;

import com.teadelivery.ordercatalog.order.fsm.OrderState;
import com.teadelivery.ordercatalog.order.dto.CancelOrderRequest;
import com.teadelivery.ordercatalog.order.dto.CreateOrderRequest;
import com.teadelivery.ordercatalog.order.dto.OrderResponse;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.model.OrderItem;
import com.teadelivery.ordercatalog.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Order Controller
 * REST API endpoints for customer order management
 */
@RestController
@RequestMapping("/api/v1/orders")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Customer order management APIs")
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * Create a new order
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create order", description = "Create a new order with items")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or validation failed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<OrderResponse> createOrder(
        @RequestBody @Valid CreateOrderRequest request,
        @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader
    ) {
        // For now, use header or generate UUID (in production, extract from JWT token)
        UUID customerId = customerIdHeader != null ? 
            UUID.fromString(customerIdHeader) : UUID.randomUUID();
        
        log.info("Creating order for customer: {}", customerId);
        
        // Convert DTOs to entities
        List<OrderItem> items = request.getItems().stream()
            .map(itemReq -> {
                OrderItem item = new OrderItem();
                item.setMenuItemId(itemReq.getMenuItemId());
                item.setQuantity(itemReq.getQuantity());
                item.setPriceAtOrder(itemReq.getUnitPrice());
                item.setCustomizations(itemReq.getCustomizations());
                item.setNotes(itemReq.getSpecialInstructions());
                // itemName will be set by the service layer after fetching menu item details
                return item;
            })
            .collect(Collectors.toList());
        
        Order order = orderService.createOrder(
            customerId,
            items,
            request.getDeliveryAddress(),
            request.getSpecialInstructions()
        );
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(OrderResponse.from(order));
    }
    
    /**
     * Get order details
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order", description = "Get order details by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> getOrder(
        @PathVariable UUID orderId,
        @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader
    ) {
        log.info("Getting order: orderId={}", orderId);
        
        Order order = orderService.getOrderById(orderId);
        
        // In production, verify customer owns this order
        if (customerIdHeader != null) {
            UUID customerId = UUID.fromString(customerIdHeader);
            if (!order.getCustomerId().equals(customerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        return ResponseEntity.ok(OrderResponse.from(order));
    }
    
    /**
     * List customer orders
     */
    @GetMapping
    @Operation(summary = "List orders", description = "List orders for the authenticated customer")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    })
    public ResponseEntity<List<OrderResponse>> listOrders(
        @RequestParam(required = false) OrderState state,
        @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader
    ) {
        UUID customerId = customerIdHeader != null ? 
            UUID.fromString(customerIdHeader) : null;
        
        log.info("Listing orders for customer: {}, state: {}", customerId, state);
        
        List<Order> orders;
        if (customerId != null && state != null) {
            orders = orderService.getCustomerOrdersByState(customerId, state);
        } else if (customerId != null) {
            orders = orderService.getOrdersByCustomer(customerId);
        } else if (state != null) {
            orders = orderService.getOrdersByState(state);
        } else {
            // In production, this should be restricted
            orders = List.of();
        }
        
        List<OrderResponse> response = orders.stream()
            .map(OrderResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel order
     */
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order if it's in a cancellable state")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Order cannot be cancelled"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> cancelOrder(
        @PathVariable UUID orderId,
        @RequestBody @Valid CancelOrderRequest request,
        @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader
    ) {
        UUID customerId = customerIdHeader != null ? 
            UUID.fromString(customerIdHeader) : null;
        
        log.info("Cancelling order: orderId={}, customerId={}", orderId, customerId);
        
        request.setCancelledBy("CUSTOMER");
        
        Order order = orderService.cancelOrder(
            orderId,
            customerId,
            request.getCancelledBy(),
            request.getReason()
        );
        
        return ResponseEntity.ok(OrderResponse.from(order));
    }
}
