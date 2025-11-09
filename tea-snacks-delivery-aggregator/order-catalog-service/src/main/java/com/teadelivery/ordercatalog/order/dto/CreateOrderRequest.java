package com.teadelivery.ordercatalog.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating a new order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    @NotNull(message = "Order items are required")
    @Size(min = 1, message = "Must have at least one item")
    @Valid
    private List<OrderItemRequest> items;
    
    @NotNull(message = "Delivery address is required")
    private Map<String, Object> deliveryAddress;
    
    @Size(max = 500, message = "Special instructions must be less than 500 characters")
    private String specialInstructions;
}
