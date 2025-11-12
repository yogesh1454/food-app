package com.teadelivery.ordercatalog.vendor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchStatusRequest {
    
    @NotNull(message = "isOpen status is required")
    private Boolean isOpen;
}
