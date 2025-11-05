package com.teadelivery.ordercatalog.vendor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchResponse {
    
    private UUID branchId;
    private UUID vendorId;
    private String branchName;
    private String branchCode;
    private Map<String, Object> address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String city;
    private String branchPhone;
    private String branchEmail;
    private String branchManagerName;
    private String onboardingStatus;
    private Boolean isActive;
    private Boolean isOpen;
    private Map<String, Object> preferences;
    private Map<String, Object> operatingHours;
    private Map<String, Object> images;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
