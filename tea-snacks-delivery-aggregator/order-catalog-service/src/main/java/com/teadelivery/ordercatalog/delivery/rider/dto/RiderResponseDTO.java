package com.teadelivery.ordercatalog.delivery.rider.dto;

import com.teadelivery.ordercatalog.delivery.dto.LocationDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Rider Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderResponseDTO {
    
    private UUID riderId;
    private String name;
    private String phone;
    private String email;
    
    // Status
    private Boolean isOnline;
    private Boolean isOnBreak;
    private Integer currentDeliveries;
    
    // Location
    private LocationDTO currentLocation;
    private Instant lastLocationUpdate;
    
    // Metrics (optional, based on query param)
    private BigDecimal rating;
    private Integer totalDeliveries;
    private Integer completedDeliveriesToday;
    private BigDecimal acceptanceRate;
    
    // Earnings (optional, based on query param)
    private EarningsDTO earnings;
    
    private Instant createdAt;
}
