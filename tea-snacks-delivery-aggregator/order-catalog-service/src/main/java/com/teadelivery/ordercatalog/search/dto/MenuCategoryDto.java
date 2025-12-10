package com.teadelivery.ordercatalog.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Menu category with items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Menu category with items")
public class MenuCategoryDto {
    
    @Schema(description = "Category name", example = "Beverages")
    private String categoryName;
    
    @Schema(description = "Category display order", example = "1")
    private Integer displayOrder;
    
    @Schema(description = "Items in this category")
    private List<MenuItemSearchResult> items;
}

