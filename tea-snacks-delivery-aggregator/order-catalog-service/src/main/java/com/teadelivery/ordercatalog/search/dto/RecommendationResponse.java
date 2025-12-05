package com.teadelivery.ordercatalog.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Personalized recommendations response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Personalized vendor and item recommendations")
public class RecommendationResponse {
    
    @Schema(description = "Recommended vendors based on user preferences")
    private List<VendorSearchResult> recommendedVendors;
    
    @Schema(description = "Recommended items based on order history and preferences")
    private List<MenuItemSearchResult> recommendedItems;
    
    @Schema(description = "Frequently ordered items by user")
    private List<MenuItemSearchResult> frequentlyOrdered;
    
    @Schema(description = "Recommended based on time of day (breakfast, lunch, dinner, snacks)")
    private List<MenuItemSearchResult> timeBasedRecommendations;
    
    @Schema(description = "Recommendation context", example = "Based on your order history and time of day")
    private String recommendationContext;
}

