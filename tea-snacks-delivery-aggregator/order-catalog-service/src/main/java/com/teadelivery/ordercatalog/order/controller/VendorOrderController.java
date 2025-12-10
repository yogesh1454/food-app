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

import java.util.UUID;

/**
 * Vendor Order Controller
 * REST API endpoints for vendor order management
 */
@RestController
@RequestMapping("/api/v1/orders")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Vendor Orders", description = "Vendor order management APIs")
public class VendorOrderController {

        private final OrderService orderService;

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

        /**
         * Transition order to target state (for testing/admin)
         * Uses FSM to validate and execute the transition
         */
        @PostMapping("/{orderId}/status")
        @Operation(summary = "Transition order state", description = "Transition order to target state using FSM. Validates that the transition is allowed from current state.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Order transitioned successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid transition - target state not reachable from current state"),
                        @ApiResponse(responseCode = "404", description = "Order not found")
        })
        public ResponseEntity<OrderDetailsResponse> transitionOrderStatus(
                        @PathVariable UUID orderId,
                        @RequestParam("targetState") OrderState targetState,
                        @RequestParam(value = "reason", required = false) String reason,
                        @RequestHeader(value = "X-Vendor-Id", required = false) String vendorIdHeader,
                        @RequestHeader(value = "X-Actor-Type", required = false, defaultValue = "SYSTEM") String actorType) {

                UUID actorId = vendorIdHeader != null ? UUID.fromString(vendorIdHeader) : null;

                log.info("Transitioning order: orderId={}, targetState={}, actorId={}, actorType={}",
                                orderId, targetState, actorId, actorType);

                Order order = orderService.transitionToState(orderId, targetState, actorId, actorType, reason);

                return ResponseEntity.ok(orderService.toCheckoutResponse(order, null, null));
        }
}
