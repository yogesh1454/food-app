package com.teadelivery.ordercatalog.rider.controller;

import com.teadelivery.ordercatalog.delivery.dto.LocationDTO;
import com.teadelivery.ordercatalog.rider.dto.RiderResponseDTO;
import com.teadelivery.ordercatalog.rider.dto.UpdateRiderRequestDTO;
import com.teadelivery.ordercatalog.rider.service.RiderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Rider Status Controller
 * Handles rider status and location updates
 */
@RestController
@RequestMapping("/api/v1/riders")
@Slf4j
@Validated
@Tag(name = "Rider Status", description = "APIs for rider status and location management")
public class RiderStatusController {
    
    private final RiderService riderService;
    
    public RiderStatusController(RiderService riderService) {
        this.riderService = riderService;
    }
    
    /**
     * Update rider (status and/or location)
     */
    @PatchMapping("/{riderId}")
    @Operation(
        summary = "Update rider",
        description = "Update rider status and/or location. Can update one or both fields."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rider updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Rider not found")
    })
    public ResponseEntity<RiderResponseDTO> updateRider(
        @PathVariable UUID riderId,
        @Valid @RequestBody UpdateRiderRequestDTO request
    ) {
        log.info("Updating rider: riderId={}, status={}, location={}", 
                 riderId, request.getStatus(), 
                 request.getLocation() != null ? "provided" : "null");
        
        RiderResponseDTO response = riderService.updateRider(riderId, request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update rider location (high-frequency endpoint)
     */
    @PatchMapping("/{riderId}/location")
    @Operation(
        summary = "Update rider location",
        description = "Update rider's current location. Optimized for high-frequency updates."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Location updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid location data"),
        @ApiResponse(responseCode = "404", description = "Rider not found")
    })
    public ResponseEntity<Void> updateLocation(
        @PathVariable UUID riderId,
        @Valid @RequestBody LocationDTO location
    ) {
        log.debug("Updating rider location: riderId={}, lat={}, lon={}", 
                  riderId, location.getLatitude(), location.getLongitude());
        
        riderService.updateRiderLocation(riderId, location);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get rider info
     */
    @GetMapping("/{riderId}")
    @Operation(
        summary = "Get rider information",
        description = "Get rider details including status, location, and optionally earnings/stats"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rider info retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Rider not found")
    })
    public ResponseEntity<RiderResponseDTO> getRider(
        @PathVariable UUID riderId,
        @Parameter(description = "Include additional data: STATUS, EARNINGS, STATS, ALL")
        @RequestParam(defaultValue = "STATUS") String include
    ) {
        log.info("Getting rider info: riderId={}, include={}", riderId, include);
        
        RiderResponseDTO response = riderService.getRider(riderId, include);
        
        return ResponseEntity.ok(response);
    }
}
