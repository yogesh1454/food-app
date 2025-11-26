package com.teadelivery.ordercatalog.search.service;

import com.teadelivery.ordercatalog.search.dto.RecommendationResponse;

import java.util.UUID;

/**
 * Recommendation Service - Personalized vendor and item recommendations
 */
public interface RecommendationService {
    
    /**
     * Get personalized recommendations based on user preferences,
     * order history, and time of day
     * 
     * @param userId User ID
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @param radiusKm Search radius in kilometers
     * @return Personalized recommendations
     */
    RecommendationResponse getRecommendations(
            UUID userId,
            Double latitude,
            Double longitude,
            Integer radiusKm
    );
}

