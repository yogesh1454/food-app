package com.teadelivery.ordercatalog.vendor.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorUpdateRequest {
    
    @Size(min = 3, max = 255, message = "Company name must be between 3 and 255 characters")
    private String companyName;
    
    @Size(max = 255, message = "Brand name must not exceed 255 characters")
    private String brandName;
    
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String companyPhone;
    
    private Map<String, Object> metadata;
    
    private String[] tags;
}
