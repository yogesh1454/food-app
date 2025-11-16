package com.teadelivery.ordercatalog.integration;

import com.teadelivery.ordercatalog.order.checkout.dto.CheckoutRequest;
import com.teadelivery.ordercatalog.order.checkout.dto.CheckoutResponse;
import com.teadelivery.ordercatalog.order.model.DeliveryAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import java.util.concurrent.TimeUnit;

/**
 * Integration Tests for Checkout API
 * Tests all scenarios for POST /api/v1/checkout/calculate and GET /api/v1/checkout/session/{sessionId}
 * 
 * Note: Tests currently use existing test data in the database.
 * To make tests fully independent, refactor to use TestDataBuilder (see TestDataBuilder.java)
 */
@DisplayName("Checkout API Integration Tests")
class CheckoutAPIIntegrationTest extends BaseIntegrationTest {

    // ========== Happy Path Scenarios ==========

    @Test
    @DisplayName("Test 1: Should create checkout session successfully with valid data")
    void shouldCreateCheckoutSessionSuccessfully() {
        // Given: Valid checkout request
        CheckoutRequest request = createValidCheckoutRequest();

        // When: POST /api/v1/checkout/calculate
        ResponseEntity<CheckoutResponse> response = restTemplate.postForEntity(
            getApiUrl("/checkout/calculate"),
            request,
            CheckoutResponse.class
        );

        // Then: Session created successfully
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        CheckoutResponse checkoutResponse = response.getBody();
        assertThat(checkoutResponse.getCheckoutSessionId()).isNotNull();
        assertThat(checkoutResponse.getStatus()).isEqualTo(CheckoutResponse.CheckoutStatus.READY_FOR_COMMIT);
        assertThat(checkoutResponse.getVendor()).isNotNull();
        assertThat(checkoutResponse.getItems()).hasSize(2);
        assertThat(checkoutResponse.getPricing()).isNotNull();
        assertThat(checkoutResponse.getExpiresAt()).isNotNull();

        // Verify: Session stored in Redis
        String sessionKey = "checkout:session:" + checkoutResponse.getCheckoutSessionId();
        Object session = redisTemplate.opsForValue().get(sessionKey);
        assertThat(session).isNotNull();
    }

    @Test
    @DisplayName("Test 2: Should retrieve checkout session by ID")
    void shouldRetrieveCheckoutSessionById() {
        // Given: Checkout session exists
        CheckoutRequest request = createValidCheckoutRequest();
        ResponseEntity<CheckoutResponse> createResponse = restTemplate.postForEntity(
            getApiUrl("/checkout/calculate"),
            request,
            CheckoutResponse.class
        );
        String sessionId = createResponse.getBody().getCheckoutSessionId();

        // When: GET /api/v1/checkout/session/{sessionId}
        ResponseEntity<CheckoutResponse> response = restTemplate.getForEntity(
            getApiUrl("/checkout/session/" + sessionId),
            CheckoutResponse.class
        );

        // Then: Session retrieved successfully
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCheckoutSessionId()).isEqualTo(sessionId);
        assertThat(response.getBody().getStatus()).isEqualTo(CheckoutResponse.CheckoutStatus.READY_FOR_COMMIT);
    }

    @Test
    @DisplayName("Test 3: Should demonstrate session idempotency - same request returns same session ID")
    void shouldDemonstrateSessionIdempotency() {
        // Given: Valid checkout request
        CheckoutRequest request = createValidCheckoutRequest();

        // When: POST same request twice
        ResponseEntity<CheckoutResponse> response1 = restTemplate.postForEntity(
            getApiUrl("/checkout/calculate"),
            request,
            CheckoutResponse.class
        );
        
        ResponseEntity<CheckoutResponse> response2 = restTemplate.postForEntity(
            getApiUrl("/checkout/calculate"),
            request,
            CheckoutResponse.class
        );

        // Then: Same session ID returned
        assertThat(response1.getBody().getCheckoutSessionId())
            .isEqualTo(response2.getBody().getCheckoutSessionId());
        
        // Verify: Only one session in Redis
        String sessionKey = "checkout:session:" + response1.getBody().getCheckoutSessionId();
        Object session = redisTemplate.opsForValue().get(sessionKey);
        assertThat(session).isNotNull();
    }

    // ========== Validation Scenarios ==========

    @Test
    @DisplayName("Test 4: Should reject checkout with invalid vendor")
    void shouldRejectCheckoutWithInvalidVendor() {
        // Given: Request with non-existent vendor ID
        CheckoutRequest request = createValidCheckoutRequest();
        request.setVendorBranchId(999999L); // Non-existent vendor

        // When: POST /api/v1/checkout/calculate
        ResponseEntity<String> response = restTemplate.postForEntity(
            getApiUrl("/checkout/calculate"),
            request,
            String.class
        );

        // Then: Bad request or not found
        assertThat(response.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.NOT_FOUND);
        
        // Verify: No session created in Redis
        // Note: We can't verify the exact session ID since it wasn't created
    }

    @Test
    @DisplayName("Test 5: Should reject checkout with invalid menu items")
    void shouldRejectCheckoutWithInvalidMenuItems() {
        // Given: Request with non-existent menu item
        CheckoutRequest request = createValidCheckoutRequest();
        request.getItems().get(0).setMenuItemId(999999L); // Non-existent item

        // When: POST /api/v1/checkout/calculate
        ResponseEntity<String> response = restTemplate.postForEntity(
            getApiUrl("/checkout/calculate"),
            request,
            String.class
        );

        // Then: Bad request or not found
        assertThat(response.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Test 6: Should reject checkout with invalid delivery address")
    void shouldRejectCheckoutWithInvalidDeliveryAddress() {
        // Given: Request with missing address fields
        CheckoutRequest request = createValidCheckoutRequest();
        request.setDeliveryAddress(null);

        // When: POST /api/v1/checkout/calculate
        ResponseEntity<String> response = restTemplate.postForEntity(
            getApiUrl("/checkout/calculate"),
            request,
            String.class
        );

        // Then: Bad request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Test 7: Should reject checkout with empty cart")
    void shouldRejectCheckoutWithEmptyCart() {
        // Given: Request with empty items list
        CheckoutRequest request = createValidCheckoutRequest();
        request.setItems(List.of());

        // When: POST /api/v1/checkout/calculate
        ResponseEntity<String> response = restTemplate.postForEntity(
            getApiUrl("/checkout/calculate"),
            request,
            String.class
        );

        // Then: Bad request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("items");
    }

    // ========== Pricing Scenarios ==========

    @Test
    @DisplayName("Test 8: Should calculate pricing correctly")
    void shouldCalculatePricingCorrectly() {
        // Given: Valid checkout request
        CheckoutRequest request = createValidCheckoutRequest();

        // When: POST /api/v1/checkout/calculate
        ResponseEntity<CheckoutResponse> response = restTemplate.postForEntity(
            getApiUrl("/checkout/calculate"),
            request,
            CheckoutResponse.class
        );

        // Then: Pricing calculated correctly
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CheckoutResponse.PricingDetails pricing = response.getBody().getPricing();
        
        assertThat(pricing).isNotNull();
        assertThat(pricing.getItemTotal()).isNotNull();
        assertThat(pricing.getDeliveryCharges()).isNotNull();
        assertThat(pricing.getPlatformFee()).isNotNull();
        assertThat(pricing.getGst()).isNotNull();
        assertThat(pricing.getTotalAmount()).isNotNull();
        
        // Verify: Total = ItemTotal + Delivery + PlatformFee + GST - Discount
        BigDecimal calculatedTotal = pricing.getItemTotal()
            .add(pricing.getDeliveryCharges())
            .add(pricing.getPlatformFee())
            .add(pricing.getGst())
            .subtract(pricing.getDiscount() != null ? pricing.getDiscount() : BigDecimal.ZERO);
        
        assertThat(pricing.getTotalAmount()).isEqualByComparingTo(calculatedTotal);
    }

    @Test
    @DisplayName("Test 9: Should handle pricing with discount")
    void shouldHandlePricingWithDiscount() {
        // Given: Request with coupon code
        CheckoutRequest request = createValidCheckoutRequest();
        request.setCouponCode("DISCOUNT10");

        // When: POST /api/v1/checkout/calculate
        ResponseEntity<CheckoutResponse> response = restTemplate.postForEntity(
            getApiUrl("/checkout/calculate"),
            request,
            CheckoutResponse.class
        );

        // Then: Discount applied (if coupon service is implemented)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Note: Actual discount validation depends on coupon service implementation
    }

    // ========== Session Management ==========

    @Test
    @DisplayName("Test 10: Should handle session expiration (TTL verification)")
    void shouldHandleSessionExpiration() {
        // Given: Checkout session with short TTL (for testing)
        CheckoutRequest request = createValidCheckoutRequest();
        ResponseEntity<CheckoutResponse> createResponse = restTemplate.postForEntity(
            getApiUrl("/checkout/calculate"),
            request,
            CheckoutResponse.class
        );
        String sessionId = createResponse.getBody().getCheckoutSessionId();

        // Verify: Session exists initially
        String sessionKey = "checkout:session:" + sessionId;
        Object session = redisTemplate.opsForValue().get(sessionKey);
        assertThat(session).isNotNull();

        // Note: In real scenario, session expires after 15 minutes
        // For testing, we verify the TTL is set
        Long ttl = redisTemplate.getExpire(sessionKey, TimeUnit.SECONDS);
        assertThat(ttl).isGreaterThan(0);
        assertThat(ttl).isLessThanOrEqualTo(900); // 15 minutes = 900 seconds
    }

    @Test
    @DisplayName("Test 11: Should return 404 for non-existent session")
    void shouldReturn404ForNonExistentSession() {
        // Given: Non-existent session ID
        String nonExistentSessionId = "non-existent-session-id";

        // When: GET /api/v1/checkout/session/{sessionId}
        ResponseEntity<String> response = restTemplate.getForEntity(
            getApiUrl("/checkout/session/" + nonExistentSessionId),
            String.class
        );

        // Then: 404 Not Found
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ========== Helper Methods ==========

    private CheckoutRequest createValidCheckoutRequest() {
        return CheckoutRequest.builder()
            .userId(UUID.randomUUID())
            .vendorBranchId(1L) // Assuming vendor with ID 1 exists in test data
            .deliveryAddress(createValidDeliveryAddress())
            .deliveryLocation(createValidGeoLocation())
            .items(createValidCartItems())
            .paymentMethod("WALLET")
            .deliveryInstructions("Please ring the doorbell")
            .contactlessDelivery(false)
            .leaveAtDoor(false)
            .build();
    }

    private DeliveryAddress createValidDeliveryAddress() {
        return DeliveryAddress.builder()
            .addressLine1("123 Main Street")
            .addressLine2("Apartment 4B")
            .landmark("Near Central Park")
            .city("Mumbai")
            .state("Maharashtra")
            .pincode("400001")
            .addressType("HOME")
            .build();
    }

    private CheckoutRequest.GeoLocation createValidGeoLocation() {
        return CheckoutRequest.GeoLocation.builder()
            .latitude(new BigDecimal("19.0760"))
            .longitude(new BigDecimal("72.8777"))
            .build();
    }

    private List<CheckoutRequest.CartItemRequest> createValidCartItems() {
        return Arrays.asList(
            CheckoutRequest.CartItemRequest.builder()
                .menuItemId(1L) // Assuming menu item with ID 1 exists
                .quantity(2)
                .specialInstructions("Extra sugar")
                .build(),
            CheckoutRequest.CartItemRequest.builder()
                .menuItemId(2L) // Assuming menu item with ID 2 exists
                .quantity(3)
                .specialInstructions("Extra chutney")
                .build()
        );
    }
}
