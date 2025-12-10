package com.teadelivery.ordercatalog.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ranking score breakdown for debugging and optimization
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Blended ranking score breakdown")
public class RankingScores {
    
    @Schema(description = "Total blended score (0-1)", example = "0.89")
    private Double total;
    
    @Schema(description = "Full-text search score (0-1)", example = "0.95")
    private Double fts;
    
    @Schema(description = "Fuzzy match score (0-1)", example = "0.85")
    private Double fuzzy;
    
    @Schema(description = "Popularity score (0-1)", example = "0.78")
    private Double popularity;
    
    @Schema(description = "Proximity/distance score (0-1)", example = "0.92")
    private Double proximity;
}

