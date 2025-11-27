package com.teadelivery.ordercatalog.delivery.dto;

import com.teadelivery.ordercatalog.delivery.dto.LocationDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update Rider Request DTO
 * Can update status, location, or both
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRiderRequestDTO {
    
    @Pattern(
        regexp = "ONLINE|OFFLINE|ON_BREAK",
        message = "Status must be one of: ONLINE, OFFLINE, ON_BREAK"
    )
    private String status;
    
    @Valid
    private LocationDTO location;
}
