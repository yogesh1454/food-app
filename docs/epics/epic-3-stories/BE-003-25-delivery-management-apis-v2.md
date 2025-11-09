# BE-003-25: Delivery Management APIs

**Story ID:** BE-003-25  
**Story Points:** 8  
**Priority:** High (P1)  
**Sprint:** 18  
**Epic:** BE-003  
**Dependencies:** BE-003-22 (Delivery FSM), BE-003-24 (Rider Search & Notification)

---

## 📖 User Story

**As a** rider  
**I want** REST APIs to manage my deliveries  
**So that** I can accept assignments, update status, and complete deliveries

---

## ✅ Acceptance Criteria

### 1. Rider Delivery APIs
- [ ] GET `/api/v1/rider/deliveries/pending` - Get pending delivery requests
- [ ] POST `/api/v1/rider/deliveries/{deliveryId}/accept` - Accept delivery
- [ ] POST `/api/v1/rider/deliveries/{deliveryId}/reject` - Reject delivery
- [ ] GET `/api/v1/rider/deliveries/active` - Get active deliveries
- [ ] POST `/api/v1/rider/deliveries/{deliveryId}/reached-restaurant` - Mark reached restaurant
- [ ] POST `/api/v1/rider/deliveries/{deliveryId}/pickup` - Confirm pickup
- [ ] POST `/api/v1/rider/deliveries/{deliveryId}/start-delivery` - Start delivery
- [ ] POST `/api/v1/rider/deliveries/{deliveryId}/deliver` - Confirm delivery
- [ ] GET `/api/v1/rider/deliveries/{deliveryId}` - Get delivery details

### 2. Rider Status APIs
- [ ] POST `/api/v1/rider/status/online` - Go online
- [ ] POST `/api/v1/rider/status/offline` - Go offline
- [ ] POST `/api/v1/rider/status/break` - Start break
- [ ] POST `/api/v1/rider/status/resume` - Resume from break
- [ ] POST `/api/v1/rider/location` - Update location
- [ ] GET `/api/v1/rider/profile` - Get rider profile

### 3. Customer Delivery Tracking APIs
- [ ] GET `/api/v1/orders/{orderId}/delivery` - Get delivery status
- [ ] GET `/api/v1/orders/{orderId}/delivery/tracking` - Get live tracking
- [ ] GET `/api/v1/orders/{orderId}/delivery/rider` - Get rider details

### 4. Request/Response DTOs
- [ ] All DTOs with Jakarta Validation
- [ ] Proper error responses
- [ ] Consistent API structure

### 5. Authentication & Authorization
- [ ] Rider authentication (JWT)
- [ ] Verify rider owns the delivery
- [ ] Customer can only track their orders

### 6. Swagger Documentation
- [ ] All endpoints documented
- [ ] Request/response examples
- [ ] Error codes documented

---

## 🔧 Technical Implementation

### **Rider Delivery Controller**

```java
package com.teadelivery.ordercatalog.delivery.controller;

import com.teadelivery.ordercatalog.delivery.dto.*;
import com.teadelivery.ordercatalog.delivery.service.RiderDeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rider/deliveries")
@Tag(name = "Rider Delivery Management", description = "APIs for riders to manage deliveries")
@Slf4j
public class RiderDeliveryController {
    
    private final RiderDeliveryService deliveryService;
    
    public RiderDeliveryController(RiderDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }
    
    @GetMapping("/pending")
    @Operation(summary = "Get pending delivery requests")
    public ResponseEntity<List<DeliveryResponse>> getPendingDeliveries(
        @RequestHeader("X-Rider-Id") UUID riderId
    ) {
        log.info("Getting pending deliveries: riderId={}", riderId);
        List<DeliveryResponse> deliveries = deliveryService.getPendingDeliveries(riderId);
        return ResponseEntity.ok(deliveries);
    }
    
    @PostMapping("/{deliveryId}/accept")
    @Operation(summary = "Accept delivery request")
    public ResponseEntity<DeliveryResponse> acceptDelivery(
        @PathVariable UUID deliveryId,
        @RequestHeader("X-Rider-Id") UUID riderId
    ) {
        log.info("Accepting delivery: deliveryId={}, riderId={}", deliveryId, riderId);
        DeliveryResponse response = deliveryService.acceptDelivery(deliveryId, riderId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{deliveryId}/reject")
    @Operation(summary = "Reject delivery request")
    public ResponseEntity<Void> rejectDelivery(
        @PathVariable UUID deliveryId,
        @RequestHeader("X-Rider-Id") UUID riderId,
        @RequestBody @Valid RejectDeliveryRequest request
    ) {
        log.info("Rejecting delivery: deliveryId={}, riderId={}, reason={}", 
                 deliveryId, riderId, request.getReason());
        deliveryService.rejectDelivery(deliveryId, riderId, request.getReason());
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get active deliveries")
    public ResponseEntity<List<DeliveryResponse>> getActiveDeliveries(
        @RequestHeader("X-Rider-Id") UUID riderId
    ) {
        log.info("Getting active deliveries: riderId={}", riderId);
        List<DeliveryResponse> deliveries = deliveryService.getActiveDeliveries(riderId);
        return ResponseEntity.ok(deliveries);
    }
    
    @PostMapping("/{deliveryId}/reached-restaurant")
    @Operation(summary = "Mark reached restaurant")
    public ResponseEntity<DeliveryResponse> reachedRestaurant(
        @PathVariable UUID deliveryId,
        @RequestHeader("X-Rider-Id") UUID riderId
    ) {
        log.info("Rider reached restaurant: deliveryId={}, riderId={}", 
                 deliveryId, riderId);
        DeliveryResponse response = deliveryService.reachedRestaurant(deliveryId, riderId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{deliveryId}/pickup")
    @Operation(summary = "Confirm order pickup")
    public ResponseEntity<DeliveryResponse> pickupOrder(
        @PathVariable UUID deliveryId,
        @RequestHeader("X-Rider-Id") UUID riderId,
        @RequestBody @Valid PickupOrderRequest request
    ) {
        log.info("Confirming pickup: deliveryId={}, riderId={}, otp={}", 
                 deliveryId, riderId, request.getOtp());
        DeliveryResponse response = deliveryService.pickupOrder(
            deliveryId, riderId, request.getOtp());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{deliveryId}/start-delivery")
    @Operation(summary = "Start delivery to customer")
    public ResponseEntity<DeliveryResponse> startDelivery(
        @PathVariable UUID deliveryId,
        @RequestHeader("X-Rider-Id") UUID riderId
    ) {
        log.info("Starting delivery: deliveryId={}, riderId={}", deliveryId, riderId);
        DeliveryResponse response = deliveryService.startDelivery(deliveryId, riderId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{deliveryId}/deliver")
    @Operation(summary = "Confirm order delivery")
    public ResponseEntity<DeliveryResponse> deliverOrder(
        @PathVariable UUID deliveryId,
        @RequestHeader("X-Rider-Id") UUID riderId,
        @RequestBody @Valid DeliverOrderRequest request
    ) {
        log.info("Confirming delivery: deliveryId={}, riderId={}, otp={}", 
                 deliveryId, riderId, request.getOtp());
        DeliveryResponse response = deliveryService.deliverOrder(
            deliveryId, riderId, request.getOtp());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{deliveryId}")
    @Operation(summary = "Get delivery details")
    public ResponseEntity<DeliveryResponse> getDelivery(
        @PathVariable UUID deliveryId,
        @RequestHeader("X-Rider-Id") UUID riderId
    ) {
        log.info("Getting delivery details: deliveryId={}, riderId={}", 
                 deliveryId, riderId);
        DeliveryResponse response = deliveryService.getDelivery(deliveryId, riderId);
        return ResponseEntity.ok(response);
    }
}
```

### **Rider Status Controller**

```java
package com.teadelivery.ordercatalog.rider.controller;

import com.teadelivery.ordercatalog.rider.dto.*;
import com.teadelivery.ordercatalog.rider.service.RiderStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rider")
@Tag(name = "Rider Status Management", description = "APIs for rider status and location")
@Slf4j
public class RiderStatusController {
    
    private final RiderStatusService statusService;
    
    public RiderStatusController(RiderStatusService statusService) {
        this.statusService = statusService;
    }
    
    @PostMapping("/status/online")
    @Operation(summary = "Go online")
    public ResponseEntity<RiderStatusResponse> goOnline(
        @RequestHeader("X-Rider-Id") UUID riderId
    ) {
        log.info("Rider going online: riderId={}", riderId);
        RiderStatusResponse response = statusService.goOnline(riderId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/status/offline")
    @Operation(summary = "Go offline")
    public ResponseEntity<RiderStatusResponse> goOffline(
        @RequestHeader("X-Rider-Id") UUID riderId
    ) {
        log.info("Rider going offline: riderId={}", riderId);
        RiderStatusResponse response = statusService.goOffline(riderId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/status/break")
    @Operation(summary = "Start break")
    public ResponseEntity<RiderStatusResponse> startBreak(
        @RequestHeader("X-Rider-Id") UUID riderId
    ) {
        log.info("Rider starting break: riderId={}", riderId);
        RiderStatusResponse response = statusService.startBreak(riderId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/status/resume")
    @Operation(summary = "Resume from break")
    public ResponseEntity<RiderStatusResponse> resumeFromBreak(
        @RequestHeader("X-Rider-Id") UUID riderId
    ) {
        log.info("Rider resuming from break: riderId={}", riderId);
        RiderStatusResponse response = statusService.resumeFromBreak(riderId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/location")
    @Operation(summary = "Update rider location")
    public ResponseEntity<Void> updateLocation(
        @RequestHeader("X-Rider-Id") UUID riderId,
        @RequestBody @Valid UpdateLocationRequest request
    ) {
        log.debug("Updating rider location: riderId={}, lat={}, lon={}", 
                  riderId, request.getLatitude(), request.getLongitude());
        statusService.updateLocation(riderId, request);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/profile")
    @Operation(summary = "Get rider profile")
    public ResponseEntity<RiderProfileResponse> getProfile(
        @RequestHeader("X-Rider-Id") UUID riderId
    ) {
        log.info("Getting rider profile: riderId={}", riderId);
        RiderProfileResponse response = statusService.getProfile(riderId);
        return ResponseEntity.ok(response);
    }
}
```

### **Customer Delivery Tracking Controller**

```java
package com.teadelivery.ordercatalog.delivery.controller;

import com.teadelivery.ordercatalog.delivery.dto.*;
import com.teadelivery.ordercatalog.delivery.service.DeliveryTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/delivery")
@Tag(name = "Delivery Tracking", description = "APIs for customers to track deliveries")
@Slf4j
public class DeliveryTrackingController {
    
    private final DeliveryTrackingService trackingService;
    
    public DeliveryTrackingController(DeliveryTrackingService trackingService) {
        this.trackingService = trackingService;
    }
    
    @GetMapping
    @Operation(summary = "Get delivery status")
    public ResponseEntity<DeliveryStatusResponse> getDeliveryStatus(
        @PathVariable UUID orderId,
        @RequestHeader("X-Customer-Id") UUID customerId
    ) {
        log.info("Getting delivery status: orderId={}, customerId={}", 
                 orderId, customerId);
        DeliveryStatusResponse response = trackingService.getDeliveryStatus(
            orderId, customerId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/tracking")
    @Operation(summary = "Get live tracking")
    public ResponseEntity<LiveTrackingResponse> getLiveTracking(
        @PathVariable UUID orderId,
        @RequestHeader("X-Customer-Id") UUID customerId
    ) {
        log.info("Getting live tracking: orderId={}, customerId={}", 
                 orderId, customerId);
        LiveTrackingResponse response = trackingService.getLiveTracking(
            orderId, customerId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/rider")
    @Operation(summary = "Get rider details")
    public ResponseEntity<RiderDetailsResponse> getRiderDetails(
        @PathVariable UUID orderId,
        @RequestHeader("X-Customer-Id") UUID customerId
    ) {
        log.info("Getting rider details: orderId={}, customerId={}", 
                 orderId, customerId);
        RiderDetailsResponse response = trackingService.getRiderDetails(
            orderId, customerId);
        return ResponseEntity.ok(response);
    }
}
```

### **DTOs**

```java
// DeliveryResponse.java
@Data
@Builder
public class DeliveryResponse {
    private UUID deliveryId;
    private UUID orderId;
    private UUID riderId;
    private String state;
    private BigDecimal deliveryFee;
    private LocationDto pickupLocation;
    private LocationDto deliveryLocation;
    private Instant estimatedPickupTime;
    private Instant estimatedDeliveryTime;
    private Integer restaurantWaitTimeMinutes;
    private Integer totalDeliveryTimeMinutes;
}

// RejectDeliveryRequest.java
@Data
public class RejectDeliveryRequest {
    @NotBlank(message = "Rejection reason is required")
    private String reason;
}

// PickupOrderRequest.java
@Data
public class PickupOrderRequest {
    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "\\d{4}", message = "OTP must be 4 digits")
    private String otp;
}

// DeliverOrderRequest.java
@Data
public class DeliverOrderRequest {
    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "\\d{4}", message = "OTP must be 4 digits")
    private String otp;
    
    private String customerSignature; // Base64 encoded
    
    private BigDecimal codAmount; // If COD payment
}

// UpdateLocationRequest.java
@Data
public class UpdateLocationRequest {
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double longitude;
}

// RiderStatusResponse.java
@Data
@Builder
public class RiderStatusResponse {
    private UUID riderId;
    private Boolean isOnline;
    private Boolean isOnBreak;
    private Integer currentDeliveries;
    private Instant lastLocationUpdate;
}

// RiderProfileResponse.java
@Data
@Builder
public class RiderProfileResponse {
    private UUID riderId;
    private String name;
    private String phone;
    private BigDecimal rating;
    private Integer totalDeliveries;
    private Integer completedDeliveriesToday;
    private BigDecimal acceptanceRate;
    private Boolean isOnline;
    private Boolean isOnBreak;
}

// DeliveryStatusResponse.java
@Data
@Builder
public class DeliveryStatusResponse {
    private UUID deliveryId;
    private String state;
    private String stateDescription;
    private Instant estimatedDeliveryTime;
    private Boolean isRiderAssigned;
    private String riderName;
    private String riderPhone;
    private BigDecimal riderRating;
}

// LiveTrackingResponse.java
@Data
@Builder
public class LiveTrackingResponse {
    private UUID deliveryId;
    private LocationDto riderLocation;
    private LocationDto pickupLocation;
    private LocationDto deliveryLocation;
    private Integer estimatedArrivalMinutes;
    private Double distanceRemainingKm;
    private Instant lastUpdate;
}

// RiderDetailsResponse.java
@Data
@Builder
public class RiderDetailsResponse {
    private UUID riderId;
    private String name;
    private String phone;
    private BigDecimal rating;
    private String vehicleType;
    private String vehicleNumber;
}
```

### **Swagger Configuration**

```java
package com.teadelivery.ordercatalog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {
    
    @Bean
    public OpenAPI orderCatalogOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Order Catalog Service API")
                .description("REST APIs for Order & Delivery Management")
                .version("v1.0")
                .contact(new Contact()
                    .name("Tea Delivery Team")
                    .email("dev@teadelivery.com")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Local Development"),
                new Server()
                    .url("https://api.teadelivery.com")
                    .description("Production")
            ));
    }
}
```

---

## 📋 Testing Requirements

### **Unit Tests**
- [ ] Test all controller endpoints
- [ ] Test request validation
- [ ] Test error handling
- [ ] Test authorization checks

### **Integration Tests**
- [ ] Test complete delivery flow via APIs
- [ ] Test rider acceptance/rejection
- [ ] Test location updates
- [ ] Test delivery tracking
- [ ] Test concurrent requests

### **API Tests**
- [ ] Test with Postman/REST Assured
- [ ] Test error responses
- [ ] Test authentication
- [ ] Test rate limiting

---

## 📚 References

- [Delivery FSM Design](../../business-flows/03_DELIVERY_FSM_DESIGN.md)
- [REST API Standards](../../REST_API_STANDARDS.md)
- [BE-003-22: Delivery FSM Implementation](./BE-003-22-delivery-fsm-implementation-v2.md)
- [BE-003-21: Order Management APIs](./BE-003-21-order-management-apis-v2.md)

---

## 🎯 Definition of Done

- [ ] All rider delivery endpoints implemented
- [ ] All rider status endpoints implemented
- [ ] All customer tracking endpoints implemented
- [ ] All DTOs created with validation
- [ ] Swagger documentation complete
- [ ] Authentication & authorization working
- [ ] Error handling implemented
- [ ] Unit tests passing with > 80% coverage
- [ ] Integration tests passing
- [ ] API tests passing
- [ ] Code reviewed and approved
- [ ] Documentation updated
