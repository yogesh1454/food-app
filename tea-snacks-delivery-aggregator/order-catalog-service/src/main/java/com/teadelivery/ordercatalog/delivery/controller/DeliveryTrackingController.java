package com.teadelivery.ordercatalog.delivery.controller;

import com.teadelivery.ordercatalog.delivery.dto.DeliveryResponseDTO;
import com.teadelivery.ordercatalog.delivery.dto.LocationDTO;
import com.teadelivery.ordercatalog.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Delivery Tracking Controller
 * Handles customer delivery tracking
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
@Validated
@Tag(name = "Delivery Tracking", description = "APIs for customers to track deliveries")
public class DeliveryTrackingController {
    
    private final DeliveryService deliveryService;
    
    public DeliveryTrackingController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }
    
    /**
     * Get delivery details
     */
    @GetMapping("/deliveries/{deliveryId}")
    @Operation(
        summary = "Get delivery details",
        description = "Get complete delivery information including status, timestamps, and locations"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Delivery details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    public ResponseEntity<DeliveryResponseDTO> getDelivery(
        @PathVariable UUID deliveryId
    ) {
        log.info("Getting delivery details: deliveryId={}", deliveryId);
        
        DeliveryResponseDTO response = deliveryService.getDeliveryDTO(deliveryId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get real-time rider location
     */
    @GetMapping("/deliveries/{deliveryId}/location")
    @Operation(
        summary = "Get rider location",
        description = "Get real-time location of the rider assigned to this delivery"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Location retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Delivery not found or no rider assigned"),
        @ApiResponse(responseCode = "400", description = "Delivery not in active state")
    })
    public ResponseEntity<LocationDTO> getRiderLocation(
        @PathVariable UUID deliveryId
    ) {
        log.info("Getting rider location for delivery: deliveryId={}", deliveryId);
        
        LocationDTO location = deliveryService.getRiderLocationForDelivery(deliveryId);
        
        return ResponseEntity.ok(location);
    }
    
    /**
     * Get delivery by order ID
     */
    @GetMapping("/orders/{orderId}/delivery")
    @Operation(
        summary = "Get delivery by order ID",
        description = "Get delivery information for a specific order"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Delivery retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Order or delivery not found")
    })
    public ResponseEntity<DeliveryResponseDTO> getDeliveryByOrderId(
        @PathVariable UUID orderId
    ) {
        log.info("Getting delivery for order: orderId={}", orderId);
        
        DeliveryResponseDTO response = deliveryService.getDeliveryByOrderIdDTO(orderId);
        
        return ResponseEntity.ok(response);
    }
}
