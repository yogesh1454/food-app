package com.teadelivery.ordercatalog.search.service;

import com.teadelivery.ordercatalog.search.dto.RankingScores;

import java.math.BigDecimal;

/**
 * Search Ranking Service
 * 
 * Centralized service for blended ranking calculations
 */
public interface SearchRankingService {
    
    /**
     * Calculate blended ranking score
     * 
     * Formula: TotalScore = (W_fts × fts) + (W_fuzzy × fuzzy) + (W_geo × proximity) + (W_pop × popularity)
     * 
     * @param ftsScore Full-text search score (0-1)
     * @param fuzzyScore Fuzzy match score (0-1)
     * @param distanceKm Distance in kilometers
     * @param normalizedPopularity Normalized popularity score (0-1)
     * @return Blended ranking score (0-1)
     */
    Double calculateBlendedScore(
            Double ftsScore,
            Double fuzzyScore,
            Double distanceKm,
            BigDecimal normalizedPopularity
    );
    
    /**
     * Calculate blended score with breakdown
     * 
     * @param ftsScore Full-text search score (0-1)
     * @param fuzzyScore Fuzzy match score (0-1)
     * @param distanceKm Distance in kilometers
     * @param normalizedPopularity Normalized popularity score (0-1)
     * @return RankingScores with breakdown
     */
    RankingScores calculateBlendedScoreWithBreakdown(
            Double ftsScore,
            Double fuzzyScore,
            Double distanceKm,
            BigDecimal normalizedPopularity
    );
    
    /**
     * Calculate proximity factor from distance
     * 
     * Formula: 1 / (1 + distance_km)
     * 
     * @param distanceKm Distance in kilometers
     * @return Proximity factor (0-1, higher is closer)
     */
    Double calculateProximityFactor(Double distanceKm);
}


