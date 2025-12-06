package com.teadelivery.ordercatalog.order.controller;

import com.teadelivery.ordercatalog.order.fsm.OrderState;
import com.teadelivery.ordercatalog.order.dto.AcceptOrderRequest;
import com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse;
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
 * Vendor Order Controller
 * REST API endpoints for vendor order management
 */
@RestController
@RequestMapping("/api/v1/vendor/orders")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Vendor Orders", description = "Vendor order management APIs")
public class VendorOrderController {

    private final OrderService orderService;

    /**
     * List pending orders for vendor
     */
    @GetMapping
    @Operation(summary = "List pending orders", description = "List orders pending acceptance for the vendor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    })
    public ResponseEntity<List<OrderDetailsResponse>> listPendingOrders(
            @RequestHeader(value = "X-Vendor-Id", required = false) String vendorIdHeader) {
        log.info("Listing pending orders for vendor: {}", vendorIdHeader);

        // Get orders in PENDING_ACCEPTANCE state
        List<Order> orders = orderService.getOrdersByState(OrderState.PENDING_ACCEPTANCE);

        // In production, filter by vendor ID
        // Note: Vendor filtering would require additional logic to map menu items to
        // vendors
        if (vendorIdHeader != null) {
            // TODO: Implement vendor filtering based on menu item ownership
        }

        List<OrderDetailsResponse> response = orders.stream()
                .map(order -> orderService.toCheckoutResponse(order, null, null))
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
    public ResponseEntity<OrderDetailsResponse> acceptOrder(
            @PathVariable UUID orderId,
            @RequestBody @Valid AcceptOrderRequest request,
            @RequestHeader(value = "X-Vendor-Id", required = false) String vendorIdHeader) {
        UUID vendorId = vendorIdHeader != null ? UUID.fromString(vendorIdHeader) : null;

        log.info("Accepting order: orderId={}, vendorId={}, prepTime={}",
                orderId, vendorId, request.getEstimatedPrepTime());

        Order order = orderService.acceptOrder(orderId, vendorId);

        // In production, store estimated prep time
        // order.setEstimatedPrepTime(request.getEstimatedPrepTime());

        return ResponseEntity.ok(orderService.toCheckoutResponse(order, null, null));
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
    public ResponseEntity<OrderDetailsResponse> rejectOrder(
            @PathVariable UUID orderId,
            @RequestBody @Valid RejectOrderRequest request,
            @RequestHeader(value = "X-Vendor-Id", required = false) String vendorIdHeader) {
        UUID vendorId = vendorIdHeader != null ? UUID.fromString(vendorIdHeader) : null;

        log.info("Rejecting order: orderId={}, vendorId={}, reason={}",
                orderId, vendorId, request.getReason());

        Order order = orderService.rejectOrder(orderId, vendorId, request.getReason());

        return ResponseEntity.ok(orderService.toCheckoutResponse(order, null, null));
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
    public ResponseEntity<OrderDetailsResponse> markOrderReady(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Vendor-Id", required = false) String vendorIdHeader) {
        UUID vendorId = vendorIdHeader != null ? UUID.fromString(vendorIdHeader) : null;

        log.info("Marking order ready: orderId={}, vendorId={}", orderId, vendorId);

        Order order = orderService.markReady(orderId, vendorId);

        return ResponseEntity.ok(orderService.toCheckoutResponse(order, null, null));
    }
}
