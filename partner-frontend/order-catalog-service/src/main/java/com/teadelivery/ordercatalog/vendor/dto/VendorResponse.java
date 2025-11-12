package com.teadelivery.ordercatalog.vendor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorResponse {
    
    private Long vendorId;
    private String companyName;
    private String brandName;
    private String legalEntityName;
    private String companyEmail;
    private String companyPhone;
    private String panNumber;
    private String gstNumber;
    private Map<String, Object> images;
    private Map<String, Object> metadata;
    private String[] tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
