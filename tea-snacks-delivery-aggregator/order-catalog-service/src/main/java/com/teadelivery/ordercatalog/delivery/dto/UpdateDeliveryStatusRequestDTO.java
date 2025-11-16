package com.teadelivery.ordercatalog.delivery.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update Delivery Status Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeliveryStatusRequestDTO {
    
    @NotBlank(message = "Status is required")
    @Pattern(
        regexp = "REACHED_RESTAURANT|PICKED_UP|OUT_FOR_DELIVERY|DELIVERED",
        message = "Status must be one of: REACHED_RESTAURANT, PICKED_UP, OUT_FOR_DELIVERY, DELIVERED"
    )
    private String status;
    
    private String notes;
    
    private String deliveryProof; // Base64 image or URL for DELIVERED status
    
    private String customerSignature; // Base64 signature for DELIVERED status
    
    @Valid
    private LocationDTO currentLocation; // Optional: update location with status
}
