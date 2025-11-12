package com.teadelivery.ordercatalog.vendor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Branch operating hours details")
public class OperatingHoursResponse {
    
    @Schema(description = "Branch ID", example = "1")
    private Long branchId;
    
    @Schema(description = "Operating hours by day of week with multiple time slots per day")
    private Map<String, Object> operatingHours;
    
    @Schema(description = "Whether the branch is currently open for orders")
    private Boolean isOpen;
}
