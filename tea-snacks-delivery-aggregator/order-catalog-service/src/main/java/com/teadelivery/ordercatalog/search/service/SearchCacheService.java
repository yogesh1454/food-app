package com.teadelivery.ordercatalog.search.service;

import java.util.function.Supplier;

/**
 * Search Cache Service
 * 
 * Tile-based regional caching strategy for geospatial queries
 * Uses 2km x 2km tiles to reduce database hits by 80%+
 */
public interface SearchCacheService {
    
    /**
     * Generate geo-based cache key for tile
     * Format: search:geo:{city}:{latTile}:{lonTile}
     * 
     * @param latitude User latitude
     * @param longitude User longitude
     * @param city City name
     * @return Cache key
     */
    String generateGeoCacheKey(Double latitude, Double longitude, String city);
    
    /**
     * Get or compute cached data with tile-based key
     * 
     * @param latitude User latitude
     * @param longitude User longitude
     * @param city City name
     * @param ttlSeconds Cache TTL in seconds
     * @param supplier Function to compute data if cache miss
     * @param <T> Result type
     * @return Cached or computed result
     */
    <T> T getOrCompute(
            Double latitude,
            Double longitude,
            String city,
            Integer ttlSeconds,
            Supplier<T> supplier
    );
    
    /**
     * Get cached data by key
     * 
     * @param key Cache key
     * @param clazz Result class
     * @param <T> Result type
     * @return Cached data or null
     */
    <T> T get(String key, Class<T> clazz);
    
    /**
     * Put data in cache
     * 
     * @param key Cache key
     * @param value Data to cache
     * @param ttlSeconds TTL in seconds
     */
    void put(String key, Object value, Integer ttlSeconds);
    
    /**
     * Invalidate cache for specific city (all tiles)
     * 
     * @param city City name
     * @return Number of keys deleted
     */
    Long invalidateCity(String city);
    
    /**
     * Invalidate specific tile
     * 
     * @param latitude Latitude
     * @param longitude Longitude
     * @param city City
     */
    void invalidateTile(Double latitude, Double longitude, String city);
    
    /**
     * Invalidate by pattern
     * 
     * @param pattern Key pattern (e.g., "search:*")
     * @return Number of keys deleted
     */
    Long invalidateByPattern(String pattern);
}

