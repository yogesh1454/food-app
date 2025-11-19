package com.teadelivery.ordercatalog.vendor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchAvailabilityResponse {
    
    private Long branchId;
    private Boolean isOpen;
    private Boolean isActive;
    private Boolean isWithinOperatingHours;
    private String currentStatus;  // OPEN, OFFLINE, CLOSED
    private List<Map<String, String>> todayHours;
    private LocalTime nextOpenTime;
    private LocalTime nextCloseTime;
}
