package com.teadelivery.ordercatalog.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Search results containing vendors and items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Search results with vendors and menu items")
public class SearchResults {
    
    @Schema(description = "Vendor search results")
    private List<VendorSearchResult> vendors;
    
    @Schema(description = "Menu item search results")
    private List<MenuItemSearchResult> items;
}

