package com.teadelivery.ordercatalog.menu.service;

import com.teadelivery.ordercatalog.menu.dto.MenuItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String BRANCH_MENU_KEY = "branch:%s:menu:v%d";
    private static final String POPULAR_ITEMS_KEY = "branch:%s:popular-items";
    private static final Duration MENU_TTL = Duration.ofHours(1);
    private static final Duration POPULAR_ITEMS_TTL = Duration.ofMinutes(15);
    
    public void cacheBranchMenu(Long branchId, Integer version, List<MenuItemResponse> menu) {
        try {
            String key = String.format(BRANCH_MENU_KEY, branchId, version);
            redisTemplate.opsForValue().set(key, menu, MENU_TTL);
            log.debug("Cached branch menu: {} v{}", branchId, version);
        } catch (Exception e) {
            log.warn("Failed to cache branch menu: {}", e.getMessage());
        }
    }
    
    public List<MenuItemResponse> getBranchMenu(Long branchId, Integer version) {
        try {
            String key = String.format(BRANCH_MENU_KEY, branchId, version);
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof List) {
                log.debug("Cache hit for branch menu: {} v{}", branchId, version);
                return (List<MenuItemResponse>) cached;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached menu: {}", e.getMessage());
        }
        return null;
    }
    
    public void evictBranchMenu(Long branchId) {
        try {
            String pattern = String.format("branch:%s:menu:v*", branchId);
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Evicted all menu versions for branch: {}", branchId);
            }
        } catch (Exception e) {
            log.warn("Failed to evict cache: {}", e.getMessage());
        }
    }
    
    public void cachePopularItems(Long branchId, List<MenuItemResponse> items) {
        try {
            String key = String.format(POPULAR_ITEMS_KEY, branchId);
            redisTemplate.opsForValue().set(key, items, POPULAR_ITEMS_TTL);
            log.debug("Cached popular items for branch: {}", branchId);
        } catch (Exception e) {
            log.warn("Failed to cache popular items: {}", e.getMessage());
        }
    }
    
    public List<MenuItemResponse> getPopularItems(Long branchId) {
        try {
            String key = String.format(POPULAR_ITEMS_KEY, branchId);
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof List) {
                log.debug("Cache hit for popular items: {}", branchId);
                return (List<MenuItemResponse>) cached;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve popular items: {}", e.getMessage());
        }
        return null;
    }
    
    public void evictPopularItems(Long branchId) {
        try {
            String key = String.format(POPULAR_ITEMS_KEY, branchId);
            redisTemplate.delete(key);
            log.debug("Evicted popular items for branch: {}", branchId);
        } catch (Exception e) {
            log.warn("Failed to evict popular items: {}", e.getMessage());
        }
    }
}
