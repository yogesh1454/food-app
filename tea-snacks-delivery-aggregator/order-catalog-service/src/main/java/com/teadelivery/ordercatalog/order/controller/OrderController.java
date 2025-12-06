package com.teadelivery.ordercatalog.order.controller;

import com.teadelivery.ordercatalog.order.fsm.OrderState;
import com.teadelivery.ordercatalog.order.dto.CancelOrderRequest;
import com.teadelivery.ordercatalog.order.dto.CreateOrderFromCheckoutRequest;
import com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.model.OrderItem;
import com.teadelivery.ordercatalog.order.service.OrderService;
import com.teadelivery.ordercatalog.order.service.OrderCreationService;
import com.teadelivery.ordercatalog.vendor.model.VendorBranch;
import com.teadelivery.ordercatalog.vendor.repository.VendorBranchRepository;
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
import java.util.Map;
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
    private final OrderCreationService orderCreationService;
    private final VendorBranchRepository vendorBranchRepository;

    /**
     * Create order from checkout session (Two-step checkout - Step 2)
     * This is the transactional step that executes payment and creates the order
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create order from checkout session", description = "Second step of two-step checkout: executes payment and creates order from validated checkout session")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "402", description = "Payment failed"),
            @ApiResponse(responseCode = "404", description = "Checkout session not found"),
            @ApiResponse(responseCode = "409", description = "Session already committed or validation failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public OrderDetailsResponse createOrder(
            @RequestBody @Valid CreateOrderFromCheckoutRequest request) {
        log.info("Creating order from checkout session: {}", request.getCheckoutSessionId());

        try {
            // Execute 6-step atomic process
            Order order = orderCreationService.createOrderFromCheckout(request);

            log.info("Order created from checkout: orderId={}, sessionId={}, state={}",
                    order.getOrderId(), request.getCheckoutSessionId(), order.getState());

            // Get vendor details for response
            VendorBranch vendorBranch = vendorBranchRepository.findByIdWithVendor(order.getVendorBranchId())
                    .orElseThrow(() -> new IllegalStateException("Vendor branch not found"));

            return orderService.toCheckoutResponse(
                    order,
                    vendorBranch.getVendor().getCompanyName(),
                    vendorBranch.getBranchName());

        } catch (Exception e) {
            log.error("Failed to create order from checkout session: {}",
                    request.getCheckoutSessionId(), e);
            throw e;
        }
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
    public ResponseEntity<OrderDetailsResponse> getOrder(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader) {
        log.info("Getting order: orderId={}", orderId);

        Order order = orderService.getOrderById(orderId);

        // In production, verify customer owns this order
        if (customerIdHeader != null) {
            UUID customerId = UUID.fromString(customerIdHeader);
            if (!order.getCustomerId().equals(customerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        // Get vendor details for response
        VendorBranch vendorBranch = vendorBranchRepository.findByIdWithVendor(order.getVendorBranchId())
                .orElseThrow(() -> new IllegalStateException("Vendor branch not found"));

        OrderDetailsResponse response = orderService.toCheckoutResponse(
                order,
                vendorBranch.getVendor().getCompanyName(),
                vendorBranch.getBranchName());

        return ResponseEntity.ok(response);
    }

    /**
     * List customer orders
     */
    @GetMapping
    @Operation(summary = "List orders", description = "List orders for the authenticated customer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    })
    public ResponseEntity<List<OrderDetailsResponse>> listOrders(
            @RequestParam(required = false) OrderState state,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader) {
        UUID customerId = customerIdHeader != null ? UUID.fromString(customerIdHeader) : null;

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

        List<OrderDetailsResponse> response = orders.stream()
                .map(order -> {
                    VendorBranch vendorBranch = vendorBranchRepository.findByIdWithVendor(order.getVendorBranchId())
                            .orElse(null);
                    String vendorName = vendorBranch != null ? vendorBranch.getVendor().getCompanyName() : "Unknown";
                    String branchName = vendorBranch != null ? vendorBranch.getBranchName() : "Unknown";
                    return orderService.toCheckoutResponse(order, vendorName, branchName);
                })
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
    public ResponseEntity<OrderDetailsResponse> cancelOrder(
            @PathVariable UUID orderId,
            @RequestBody @Valid CancelOrderRequest request,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader) {
        UUID customerId = customerIdHeader != null ? UUID.fromString(customerIdHeader) : null;

        log.info("Cancelling order: orderId={}, customerId={}", orderId, customerId);

        request.setCancelledBy("CUSTOMER");

        Order order = orderService.cancelOrder(
                orderId,
                customerId,
                request.getCancelledBy(),
                request.getReason());

        // Get vendor details for response
        VendorBranch vendorBranch = vendorBranchRepository.findByIdWithVendor(order.getVendorBranchId())
                .orElseThrow(() -> new IllegalStateException("Vendor branch not found"));

        OrderDetailsResponse response = orderService.toCheckoutResponse(
                order,
                vendorBranch.getVendor().getCompanyName(),
                vendorBranch.getBranchName());

        return ResponseEntity.ok(response);
    }
}
