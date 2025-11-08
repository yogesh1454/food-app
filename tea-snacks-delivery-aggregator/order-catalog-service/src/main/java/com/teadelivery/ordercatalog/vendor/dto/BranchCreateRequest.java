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
    
    @Size(max = 50, message = "Branch code must not exceed 50 characters")
    private String branchCode;
    
    @Size(max = 255, message = "Display name must not exceed 255 characters")
    private String displayName;
    
    @NotBlank(message = "City is required")
    @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
    private String city;
    
    @NotNull(message = "Address is required")
    private Map<String, Object> address;
    
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;
    
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String branchPhone;
    
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String branchEmail;
    
    @Size(max = 255, message = "Manager name must not exceed 255 characters")
    private String branchManagerName;
    
    @Pattern(regexp = "^[0-9]{10}$", message = "Manager phone must be 10 digits")
    private String branchManagerPhone;
    
    // Optional: Preferences (can be set during creation or updated later)
    private Map<String, Object> preferences;
    
    // Optional: Operating hours (can be set during creation or updated later)
    private Map<String, Object> operatingHours;
}
