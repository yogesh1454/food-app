package com.teadelivery.ordercatalog.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Unified search request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Unified search request")
public class SearchRequest {
    
    @NotBlank(message = "Search query is required")
    @Schema(description = "Search query", example = "chai", requiredMode = Schema.RequiredMode.REQUIRED)
    private String query;
    
    @Schema(description = "Search type", 
            example = "all", 
            allowableValues = {"all", "vendors", "items"},
            defaultValue = "all")
    private String type;
    
    @Min(value = -90, message = "Invalid latitude")
    @Max(value = 90, message = "Invalid latitude")
    @Schema(description = "User latitude", example = "12.9716", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double latitude;
    
    @Min(value = -180, message = "Invalid longitude")
    @Max(value = 180, message = "Invalid longitude")
    @Schema(description = "User longitude", example = "77.5946", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double longitude;
    
    @Schema(description = "Search filters (category, priceRange, dietary, etc.)")
    private Map<String, Object> filters;
    
    @Min(value = 0, message = "Page must be >= 0")
    @Schema(description = "Page number (0-based)", example = "0", defaultValue = "0")
    private Integer page;
    
    @Min(value = 1, message = "Size must be >= 1")
    @Max(value = 100, message = "Size must be <= 100")
    @Schema(description = "Page size", example = "20", defaultValue = "20")
    private Integer size;
    
    @Schema(description = "City filter", example = "Bangalore")
    private String city;
    
    @Schema(description = "Radius in km", example = "5")
    private Integer radiusKm;
}

