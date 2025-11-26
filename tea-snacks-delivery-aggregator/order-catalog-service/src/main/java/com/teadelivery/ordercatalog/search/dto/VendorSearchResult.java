package com.teadelivery.ordercatalog.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Vendor search result with enriched details for mobile UI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Vendor/branch search result with complete details")
public class VendorSearchResult {
    
    @Schema(description = "Branch ID", example = "101")
    private Long branchId;
    
    @Schema(description = "Vendor ID", example = "1")
    private Long vendorId;
    
    @Schema(description = "Branch name", example = "Chai Express - MG Road")
    private String branchName;
    
    @Schema(description = "Display name", example = "Chai Express")
    private String displayName;
    
    @Schema(description = "Cuisine types", example = "[\"Tea\", \"Snacks\"]")
    private List<String> cuisine;
    
    @Schema(description = "Rating (0-5)", example = "4.5")
    private BigDecimal rating;
    
    @Schema(description = "Total number of ratings", example = "1250")
    private Integer totalRatings;
    
    @Schema(description = "Delivery time estimate", example = "20-25 min")
    private String deliveryTime;
    
    @Schema(description = "Distance in km", example = "1.2")
    private Double distance;
    
    @Schema(description = "Distance unit", example = "km")
    private String distanceUnit;
    
    @Schema(description = "Delivery fee", example = "20.00")
    private BigDecimal deliveryFee;
    
    @Schema(description = "Minimum order value", example = "50.00")
    private BigDecimal minOrderValue;
    
    @Schema(description = "Is vendor currently open", example = "true")
    private Boolean isOpen;
    
    @Schema(description = "Opening time if currently closed", example = "Opens at 9:00 AM")
    private String openingTime;
    
    @Schema(description = "Images with multiple sizes")
    private ImagesResponse images;
    
    @Schema(description = "Tags", example = "[\"Fast Delivery\", \"Popular\"]")
    private List<String> tags;
    
    @Schema(description = "Blended ranking score", example = "0.87")
    private Double rankingScore;
    
    @Schema(description = "Ranking score breakdown (for debugging)")
    private RankingScores scores;
    
    @Schema(description = "Highlighted text for search queries", example = "<em>Chai</em> Express")
    private String highlightedText;
}

