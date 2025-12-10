package com.teadelivery.ordercatalog.search.service.impl;

import com.teadelivery.ordercatalog.search.dto.*;
import com.teadelivery.ordercatalog.search.mapper.SearchMapper;
import com.teadelivery.ordercatalog.search.model.SearchMenuItem;
import com.teadelivery.ordercatalog.search.model.SearchVendor;
import com.teadelivery.ordercatalog.search.repository.SearchMenuItemRepository;
import com.teadelivery.ordercatalog.search.repository.SearchVendorRepository;
import com.teadelivery.ordercatalog.search.service.DiscoveryFeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Discovery Feed Service Implementation
 * 
 * Provides personalized discovery feed with 4 sections:
 * 1. Nearby Vendors
 * 2. Popular Items (last 30 days)
 * 3. Recommended Items (personalized)
 * 4. Top Ordered Items (trending, last 7 days)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiscoveryFeedServiceImpl implements DiscoveryFeedService {
    
    private final SearchVendorRepository vendorRepository;
    private final SearchMenuItemRepository menuItemRepository;
    private final SearchMapper mapper;
    // TODO: Add SearchCacheService for caching in future optimization
    
    @Value("${search.geospatial.default-radius-km:5}")
    private Integer defaultRadiusKm;
    
    @Value("${search.cache.ttl.feed-cache:600}")
    private Integer feedCacheTtl;

    @Value("${features.search.bypass-geospatial:false}")
    private Boolean bypassGeospatial;
    
    @Override
    public DiscoveryFeedResponse getDiscoveryFeed(
            Double latitude,
            Double longitude,
            Integer radiusKm,
            UUID userId,
            Integer page,
            Integer size
    ) {
        log.info("Getting discovery feed for location: ({}, {}), radius: {}km, userId: {}, bypassGeo={}", 
                latitude, longitude, radiusKm, userId, bypassGeospatial);
        
        // Calculate radius in meters for PostGIS - use very large radius if bypassing geospatial
        int radiusMeters = Boolean.TRUE.equals(bypassGeospatial)
                                ? 10_000_000 // ~10,000km - effectively worldwide
                                : (radiusKm != null ? radiusKm : defaultRadiusKm) * 1000;
        log.info("Radius in meters: {}", radiusMeters);
        
        // Section 1: Nearby Vendors (with blended ranking)
        List<SearchVendor> nearbyVendors = vendorRepository.findNearbyVendors(
                latitude,
                longitude,
                radiusMeters,
                null, // city filter (optional)
                PageRequest.of(page != null ? page : 0, size != null ? size : 20)
        );
        
        // Section 2: Popular Items (last 30 days in area)
        List<SearchMenuItem> popularItems = menuItemRepository.findPopularItemsInArea(
                latitude,
                longitude,
                radiusMeters,
                10 // Limit to top 10
        );
        
        // Section 3: Recommended Items (personalized)
        // TODO: Implement actual personalization based on user order history
        List<SearchMenuItem> recommendedItems = Collections.emptyList();
        if (userId != null) {
            // Placeholder: For now, return empty list
            // Future: Query user preferences and order history
            recommendedItems = Collections.emptyList();
        }
        
        // Section 4: Top Ordered Items (trending, last 7 days)
        List<SearchMenuItem> trendingItems = menuItemRepository.findTrendingItems(
                latitude,
                longitude,
                radiusMeters,
                10 // Limit to top 10
        );
        
        // Convert to DTOs
        List<VendorSearchResult> nearbyVendorDtos = mapper.toVendorSearchResults(nearbyVendors);
        List<MenuItemSearchResult> popularItemDtos = mapper.toMenuItemSearchResults(popularItems);
        List<MenuItemSearchResult> recommendedItemDtos = mapper.toMenuItemSearchResults(recommendedItems);
        List<MenuItemSearchResult> trendingItemDtos = mapper.toMenuItemSearchResults(trendingItems);
        
        // Build metadata
        FeedMetadata metadata = FeedMetadata.builder()
                .totalVendors(nearbyVendorDtos.size())
                .cacheHit(false)
                .cacheUntil(Instant.now().plusSeconds(feedCacheTtl))
                .rankingVersion("v2-blended")
                .build();
        
        // Build response
        return DiscoveryFeedResponse.builder()
                .nearbyVendors(nearbyVendorDtos)
                .popularItems(popularItemDtos)
                .recommendedItems(recommendedItemDtos)
                .topOrderedItems(trendingItemDtos)
                .searchSuggestions(getSearchSuggestions()) // TODO: Get from popular queries
                .metadata(metadata)
                .build();
    }
    
    /**
     * Get search suggestions (placeholder)
     * TODO: Get from search_popular_queries table
     */
    private List<String> getSearchSuggestions() {
        return List.of("Masala Chai", "Samosa", "Filter Coffee", "Vada Pav", "Dosa");
    }
}

