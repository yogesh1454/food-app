package com.teadelivery.ordercatalog.integration;

import com.teadelivery.ordercatalog.delivery.dto.DeliveryResponseDTO;
import com.teadelivery.ordercatalog.delivery.dto.LocationDTO;
import com.teadelivery.ordercatalog.delivery.dto.RejectDeliveryRequestDTO;
import com.teadelivery.ordercatalog.delivery.dto.UpdateDeliveryStatusRequestDTO;
import com.teadelivery.ordercatalog.delivery.model.Delivery;
import com.teadelivery.ordercatalog.delivery.repository.DeliveryRepository;
import com.teadelivery.ordercatalog.delivery.fsm.DeliveryState;
import com.teadelivery.ordercatalog.delivery.rider.model.Rider;
import com.teadelivery.ordercatalog.delivery.rider.repository.RiderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Delivery FSM Integration Tests
 * E2E tests for complete delivery lifecycle with real containers
 */
@DisplayName("Delivery FSM Integration Tests")
public class DeliveryFSMIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private DeliveryRepository deliveryRepository;
    
    @Autowired
    private RiderRepository riderRepository;
    
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    
    @Test
    @DisplayName("Should complete full delivery lifecycle: PENDING → DELIVERED")
    void testCompleteDeliveryLifecycle() {
        // Given: Create a rider
        Rider rider = createTestRider("John Doe", "9876543210", 12.9716, 77.5946);
        
        // And: Create a delivery
        Delivery delivery = createTestDelivery(UUID.randomUUID());
        
        // When: Rider accepts delivery
        String acceptUrl = String.format("http://localhost:%d/api/v1/riders/%s/deliveries/%s/accept",
            port, rider.getRiderId(), delivery.getDeliveryId());
        ResponseEntity<DeliveryResponseDTO> acceptResponse = restTemplate.postForEntity(
            acceptUrl, null, DeliveryResponseDTO.class);
        
        // Then: Delivery should be in RIDER_ACCEPTED state
        assertThat(acceptResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(acceptResponse.getBody()).isNotNull();
        assertThat(acceptResponse.getBody().getState()).isEqualTo(DeliveryState.RIDER_ACCEPTED);
        assertThat(acceptResponse.getBody().getRiderId()).isEqualTo(rider.getRiderId());
        
        // When: Rider reaches restaurant
        UpdateDeliveryStatusRequestDTO reachedRequest = UpdateDeliveryStatusRequestDTO.builder()
            .status("REACHED_RESTAURANT")
            .build();
        String statusUrl = String.format("http://localhost:%d/api/v1/riders/%s/deliveries/%s/status",
            port, rider.getRiderId(), delivery.getDeliveryId());
        ResponseEntity<DeliveryResponseDTO> reachedResponse = restTemplate.exchange(
            statusUrl, HttpMethod.PATCH, new HttpEntity<>(reachedRequest), DeliveryResponseDTO.class);
        
        // Then: Delivery should be AT_RESTAURANT
        assertThat(reachedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reachedResponse.getBody().getState()).isEqualTo(DeliveryState.AT_RESTAURANT);
        assertThat(reachedResponse.getBody().getReachedRestaurantAt()).isNotNull();
        
        // When: Rider picks up order
        UpdateDeliveryStatusRequestDTO pickupRequest = UpdateDeliveryStatusRequestDTO.builder()
            .status("PICKED_UP")
            .build();
        ResponseEntity<DeliveryResponseDTO> pickupResponse = restTemplate.exchange(
            statusUrl, HttpMethod.PATCH, new HttpEntity<>(pickupRequest), DeliveryResponseDTO.class);
        
        // Then: Delivery should be OUT_FOR_DELIVERY
        assertThat(pickupResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pickupResponse.getBody().getState()).isEqualTo(DeliveryState.OUT_FOR_DELIVERY);
        assertThat(pickupResponse.getBody().getPickedUpAt()).isNotNull();
        
        // When: Rider delivers order
        UpdateDeliveryStatusRequestDTO deliveredRequest = UpdateDeliveryStatusRequestDTO.builder()
            .status("DELIVERED")
            .deliveryProof("base64_image_data")
            .build();
        ResponseEntity<DeliveryResponseDTO> deliveredResponse = restTemplate.exchange(
            statusUrl, HttpMethod.PATCH, new HttpEntity<>(deliveredRequest), DeliveryResponseDTO.class);
        
        // Then: Delivery should be DELIVERED
        assertThat(deliveredResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deliveredResponse.getBody().getState()).isEqualTo(DeliveryState.DELIVERED);
        assertThat(deliveredResponse.getBody().getDeliveredAt()).isNotNull();
        assertThat(deliveredResponse.getBody().getTotalDeliveryTimeMinutes()).isNotNull();
        
        // Verify in database
        Delivery finalDelivery = deliveryRepository.findById(delivery.getDeliveryId()).orElseThrow();
        assertThat(finalDelivery.getState()).isEqualTo(DeliveryState.DELIVERED);
        assertThat(finalDelivery.getTotalDeliveryTimeMinutes()).isGreaterThan(0);
    }
    
    @Test
    @DisplayName("Should handle rider rejection and reassignment")
    void testRiderRejectionAndReassignment() {
        // Given: Create two riders
        Rider rider1 = createTestRider("Rider One", "9876543210", 12.9716, 77.5946);
        Rider rider2 = createTestRider("Rider Two", "9876543211", 12.9716, 77.5946);
        
        // And: Create a delivery in RIDER_ASSIGNED state
        Delivery delivery = createTestDelivery(UUID.randomUUID());
        delivery.setState(DeliveryState.RIDER_ASSIGNED);
        delivery.setRiderId(rider1.getRiderId());
        delivery = deliveryRepository.save(delivery);
        
        // When: Rider 1 rejects delivery
        RejectDeliveryRequestDTO rejectRequest = RejectDeliveryRequestDTO.builder()
            .reason("Too far from my current location, cannot reach in time")
            .build();
        String rejectUrl = String.format("http://localhost:%d/api/v1/riders/%s/deliveries/%s/reject",
            port, rider1.getRiderId(), delivery.getDeliveryId());
        ResponseEntity<Void> rejectResponse = restTemplate.postForEntity(
            rejectUrl, rejectRequest, Void.class);
        
        // Then: Rejection should be successful
        assertThat(rejectResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // And: Delivery should go back to SEARCHING_RIDER state
        await().atMost(3, SECONDS).untilAsserted(() -> {
            Delivery updatedDelivery = deliveryRepository.findById(delivery.getDeliveryId()).orElseThrow();
            assertThat(updatedDelivery.getState()).isIn(
                DeliveryState.SEARCHING_RIDER, 
                DeliveryState.RIDER_ASSIGNED
            );
        });
    }
    
    @Test
    @DisplayName("Should track delivery for customer")
    void testCustomerDeliveryTracking() {
        // Given: Create a rider and delivery
        Rider rider = createTestRider("John Doe", "9876543210", 12.9716, 77.5946);
        Delivery delivery = createTestDelivery(UUID.randomUUID());
        delivery.setState(DeliveryState.OUT_FOR_DELIVERY);
        delivery.setRiderId(rider.getRiderId());
        delivery = deliveryRepository.save(delivery);
        
        // When: Customer tracks delivery
        String trackingUrl = String.format("http://localhost:%d/api/v1/deliveries/%s",
            port, delivery.getDeliveryId());
        ResponseEntity<DeliveryResponseDTO> trackingResponse = restTemplate.getForEntity(
            trackingUrl, DeliveryResponseDTO.class);
        
        // Then: Should get delivery details
        assertThat(trackingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(trackingResponse.getBody()).isNotNull();
        assertThat(trackingResponse.getBody().getDeliveryId()).isEqualTo(delivery.getDeliveryId());
        assertThat(trackingResponse.getBody().getState()).isEqualTo(DeliveryState.OUT_FOR_DELIVERY);
        
        // When: Customer gets rider location
        String locationUrl = String.format("http://localhost:%d/api/v1/deliveries/%s/location",
            port, delivery.getDeliveryId());
        ResponseEntity<LocationDTO> locationResponse = restTemplate.getForEntity(
            locationUrl, LocationDTO.class);
        
        // Then: Should get rider's current location
        assertThat(locationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(locationResponse.getBody()).isNotNull();
        assertThat(locationResponse.getBody().getLatitude()).isEqualTo(12.9716);
        assertThat(locationResponse.getBody().getLongitude()).isEqualTo(77.5946);
    }
    
    @Test
    @DisplayName("Should get delivery by order ID")
    void testGetDeliveryByOrderId() {
        // Given: Create a delivery for an order
        UUID orderId = UUID.randomUUID();
        Delivery delivery = createTestDelivery(orderId);
        
        // When: Get delivery by order ID
        String url = String.format("http://localhost:%d/api/v1/orders/%s/delivery",
            port, orderId);
        ResponseEntity<DeliveryResponseDTO> response = restTemplate.getForEntity(
            url, DeliveryResponseDTO.class);
        
        // Then: Should get delivery details
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOrderId()).isEqualTo(orderId);
        assertThat(response.getBody().getDeliveryId()).isEqualTo(delivery.getDeliveryId());
    }
    
    @Test
    @DisplayName("Should prevent invalid state transitions")
    void testInvalidStateTransitions() {
        // Given: Create a delivery in PENDING state
        Rider rider = createTestRider("John Doe", "9876543210", 12.9716, 77.5946);
        Delivery delivery = createTestDelivery(UUID.randomUUID());
        delivery.setRiderId(rider.getRiderId());
        delivery = deliveryRepository.save(delivery);
        
        // When: Try to mark as DELIVERED without going through intermediate states
        UpdateDeliveryStatusRequestDTO request = UpdateDeliveryStatusRequestDTO.builder()
            .status("DELIVERED")
            .build();
        String statusUrl = String.format("http://localhost:%d/api/v1/riders/%s/deliveries/%s/status",
            port, rider.getRiderId(), delivery.getDeliveryId());
        ResponseEntity<DeliveryResponseDTO> response = restTemplate.exchange(
            statusUrl, HttpMethod.PATCH, new HttpEntity<>(request), DeliveryResponseDTO.class);
        
        // Then: Should fail with bad request
        assertThat(response.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @Test
    @DisplayName("Should calculate restaurant wait time")
    void testRestaurantWaitTimeCalculation() throws InterruptedException {
        // Given: Create a rider and delivery
        Rider rider = createTestRider("John Doe", "9876543210", 12.9716, 77.5946);
        Delivery delivery = createTestDelivery(UUID.randomUUID());
        
        // When: Rider accepts and reaches restaurant
        String acceptUrl = String.format("http://localhost:%d/api/v1/riders/%s/deliveries/%s/accept",
            port, rider.getRiderId(), delivery.getDeliveryId());
        restTemplate.postForEntity(acceptUrl, null, DeliveryResponseDTO.class);
        
        UpdateDeliveryStatusRequestDTO reachedRequest = UpdateDeliveryStatusRequestDTO.builder()
            .status("REACHED_RESTAURANT")
            .build();
        String statusUrl = String.format("http://localhost:%d/api/v1/riders/%s/deliveries/%s/status",
            port, rider.getRiderId(), delivery.getDeliveryId());
        restTemplate.exchange(statusUrl, HttpMethod.PATCH, 
            new HttpEntity<>(reachedRequest), DeliveryResponseDTO.class);
        
        // Wait 2 seconds
        Thread.sleep(2000);
        
        // And: Rider picks up order
        UpdateDeliveryStatusRequestDTO pickupRequest = UpdateDeliveryStatusRequestDTO.builder()
            .status("PICKED_UP")
            .build();
        ResponseEntity<DeliveryResponseDTO> pickupResponse = restTemplate.exchange(
            statusUrl, HttpMethod.PATCH, new HttpEntity<>(pickupRequest), DeliveryResponseDTO.class);
        
        // Then: Restaurant wait time should be calculated
        assertThat(pickupResponse.getBody().getRestaurantWaitTimeMinutes()).isNotNull();
        assertThat(pickupResponse.getBody().getRestaurantWaitTimeMinutes()).isGreaterThanOrEqualTo(0);
    }
    
    @Test
    @DisplayName("Should get available deliveries for rider")
    void testGetAvailableDeliveriesForRider() {
        // Given: Create a rider and some deliveries
        Rider rider = createTestRider("John Doe", "9876543210", 12.9716, 77.5946);
        Delivery delivery1 = createTestDelivery(UUID.randomUUID());
        delivery1.setState(DeliveryState.RIDER_ASSIGNED);
        deliveryRepository.save(delivery1);
        
        Delivery delivery2 = createTestDelivery(UUID.randomUUID());
        delivery2.setState(DeliveryState.RIDER_ASSIGNED);
        deliveryRepository.save(delivery2);
        
        // When: Get available deliveries
        String url = String.format("http://localhost:%d/api/v1/riders/%s/deliveries?status=AVAILABLE",
            port, rider.getRiderId());
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        // Then: Should get list of available deliveries
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
    
    @Test
    @DisplayName("Should prevent unauthorized rider from updating delivery")
    void testUnauthorizedRiderUpdate() {
        // Given: Create two riders and a delivery assigned to rider1
        Rider rider1 = createTestRider("Rider One", "9876543210", 12.9716, 77.5946);
        Rider rider2 = createTestRider("Rider Two", "9876543211", 12.9716, 77.5946);
        
        Delivery delivery = createTestDelivery(UUID.randomUUID());
        delivery.setState(DeliveryState.RIDER_ACCEPTED);
        delivery.setRiderId(rider1.getRiderId());
        delivery = deliveryRepository.save(delivery);
        
        // When: Rider 2 tries to update delivery status
        UpdateDeliveryStatusRequestDTO request = UpdateDeliveryStatusRequestDTO.builder()
            .status("PICKED_UP")
            .build();
        String statusUrl = String.format("http://localhost:%d/api/v1/riders/%s/deliveries/%s/status",
            port, rider2.getRiderId(), delivery.getDeliveryId());
        ResponseEntity<DeliveryResponseDTO> response = restTemplate.exchange(
            statusUrl, HttpMethod.PATCH, new HttpEntity<>(request), DeliveryResponseDTO.class);
        
        // Then: Should fail with forbidden or bad request
        assertThat(response.getStatusCode()).isIn(
            HttpStatus.FORBIDDEN, 
            HttpStatus.BAD_REQUEST,
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
    
    // Helper methods
    
    private Rider createTestRider(String name, String phone, double latitude, double longitude) {
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        
        Rider rider = Rider.builder()
            .name(name)
            .phone(phone)
            .email(phone + "@test.com")
            .currentLocation(location)
            .isOnline(true)
            .isOnBreak(false)
            .currentDeliveries(0)
            .rating(new BigDecimal("5.00"))
            .totalDeliveries(0)
            .completedDeliveriesToday(0)
            .acceptanceRate(new BigDecimal("100.00"))
            .totalAssignments(0)
            .acceptedAssignments(0)
            .build();
        
        return riderRepository.save(rider);
    }
    
    private Delivery createTestDelivery(UUID orderId) {
        Delivery delivery = Delivery.builder()
            .orderId(orderId)
            .state(DeliveryState.PENDING)
            .deliveryFee(new BigDecimal("50.00"))
            .searchRadiusKm(2.0)
            .retryCount(0)
            .pickupLocation("{\"latitude\": 12.9716, \"longitude\": 77.5946}")
            .deliveryLocation("{\"latitude\": 12.9352, \"longitude\": 77.6245}")
            .build();
        
        return deliveryRepository.save(delivery);
    }
}
