package com.teadelivery.ordercatalog.vendor.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchCreateRequest {
    
    @NotBlank(message = "Branch name is required")
    @Size(min = 3, max = 255, message = "Branch name must be between 3 and 255 characters")
    private String branchName;
    
    @NotBlank(message = "City is required")
    @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
    private String city;
    
    @NotNull(message = "Address is required")
    private Map<String, Object> address;
    
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;
    
    @NotBlank(message = "Branch phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String branchPhone;
    
    @NotBlank(message = "Branch email is required")
    @Email(message = "Invalid email format")
    private String branchEmail;
    
    @NotBlank(message = "Branch manager name is required")
    @Size(min = 3, max = 255, message = "Manager name must be between 3 and 255 characters")
    private String branchManagerName;
    
    // Optional: Preferences (can be set during creation or updated later)
    private Map<String, Object> preferences;
    
    // Optional: Operating hours (can be set during creation or updated later)
    private Map<String, Object> operatingHours;
}
