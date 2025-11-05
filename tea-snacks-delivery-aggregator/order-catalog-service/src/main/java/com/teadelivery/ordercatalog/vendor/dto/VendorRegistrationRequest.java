package com.teadelivery.ordercatalog.vendor.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorRegistrationRequest {
    
    @NotBlank(message = "Company name is required")
    @Size(min = 3, max = 255, message = "Company name must be between 3 and 255 characters")
    private String companyName;
    
    @Size(max = 255, message = "Brand name must not exceed 255 characters")
    private String brandName;
    
    @Size(max = 255, message = "Legal entity name must not exceed 255 characters")
    private String legalEntityName;
    
    @NotBlank(message = "Company email is required")
    @Email(message = "Invalid email format")
    private String companyEmail;
    
    @NotBlank(message = "Company phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String companyPhone;
    
    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z0-9]{10}$", message = "Invalid PAN format (must be 10 alphanumeric characters)")
    private String panNumber;
    
    @NotBlank(message = "GST number is required")
    @Pattern(regexp = "^[0-9]{15}$", message = "Invalid GST format (must be 15 digits)")
    private String gstNumber;
}
