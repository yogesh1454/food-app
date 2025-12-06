package com.teadelivery.ordercatalog.order.checkout.controller;

import com.teadelivery.ordercatalog.order.checkout.dto.CheckoutRequest;
import com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse;
import com.teadelivery.ordercatalog.order.checkout.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Checkout API
 */
@RestController
@RequestMapping("/api/v1/checkout")
@Tag(name = "Checkout", description = "Checkout calculation and session management APIs")
@Slf4j
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    /**
     * Calculate checkout and create session
     * POST /api/v1/checkout/calculate
     */
    @PostMapping("/calculate")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Calculate checkout", description = "Validates cart items, calculates pricing, and creates a checkout session. This is an idempotent operation.")
    public OrderDetailsResponse calculateCheckout(
            @Valid @RequestBody CheckoutRequest request,
            @RequestHeader(value = "X-User-Id", required = false) @Parameter(description = "User ID from JWT token") String userId) {
        log.info("Checkout calculation request received for user: {}, vendor branch: {}",
                request.getUserId(), request.getVendorBranchId());

        // TODO: Validate userId from JWT matches request.userId

        OrderDetailsResponse response = checkoutService.calculateCheckout(request);

        log.info("Checkout session created: {}, total: {}",
                response.getCheckoutSessionId(),
                response.getPricing() != null ? response.getPricing().getTotalAmount() : "N/A");

        return response;
    }

    /**
     * Get existing checkout session
     * GET /api/v1/checkout/session/{sessionId}
     */
    @GetMapping("/session/{sessionId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get checkout session", description = "Retrieve an existing checkout session by ID")
    public OrderDetailsResponse getCheckoutSession(
            @PathVariable @Parameter(description = "Checkout session ID") String sessionId) {
        log.info("Retrieving checkout session: {}", sessionId);

        OrderDetailsResponse response = checkoutService.getCheckoutSession(sessionId);

        log.info("Checkout session retrieved: {}, status: {}", sessionId, response.getStatus());

        return response;
    }

    /**
     * @deprecated Use POST /api/v1/orders instead to commit checkout and create
     *             order
     *             The /commit endpoint was incomplete - missing payment processing,
     *             session locking,
     *             validation, and event publishing. POST /api/v1/orders provides
     *             the complete
     *             6-step atomic order creation process as documented in
     *             CREATE_ORDER_API_REQUIREMENTS.md
     */

    /**
     * Health check endpoint
     * GET /api/v1/checkout/health
     */
    @GetMapping("/health")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Health check", description = "Check if checkout service is healthy")
    public String healthCheck() {
        return "Checkout service is healthy";
    }
}
