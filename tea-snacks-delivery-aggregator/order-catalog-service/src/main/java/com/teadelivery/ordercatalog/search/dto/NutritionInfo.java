package com.teadelivery.ordercatalog.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Nutritional information for menu items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nutritional information")
public class NutritionInfo {
    
    @Schema(description = "Calories", example = "120")
    private Integer calories;
    
    @Schema(description = "Protein in grams", example = "3.5")
    private BigDecimal protein;
    
    @Schema(description = "Carbohydrates in grams", example = "25.0")
    private BigDecimal carbs;
    
    @Schema(description = "Fat in grams", example = "2.5")
    private BigDecimal fat;
    
    @Schema(description = "Serving size", example = "200ml")
    private String servingSize;
}

