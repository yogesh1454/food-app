package com.teadelivery.ordercatalog.order.controller;

import com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Rider Order Controller
 * REST API endpoints for rider order management
 */
@RestController
@RequestMapping("/api/v1/rider/orders")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Rider Orders", description = "Rider order management APIs")
public class RiderOrderController {

    private final OrderService orderService;

    /**
     * Get order for pickup
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order for pickup", description = "Get order details for pickup")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderDetailsResponse> getOrderForPickup(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Rider-Id", required = false) String riderIdHeader) {
        log.info("Getting order for pickup: orderId={}, riderId={}", orderId, riderIdHeader);

        Order order = orderService.getOrderById(orderId);

        // In production, verify rider is assigned to this order

        return ResponseEntity.ok(orderService.toCheckoutResponse(order, null, null));
    }

    /**
     * Confirm pickup
     */
    @PostMapping("/{orderId}/pickup")
    @Operation(summary = "Confirm pickup", description = "Confirm order pickup from restaurant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pickup confirmed successfully"),
            @ApiResponse(responseCode = "400", description = "Order cannot be picked up"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderDetailsResponse> confirmPickup(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Rider-Id", required = false) String riderIdHeader) {
        UUID riderId = riderIdHeader != null ? UUID.fromString(riderIdHeader) : null;

        log.info("Confirming pickup: orderId={}, riderId={}", orderId, riderId);

        Order order = orderService.pickupOrder(orderId, riderId);

        return ResponseEntity.ok(orderService.toCheckoutResponse(order, null, null));
    }

    /**
     * Confirm delivery
     */
    @PostMapping("/{orderId}/deliver")
    @Operation(summary = "Confirm delivery", description = "Confirm order delivery to customer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delivery confirmed successfully"),
            @ApiResponse(responseCode = "400", description = "Order cannot be delivered"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderDetailsResponse> confirmDelivery(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Rider-Id", required = false) String riderIdHeader) {
        UUID riderId = riderIdHeader != null ? UUID.fromString(riderIdHeader) : null;

        log.info("Confirming delivery: orderId={}, riderId={}", orderId, riderId);

        Order order = orderService.deliverOrder(orderId, riderId);

        return ResponseEntity.ok(orderService.toCheckoutResponse(order, null, null));
    }
}
