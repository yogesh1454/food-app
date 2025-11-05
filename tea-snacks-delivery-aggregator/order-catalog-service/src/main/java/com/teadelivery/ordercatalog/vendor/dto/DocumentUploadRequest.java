package com.teadelivery.ordercatalog.vendor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {
    
    @NotBlank(message = "Document type is required (FSSAI, SHOP_ACT, GST, ID_PROOF, MENU)")
    private String documentType;
    
    @NotBlank(message = "Document number is required")
    private String documentNumber;
    
    private LocalDate issueDate;
    
    private LocalDate expiryDate;
}
