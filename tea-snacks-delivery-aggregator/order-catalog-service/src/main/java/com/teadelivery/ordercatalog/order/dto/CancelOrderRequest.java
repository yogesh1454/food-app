package com.teadelivery.ordercatalog.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for cancelling an order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequest {
    
    @NotNull(message = "Reason is required")
    @NotBlank(message = "Reason cannot be blank")
    private String reason;
    
    private String cancelledBy;
}
