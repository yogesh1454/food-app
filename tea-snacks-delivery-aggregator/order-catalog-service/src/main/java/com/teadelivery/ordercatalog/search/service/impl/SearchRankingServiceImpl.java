package com.teadelivery.ordercatalog.search.service.impl;

import com.teadelivery.ordercatalog.search.dto.RankingScores;
import com.teadelivery.ordercatalog.search.service.SearchRankingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Search Ranking Service Implementation
 * 
 * Implements blended ranking algorithm with configurable weights
 */
@Service
@Slf4j
public class SearchRankingServiceImpl implements SearchRankingService {
    
    // Configurable ranking weights
    @Value("${search.ranking.weights.fts:0.50}")
    private Double ftWeight;
    
    @Value("${search.ranking.weights.fuzzy:0.30}")
    private Double fuzzyWeight;
    
    @Value("${search.ranking.weights.proximity:0.05}")
    private Double proximityWeight;
    
    @Value("${search.ranking.weights.popularity:0.15}")
    private Double popularityWeight;
    
    @Override
    public Double calculateBlendedScore(
            Double ftsScore,
            Double fuzzyScore,
            Double distanceKm,
            BigDecimal normalizedPopularity
    ) {
        Double proximityFactor = calculateProximityFactor(distanceKm);
        Double popularityScore = normalizedPopularity != null ? normalizedPopularity.doubleValue() : 0.0;
        
        Double fts = ftsScore != null ? ftsScore : 0.0;
        Double fuzzy = fuzzyScore != null ? fuzzyScore : 0.0;
        
        Double blendedScore = (ftWeight * fts) +
                             (fuzzyWeight * fuzzy) +
                             (proximityWeight * proximityFactor) +
                             (popularityWeight * popularityScore);
        
        return Math.max(0.0, Math.min(1.0, blendedScore)); // Clamp to [0, 1]
    }
    
    @Override
    public RankingScores calculateBlendedScoreWithBreakdown(
            Double ftsScore,
            Double fuzzyScore,
            Double distanceKm,
            BigDecimal normalizedPopularity
    ) {
        Double proximityFactor = calculateProximityFactor(distanceKm);
        Double popularityScore = normalizedPopularity != null ? normalizedPopularity.doubleValue() : 0.0;
        
        Double fts = ftsScore != null ? ftsScore : 0.0;
        Double fuzzy = fuzzyScore != null ? fuzzyScore : 0.0;
        
        Double total = calculateBlendedScore(ftsScore, fuzzyScore, distanceKm, normalizedPopularity);
        
        return RankingScores.builder()
                .total(total)
                .fts(fts)
                .fuzzy(fuzzy)
                .proximity(proximityFactor)
                .popularity(popularityScore)
                .build();
    }
    
    @Override
    public Double calculateProximityFactor(Double distanceKm) {
        if (distanceKm == null || distanceKm < 0) {
            return 0.0;
        }
        return 1.0 / (1.0 + distanceKm);
    }
}


