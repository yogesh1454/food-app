package com.teadelivery.ordercatalog.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reject Delivery Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectDeliveryRequestDTO {
    
    @NotBlank(message = "Rejection reason is required")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    private String reason;
}
