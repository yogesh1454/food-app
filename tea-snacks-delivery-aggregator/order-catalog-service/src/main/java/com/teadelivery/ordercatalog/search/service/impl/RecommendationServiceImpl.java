package com.teadelivery.ordercatalog.search.service.impl;

import com.teadelivery.ordercatalog.search.dto.RecommendationResponse;
import com.teadelivery.ordercatalog.search.mapper.SearchMapper;
import com.teadelivery.ordercatalog.search.model.SearchMenuItem;
import com.teadelivery.ordercatalog.search.model.SearchVendor;
import com.teadelivery.ordercatalog.search.repository.SearchMenuItemRepository;
import com.teadelivery.ordercatalog.search.repository.SearchVendorRepository;
import com.teadelivery.ordercatalog.search.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Recommendation Service Implementation
 * 
 * Provides personalized vendor and item recommendations
 * 
 * TODO: Enhance with actual user order history and ML-based recommendations (Phase 2)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    
    private final SearchVendorRepository vendorRepository;
    private final SearchMenuItemRepository menuItemRepository;
    private final SearchMapper mapper;
    
    @Override
    public RecommendationResponse getRecommendations(
            UUID userId,
            Double latitude,
            Double longitude,
            Integer radiusKm
    ) {
        log.info("Getting recommendations for userId: {}, location: ({}, {})", userId, latitude, longitude);
        
        int radiusMeters = radiusKm * 1000;
        
        // Section 1: Recommended Vendors (nearby, highly rated)
        List<SearchVendor> recommendedVendors = vendorRepository.findNearbyVendors(
                latitude,
                longitude,
                radiusMeters,
                null,
                PageRequest.of(0, 10)
        );
        
        // Section 2: Recommended Items (popular in area)
        List<SearchMenuItem> recommendedItems = menuItemRepository.findPopularItemsInArea(
                latitude,
                longitude,
                radiusMeters,
                10
        );
        
        // Section 3: Frequently Ordered (placeholder - requires user order history)
        // TODO: Query actual user order history from orders table
        List<SearchMenuItem> frequentlyOrdered = Collections.emptyList();
        
        // Section 4: Time-based Recommendations
        List<SearchMenuItem> timeBasedItems = getTimeBasedRecommendations(
                latitude,
                longitude,
                radiusMeters
        );
        
        // Build context message
        String context = buildRecommendationContext(userId);
        
        return RecommendationResponse.builder()
                .recommendedVendors(mapper.toVendorSearchResults(recommendedVendors))
                .recommendedItems(mapper.toMenuItemSearchResults(recommendedItems))
                .frequentlyOrdered(mapper.toMenuItemSearchResults(frequentlyOrdered))
                .timeBasedRecommendations(mapper.toMenuItemSearchResults(timeBasedItems))
                .recommendationContext(context)
                .build();
    }
    
    /**
     * Get time-based recommendations based on current time
     * Breakfast: 6am-11am, Lunch: 11am-3pm, Snacks: 3pm-6pm, Dinner: 6pm-11pm
     */
    private List<SearchMenuItem> getTimeBasedRecommendations(
            Double latitude,
            Double longitude,
            Integer radiusMeters
    ) {
        // TODO: Filter by category based on time of day
        // For now, return popular items
        return menuItemRepository.findPopularItemsInArea(latitude, longitude, radiusMeters, 5);
    }
    
    /**
     * Get time category based on current time
     */
    private String getTimeCategory(LocalTime time) {
        int hour = time.getHour();
        if (hour >= 6 && hour < 11) {
            return "Breakfast";
        } else if (hour >= 11 && hour < 15) {
            return "Lunch";
        } else if (hour >= 15 && hour < 18) {
            return "Snacks";
        } else {
            return "Dinner";
        }
    }
    
    /**
     * Build recommendation context message
     */
    private String buildRecommendationContext(UUID userId) {
        LocalTime now = LocalTime.now();
        String timeOfDay = getTimeCategory(now).toLowerCase();
        
        if (userId != null) {
            return String.format("Based on your preferences and popular %s items nearby", timeOfDay);
        } else {
            return String.format("Popular %s items in your area", timeOfDay);
        }
    }
}

