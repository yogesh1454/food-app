package com.teadelivery.ordercatalog.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for restaurant accepting an order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptOrderRequest {
    
    @NotNull(message = "Estimated prep time is required")
    @Min(value = 5, message = "Minimum prep time is 5 minutes")
    @Max(value = 120, message = "Maximum prep time is 120 minutes")
    private Integer estimatedPrepTime;
}
