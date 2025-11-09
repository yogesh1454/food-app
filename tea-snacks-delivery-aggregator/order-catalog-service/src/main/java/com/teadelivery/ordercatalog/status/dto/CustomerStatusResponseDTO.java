package com.teadelivery.ordercatalog.status.dto;

import com.teadelivery.ordercatalog.delivery.dto.LocationDTO;
import com.teadelivery.ordercatalog.status.model.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Customer Status Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatusResponseDTO {
    
    private UUID orderId;
    private CustomerStatus status;
    private String primaryMessage;
    private String secondaryMessage;
    private Integer progressPercentage;
    private Boolean canCancel;
    
    // Timing
    private Instant estimatedArrival;
    private Integer estimatedMinutesRemaining;
    
    // Rider info (if assigned)
    private RiderInfoDTO riderInfo;
    
    // Order details
    private Instant orderPlacedAt;
    private Instant lastUpdatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiderInfoDTO {
        private UUID riderId;
        private String name;
        private String phone;
        private Double rating;
        private LocationDTO currentLocation;
    }
}
