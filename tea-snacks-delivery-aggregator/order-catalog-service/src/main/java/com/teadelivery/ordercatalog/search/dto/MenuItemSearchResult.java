package com.teadelivery.ordercatalog.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Menu item search result with enriched details for mobile UI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Menu item search result with complete details")
public class MenuItemSearchResult {
    
    @Schema(description = "Menu item ID", example = "501")
    private Long menuItemId;
    
    @Schema(description = "Item name", example = "Masala Chai")
    private String name;
    
    @Schema(description = "Item description", example = "Traditional Indian spiced tea")
    private String description;
    
    @Schema(description = "Branch ID", example = "101")
    private Long branchId;
    
    @Schema(description = "Branch name", example = "Chai Express - MG Road")
    private String branchName;
    
    @Schema(description = "Vendor name", example = "Chai Express")
    private String vendorName;
    
    @Schema(description = "Price", example = "20.00")
    private BigDecimal price;
    
    @Schema(description = "Category", example = "Beverages")
    private String category;
    
    @Schema(description = "Images with multiple sizes")
    private ImagesResponse images;
    
    @Schema(description = "Rating (0-5)", example = "4.7")
    private BigDecimal rating;
    
    @Schema(description = "Preparation time in minutes", example = "5")
    private Integer preparationTime;
    
    @Schema(description = "Dietary information", example = "[\"Vegetarian\", \"Vegan\"]")
    private List<String> dietaryInfo;
    
    @Schema(description = "Nutritional information")
    private NutritionInfo nutrition;
    
    @Schema(description = "Is item currently available", example = "true")
    private Boolean isAvailable;
    
    @Schema(description = "Availability message", example = "Available until 10 PM")
    private String availabilityMessage;
    
    @Schema(description = "Distance from user in km", example = "1.2")
    private Double distance;
    
    @Schema(description = "Order count (for trending items)", example = "2450")
    private Integer orderCount;
    
    @Schema(description = "Blended ranking score", example = "0.92")
    private Double rankingScore;
    
    @Schema(description = "Trending score (for top ordered items)", example = "0.95")
    private Double trendingScore;
    
    @Schema(description = "Recommendation score (for personalized recommendations)", example = "0.88")
    private Double recommendationScore;
    
    @Schema(description = "Ranking score breakdown (for debugging)")
    private RankingScores scores;
    
    @Schema(description = "Highlighted text for search queries", example = "Masala <em>Chai</em>")
    private String highlightedText;
}

