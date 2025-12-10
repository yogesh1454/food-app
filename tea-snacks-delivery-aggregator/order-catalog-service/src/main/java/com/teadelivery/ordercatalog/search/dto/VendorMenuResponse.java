package com.teadelivery.ordercatalog.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Complete vendor menu with categories, items, and recommendations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Complete vendor menu with categories and recommendations")
public class VendorMenuResponse {
    
    @Schema(description = "Vendor information")
    private VendorSearchResult vendor;
    
    @Schema(description = "Menu categories with items")
    private List<MenuCategoryDto> categories;
    
    @Schema(description = "Recommended items from this vendor (blended ranking)")
    private List<MenuItemSearchResult> recommendations;
    
    @Schema(description = "Popular items from this vendor")
    private List<MenuItemSearchResult> popularItems;
}

