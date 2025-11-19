# BE-003-06: Menu Versioning & Cache Management

**Story ID:** BE-003-06  
**Story Points:** 5  
**Priority:** High  
**Sprint:** 6  
**Epic:** BE-003  
**Dependencies:** BE-003-05 (Menu Item CRUD)

---

## 📖 User Story

**As a** system  
**I want** to implement menu versioning and intelligent cache management  
**So that** customers always see the latest menu while maintaining high performance

---

## ✅ Acceptance Criteria

1. **Menu Versioning**
   - [ ] menu_version increments on any menu change (add/update/delete item)
   - [ ] Version included in cache keys for invalidation
   - [ ] Version exposed in API responses
   - [ ] Version tracked in Kafka events

2. **Cache Strategy**
   - [ ] Full menu cached in Redis (TTL: 1 hour)
   - [ ] Cache key includes branch ID and menu version
   - [ ] Cache invalidated on menu changes
   - [ ] Popular items cached separately (TTL: 15 mins)
   - [ ] Cache hit rate > 80%

3. **Cache Invalidation**
   - [ ] Automatic invalidation on menu item create/update/delete
   - [ ] Manual cache clear endpoint for admins
   - [ ] Branch-specific invalidation (don't clear all caches)
   - [ ] Event-driven invalidation via Kafka

4. **Performance Optimization**
   - [ ] Menu retrieval < 50ms (cached)
   - [ ] Menu retrieval < 200ms (uncached)
   - [ ] Lazy loading for menu item images
   - [ ] Pagination for large menus (50 items/page)

---

## 🔧 Technical Implementation

### **Cache Key Strategy**
```
branch:{branchId}:menu:v{menuVersion}
branch:{branchId}:popular-items
menu-item:{itemId}
```

### **Cache Service**
```java
@Service
@Slf4j
public class MenuCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String BRANCH_MENU_KEY = "branch:%s:menu:v%d";
    private static final String POPULAR_ITEMS_KEY = "branch:%s:popular-items";
    private static final Duration MENU_TTL = Duration.ofHours(1);
    private static final Duration POPULAR_ITEMS_TTL = Duration.ofMinutes(15);
    
    public void cacheBranchMenu(UUID branchId, Integer version, MenuResponse menu) {
        String key = String.format(BRANCH_MENU_KEY, branchId, version);
        redisTemplate.opsForValue().set(key, menu, MENU_TTL);
        log.debug("Cached branch menu: {} v{}", branchId, version);
    }
    
    public MenuResponse getBranchMenu(UUID branchId, Integer version) {
        String key = String.format(BRANCH_MENU_KEY, branchId, version);
        return (MenuResponse) redisTemplate.opsForValue().get(key);
    }
    
    public void evictBranchMenu(UUID branchId) {
        String pattern = String.format("branch:%s:menu:v*", branchId);
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Evicted all menu versions for branch: {}", branchId);
        }
    }
    
    public void cachePopularItems(UUID branchId, List<MenuItemResponse> items) {
        String key = String.format(POPULAR_ITEMS_KEY, branchId);
        redisTemplate.opsForValue().set(key, items, POPULAR_ITEMS_TTL);
        log.debug("Cached popular items for branch: {}", branchId);
    }
    
    public List<MenuItemResponse> getPopularItems(UUID branchId) {
        String key = String.format(POPULAR_ITEMS_KEY, branchId);
        return (List<MenuItemResponse>) redisTemplate.opsForValue().get(key);
    }
}
```

### **Popular Items Service**
```java
@Service
@Slf4j
public class PopularItemsService {
    
    private final MenuItemRepository menuItemRepository;
    private final MenuCacheService cacheService;
    
    public List<MenuItemResponse> getPopularItems(UUID branchId, int limit) {
        // Try cache first
        List<MenuItemResponse> cached = cacheService.getPopularItems(branchId);
        if (cached != null) {
            return cached;
        }
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        // Query based on order frequency
        List<MenuItem> items = menuItemRepository.findPopularItems(
            branch, PageRequest.of(0, limit));
        
        List<MenuItemResponse> response = items.stream()
            .map(MenuMapper::toResponse)
            .collect(Collectors.toList());
        
        // Cache for 15 minutes
        cacheService.cachePopularItems(branchId, response);
        
        return response;
    }
}
```

### **Menu Version Aspect**
```java
@Aspect
@Component
@Slf4j
public class MenuVersionAspect {
    
    private final BranchRepository branchRepository;
    
    @AfterReturning("execution(* com.teadelivery.ordercatalog.menu.service.MenuService.create*(..))")
    public void incrementVersionAfterCreate(JoinPoint joinPoint) {
        incrementMenuVersion(joinPoint);
    }
    
    @AfterReturning("execution(* com.teadelivery.ordercatalog.menu.service.MenuService.update*(..))")
    public void incrementVersionAfterUpdate(JoinPoint joinPoint) {
        incrementMenuVersion(joinPoint);
    }
    
    @AfterReturning("execution(* com.teadelivery.ordercatalog.menu.service.MenuService.delete*(..))")
    public void incrementVersionAfterDelete(JoinPoint joinPoint) {
        incrementMenuVersion(joinPoint);
    }
    
    private void incrementMenuVersion(JoinPoint joinPoint) {
        // Extract branchId from method arguments and increment version
        // This is handled in the service layer directly
    }
}
```

---

## 🧪 Testing Requirements

- [ ] Cache hit/miss scenarios
- [ ] Version increment on menu changes
- [ ] Cache invalidation working
- [ ] Popular items caching
- [ ] Performance benchmarks
- [ ] Cache hit rate > 80%

---

## 📋 Definition of Done

- [ ] Menu versioning implemented
- [ ] Cache service with version support
- [ ] Cache invalidation working
- [ ] Popular items caching
- [ ] Performance tests passing
- [ ] Cache hit rate > 80%
- [ ] Code reviewed

---

## 📚 References

- [Caching Strategy](/docs/architecture/2-high-level-architecture.md)
