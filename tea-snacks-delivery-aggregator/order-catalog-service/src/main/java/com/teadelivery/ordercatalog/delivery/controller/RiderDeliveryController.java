package com.teadelivery.ordercatalog.delivery.controller;

import com.teadelivery.ordercatalog.delivery.dto.DeliveryResponseDTO;
import com.teadelivery.ordercatalog.delivery.dto.RejectDeliveryRequestDTO;
import com.teadelivery.ordercatalog.delivery.dto.UpdateDeliveryStatusRequestDTO;
import com.teadelivery.ordercatalog.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Rider Delivery Controller
 * Handles rider delivery operations
 */
@RestController
@RequestMapping("/api/v1/riders/{riderId}/deliveries")
@Slf4j
@Validated
@Tag(name = "Rider Deliveries", description = "APIs for riders to manage deliveries")
public class RiderDeliveryController {
    
    private final DeliveryService deliveryService;
    
    public RiderDeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }
    
    /**
     * Get deliveries for rider
     * Query param: status=AVAILABLE|CURRENT|COMPLETED|ALL
     */
    @GetMapping
    @Operation(
        summary = "Get rider deliveries",
        description = "Get deliveries for rider filtered by status with pagination"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Deliveries retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "404", description = "Rider not found")
    })
    public ResponseEntity<Page<DeliveryResponseDTO>> getDeliveries(
        @PathVariable UUID riderId,
        @Parameter(description = "Delivery status filter")
        @RequestParam(defaultValue = "AVAILABLE") String status,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Getting deliveries for rider: riderId={}, status={}, page={}, size={}", 
                 riderId, status, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<DeliveryResponseDTO> deliveries = deliveryService.getDeliveriesForRider(
            riderId, status, pageable);
        
        return ResponseEntity.ok(deliveries);
    }
    
    /**
     * Accept delivery
     */
    @PostMapping("/{deliveryId}/accept")
    @Operation(
        summary = "Accept delivery",
        description = "Rider accepts a delivery assignment"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Delivery accepted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid state transition"),
        @ApiResponse(responseCode = "404", description = "Delivery not found"),
        @ApiResponse(responseCode = "409", description = "Delivery already assigned to another rider")
    })
    public ResponseEntity<DeliveryResponseDTO> acceptDelivery(
        @PathVariable UUID riderId,
        @PathVariable UUID deliveryId
    ) {
        log.info("Rider accepting delivery: riderId={}, deliveryId={}", riderId, deliveryId);
        
        deliveryService.riderAcceptDelivery(deliveryId, riderId);
        DeliveryResponseDTO response = deliveryService.getDeliveryDTO(deliveryId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Reject delivery
     */
    @PostMapping("/{deliveryId}/reject")
    @Operation(
        summary = "Reject delivery",
        description = "Rider rejects a delivery assignment with reason"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Delivery rejected successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or state transition"),
        @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    public ResponseEntity<Void> rejectDelivery(
        @PathVariable UUID riderId,
        @PathVariable UUID deliveryId,
        @Valid @RequestBody RejectDeliveryRequestDTO request
    ) {
        log.info("Rider rejecting delivery: riderId={}, deliveryId={}, reason={}", 
                 riderId, deliveryId, request.getReason());
        
        deliveryService.riderRejectDelivery(deliveryId, riderId, request.getReason());
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * Update delivery status
     */
    @PatchMapping("/{deliveryId}/status")
    @Operation(
        summary = "Update delivery status",
        description = "Update delivery status (REACHED_RESTAURANT, PICKED_UP, OUT_FOR_DELIVERY, DELIVERED)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status or state transition"),
        @ApiResponse(responseCode = "404", description = "Delivery not found"),
        @ApiResponse(responseCode = "403", description = "Rider not assigned to this delivery")
    })
    public ResponseEntity<DeliveryResponseDTO> updateDeliveryStatus(
        @PathVariable UUID riderId,
        @PathVariable UUID deliveryId,
        @Valid @RequestBody UpdateDeliveryStatusRequestDTO request
    ) {
        log.info("Updating delivery status: riderId={}, deliveryId={}, status={}", 
                 riderId, deliveryId, request.getStatus());
        
        DeliveryResponseDTO response = deliveryService.updateDeliveryStatus(
            riderId, deliveryId, request);
        
        return ResponseEntity.ok(response);
    }
}
