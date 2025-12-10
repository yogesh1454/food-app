package com.teadelivery.ordercatalog.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Discovery feed response with 4 sections:
 * 1. Nearby Vendors - Location-based discovery
 * 2. Popular Items - Popular in area (last 30 days)
 * 3. Recommended Items - Personalized recommendations
 * 4. Top Ordered Items - Trending items (last 7 days)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Discovery feed with nearby vendors, popular items, recommendations, and trending items")
public class DiscoveryFeedResponse {
    
    @Schema(description = "Nearby vendors sorted by blended ranking score")
    private List<VendorSearchResult> nearbyVendors;
    
    @Schema(description = "Popular items in the area (last 30 days)")
    private List<MenuItemSearchResult> popularItems;
    
    @Schema(description = "Recommended items based on user preferences and order history")
    private List<MenuItemSearchResult> recommendedItems;
    
    @Schema(description = "Top ordered/trending items (last 7 days)")
    private List<MenuItemSearchResult> topOrderedItems;
    
    @Schema(description = "Search suggestions for auto-complete", example = "[\"Masala Chai\", \"Samosa\", \"Filter Coffee\"]")
    private List<String> searchSuggestions;
    
    @Schema(description = "Feed metadata")
    private FeedMetadata metadata;
}

