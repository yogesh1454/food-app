package com.teadelivery.ordercatalog.search.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teadelivery.ordercatalog.search.service.SearchCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Search Cache Service Implementation
 * 
 * Implements tile-based regional caching using Redis
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchCacheServiceImpl implements SearchCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${search.cache.tile-size-km:2}")
    private Integer tileSizeKm;
    
    @Override
    public String generateGeoCacheKey(Double latitude, Double longitude, String city) {
        int latTile = (int) Math.floor(latitude / tileSizeKm);
        int lonTile = (int) Math.floor(longitude / tileSizeKm);
        return String.format("search:geo:%s:%d:%d", city, latTile, lonTile);
    }
    
    @Override
    public <T> T getOrCompute(
            Double latitude,
            Double longitude,
            String city,
            Integer ttlSeconds,
            Supplier<T> supplier
    ) {
        String cacheKey = generateGeoCacheKey(latitude, longitude, city);
        
        // Try to get from cache
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Cache HIT for key: {}", cacheKey);
            try {
                @SuppressWarnings("unchecked")
                T result = (T) cached;
                return result;
            } catch (ClassCastException e) {
                log.warn("Cache value type mismatch for key: {}", cacheKey, e);
                // Fall through to recompute
            }
        }
        
        log.debug("Cache MISS for key: {}", cacheKey);
        
        // Compute value
        T result = supplier.get();
        
        // Store in cache
        if (result != null) {
            put(cacheKey, result, ttlSeconds);
        }
        
        return result;
    }
    
    @Override
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            
            if (clazz.isInstance(value)) {
                return clazz.cast(value);
            }
            
            // Try to convert using ObjectMapper if not direct instance
            return objectMapper.convertValue(value, clazz);
        } catch (Exception e) {
            log.error("Error getting value from cache for key: {}", key, e);
            return null;
        }
    }
    
    @Override
    public void put(String key, Object value, Integer ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
            log.debug("Cached value for key: {} with TTL: {}s", key, ttlSeconds);
        } catch (Exception e) {
            log.error("Error caching value for key: {}", key, e);
        }
    }
    
    @Override
    public Long invalidateCity(String city) {
        String pattern = String.format("search:geo:%s:*", city);
        return invalidateByPattern(pattern);
    }
    
    @Override
    public void invalidateTile(Double latitude, Double longitude, String city) {
        String cacheKey = generateGeoCacheKey(latitude, longitude, city);
        Boolean deleted = redisTemplate.delete(cacheKey);
        log.info("Invalidated tile cache: {} - {}", cacheKey, deleted);
    }
    
    @Override
    public Long invalidateByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                log.info("Invalidated {} keys matching pattern: {}", deleted, pattern);
                return deleted != null ? deleted : 0L;
            }
            return 0L;
        } catch (Exception e) {
            log.error("Error invalidating keys by pattern: {}", pattern, e);
            return 0L;
        }
    }
}


