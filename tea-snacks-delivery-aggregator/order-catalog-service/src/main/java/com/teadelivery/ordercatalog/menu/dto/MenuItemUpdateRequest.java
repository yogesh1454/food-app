package com.teadelivery.ordercatalog.menu.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemUpdateRequest {
    
    @Size(min = 3, max = 255, message = "Item name must be between 3 and 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @DecimalMin(value = "0.00", message = "Price must be greater than or equal to 0")
    @DecimalMax(value = "999999.99", message = "Price must not exceed 999999.99")
    private BigDecimal price;
    
    private String category;
    
    private Boolean isAvailable;
    
    @Min(value = 1, message = "Preparation time must be at least 1 minute")
    @Max(value = 240, message = "Preparation time must not exceed 240 minutes")
    private Integer preparationTimeMinutes;
    
    private Map<String, Object> metadata;
    
    private String[] tags;
}
