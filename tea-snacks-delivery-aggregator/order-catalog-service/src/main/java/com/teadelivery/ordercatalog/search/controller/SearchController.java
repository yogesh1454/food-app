package com.teadelivery.ordercatalog.search.controller;

import com.teadelivery.ordercatalog.search.dto.*;
import com.teadelivery.ordercatalog.search.service.DiscoveryFeedService;
import com.teadelivery.ordercatalog.search.service.RecommendationService;
import com.teadelivery.ordercatalog.search.service.SearchAnalyticsService;
import com.teadelivery.ordercatalog.search.service.UnifiedSearchService;
import com.teadelivery.ordercatalog.search.service.VendorMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Search & Discovery REST Controller
 * 
 * Provides 4 mobile-optimized endpoints:
 * 1. Discovery Feed - Nearby vendors, popular items, recommendations, trending
 * 2. Unified Search - Full-text + fuzzy search with blended ranking
 * 3. Vendor Menu - Complete menu with categories and recommendations
 * 4. Recommendations - Personalized vendor and item recommendations
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Search & Discovery", description = "Search and discovery APIs with blended ranking and recommendations")
public class SearchController {
    
    // Service dependencies
    private final DiscoveryFeedService discoveryFeedService;
    private final UnifiedSearchService unifiedSearchService;
    private final VendorMenuService vendorMenuService;
    private final RecommendationService recommendationService;
    private final SearchAnalyticsService analyticsService;
    
    /**
     * API 1: Discovery Feed
     * 
     * Returns 4 sections:
     * - Nearby Vendors (location-based with blended ranking)
     * - Popular Items (last 30 days in area)
     * - Recommended Items (personalized based on user preferences)
     * - Top Ordered Items (trending, last 7 days)
     */
    @GetMapping("/feed")
    @Operation(
        summary = "Get personalized discovery feed",
        description = "Returns nearby vendors, popular items, personalized recommendations, and trending items based on user location and preferences",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Discovery feed retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DiscoveryFeedResponse.class),
                    examples = @ExampleObject(
                        name = "Discovery Feed Example",
                        value = """
                        {
                          "nearbyVendors": [
                            {
                              "branchId": 101,
                              "vendorId": 1,
                              "branchName": "Chai Express - MG Road",
                              "displayName": "Chai Express",
                              "cuisine": ["Tea", "Snacks"],
                              "rating": 4.5,
                              "totalRatings": 1250,
                              "deliveryTime": "20-25 min",
                              "distance": 1.2,
                              "distanceUnit": "km",
                              "deliveryFee": 20.00,
                              "minOrderValue": 50.00,
                              "isOpen": true,
                              "images": {
                                "primary": "https://cdn.foodapp.com/vendors/101/cover_thumbnail.jpg",
                                "cover": {
                                  "thumbnail": "https://cdn.foodapp.com/vendors/101/cover_thumbnail.jpg",
                                  "small": "https://cdn.foodapp.com/vendors/101/cover_small.jpg"
                                }
                              },
                              "tags": ["Fast Delivery", "Popular"],
                              "rankingScore": 0.87
                            }
                          ],
                          "popularItems": [],
                          "recommendedItems": [],
                          "topOrderedItems": [],
                          "searchSuggestions": ["Masala Chai", "Samosa", "Filter Coffee"],
                          "metadata": {
                            "totalVendors": 45,
                            "cacheHit": true,
                            "rankingVersion": "v2-blended"
                          }
                        }
                        """
                    )
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<DiscoveryFeedResponse> getDiscoveryFeed(
            @Parameter(description = "User latitude", required = true, example = "12.9716")
            @RequestParam @Min(-90) @Max(90) Double latitude,
            
            @Parameter(description = "User longitude", required = true, example = "77.5946")
            @RequestParam @Min(-180) @Max(180) Double longitude,
            
            @Parameter(description = "Search radius in km", example = "5")
            @RequestParam(required = false, defaultValue = "5") @Min(1) @Max(20) Integer radius,
            
            @Parameter(description = "User ID for personalization", example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam(required = false) UUID userId,
            
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer page,
            
            @Parameter(description = "Page size", example = "20")
            @RequestParam(required = false, defaultValue = "20") @Min(1) @Max(100) Integer size
    ) {
        log.info("Discovery feed request: lat={}, lon={}, radius={}, userId={}, page={}, size={}", 
                latitude, longitude, radius, userId, page, size);
        
        long startTime = System.currentTimeMillis();
        
        // Call discovery feed service
        DiscoveryFeedResponse response = discoveryFeedService.getDiscoveryFeed(
                latitude,
                longitude,
                radius,
                userId,
                page,
                size
        );
        
        // Track analytics
        long responseTime = System.currentTimeMillis() - startTime;
        int totalResults = (response.getNearbyVendors() != null ? response.getNearbyVendors().size() : 0) +
                          (response.getPopularItems() != null ? response.getPopularItems().size() : 0);
        analyticsService.trackFeedView(userId, latitude, longitude, totalResults, responseTime, 
                response.getMetadata() != null ? response.getMetadata().getCacheHit() : false);
        
        // Cache-Control: 10 minutes, stale-while-revalidate=30 minutes
        CacheControl cacheControl = CacheControl.maxAge(10, TimeUnit.MINUTES)
                .cachePublic()
                .staleWhileRevalidate(30, TimeUnit.MINUTES);
        
        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .eTag(String.valueOf(response.hashCode()))
                .header("X-Cache-Hit", "false")
                .body(response);
    }
    
    /**
     * API 2: Unified Search
     * 
     * Hybrid search with:
     * - Full-text search (FTS) for exact matches
     * - Fuzzy search (pg_trgm) for typo tolerance
     * - Geospatial filtering
     * - Blended ranking algorithm
     */
    @GetMapping
    @Operation(
        summary = "Execute unified search",
        description = "Hybrid search with full-text search, fuzzy matching, and blended ranking. Returns both vendors and menu items sorted by relevance.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Search results retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SearchResponse.class),
                    examples = @ExampleObject(
                        name = "Search Response Example",
                        value = """
                        {
                          "query": "chai",
                          "type": "all",
                          "results": {
                            "vendors": [],
                            "items": []
                          },
                          "suggestions": ["chai latte", "masala chai"],
                          "pagination": {
                            "currentPage": 0,
                            "totalResults": 0,
                            "hasMore": false,
                            "pageSize": 20
                          },
                          "metadata": {
                            "searchTime": 45,
                            "cacheHit": false,
                            "rankingStrategy": "blended-v2",
                            "queryType": "hybrid-fts-fuzzy"
                          }
                        }
                        """
                    )
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<SearchResponse> search(
            @Parameter(description = "Search query", required = true, example = "chai")
            @RequestParam String q,
            
            @Parameter(description = "Search type", example = "all")
            @RequestParam(required = false, defaultValue = "all") String type,
            
            @Parameter(description = "User latitude", required = true, example = "12.9716")
            @RequestParam @Min(-90) @Max(90) Double latitude,
            
            @Parameter(description = "User longitude", required = true, example = "77.5946")
            @RequestParam @Min(-180) @Max(180) Double longitude,
            
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer page,
            
            @Parameter(description = "Page size", example = "20")
            @RequestParam(required = false, defaultValue = "20") @Min(1) @Max(100) Integer size,
            
            @Parameter(description = "City filter", example = "Bangalore")
            @RequestParam(required = false) String city,
            
            @Parameter(description = "Radius in km", example = "5")
            @RequestParam(required = false, defaultValue = "5") @Min(1) @Max(20) Integer radiusKm
    ) {
        log.info("Search request: query={}, type={}, lat={}, lon={}, page={}, size={}", 
                q, type, latitude, longitude, page, size);
        
        long startTime = System.currentTimeMillis();
        
        // Build search request
        SearchRequest searchRequest = SearchRequest.builder()
                .query(q)
                .type(type)
                .latitude(latitude)
                .longitude(longitude)
                .page(page)
                .size(size)
                .city(city)
                .radiusKm(radiusKm)
                .build();
        
        // Call unified search service
        SearchResponse response = unifiedSearchService.search(searchRequest);
        
        // Track analytics (async)
        long responseTime = System.currentTimeMillis() - startTime;
        analyticsService.trackSearch(searchRequest, response, responseTime, 
                response.getMetadata() != null ? response.getMetadata().getCacheHit() : false);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API 3: Vendor Menu
     * 
     * Returns complete vendor menu with:
     * - All menu categories and items
     * - Vendor information
     * - Personalized recommendations
     * - Popular items from this vendor
     */
    @GetMapping("/vendors/{branchId}/menu")
    @Operation(
        summary = "Get complete vendor menu",
        description = "Returns vendor menu with categories, items, recommendations, and popular items",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Vendor menu retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = VendorMenuResponse.class)
                )
            ),
            @ApiResponse(responseCode = "404", description = "Vendor not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<VendorMenuResponse> getVendorMenu(
            @Parameter(description = "Branch ID", required = true, example = "101")
            @PathVariable Long branchId,
            
            @Parameter(description = "User ID for personalization", example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam(required = false) UUID userId,
            
            @Parameter(description = "User latitude", example = "12.9716")
            @RequestParam(required = false) Double latitude,
            
            @Parameter(description = "User longitude", example = "77.5946")
            @RequestParam(required = false) Double longitude
    ) {
        log.info("Vendor menu request: branchId={}, userId={}", branchId, userId);
        
        // Call vendor menu service
        VendorMenuResponse response = vendorMenuService.getVendorMenu(
                branchId,
                userId,
                latitude,
                longitude
        );
        
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Cache-Control: 5 minutes
        CacheControl cacheControl = CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic();
        
        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .body(response);
    }
    
    /**
     * API 4: Personalized Recommendations
     * 
     * Returns personalized recommendations:
     * - Recommended vendors based on preferences
     * - Recommended items based on order history
     * - Frequently ordered items
     * - Time-based recommendations (breakfast, lunch, dinner)
     */
    @GetMapping("/recommendations")
    @Operation(
        summary = "Get personalized recommendations",
        description = "Returns personalized vendor and item recommendations based on user preferences, order history, and time of day",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Recommendations retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RecommendationResponse.class)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user ID required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @Parameter(description = "User ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam UUID userId,
            
            @Parameter(description = "User latitude", required = true, example = "12.9716")
            @RequestParam @Min(-90) @Max(90) Double latitude,
            
            @Parameter(description = "User longitude", required = true, example = "77.5946")
            @RequestParam @Min(-180) @Max(180) Double longitude,
            
            @Parameter(description = "Search radius in km", example = "5")
            @RequestParam(required = false, defaultValue = "5") @Min(1) @Max(20) Integer radiusKm
    ) {
        log.info("Recommendations request: userId={}, lat={}, lon={}, radiusKm={}", 
                userId, latitude, longitude, radiusKm);
        
        // Call recommendation service
        RecommendationResponse response = recommendationService.getRecommendations(
                userId,
                latitude,
                longitude,
                radiusKm
        );
        
        // Cache-Control: 15 minutes (personalized, so private cache)
        CacheControl cacheControl = CacheControl.maxAge(15, TimeUnit.MINUTES).cachePrivate();
        
        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .body(response);
    }
}

