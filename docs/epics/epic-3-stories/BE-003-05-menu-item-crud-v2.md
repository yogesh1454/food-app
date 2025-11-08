# BE-003-05: Menu Item CRUD Operations (Branch-Specific)

**Story ID:** BE-003-05  
**Story Points:** 8  
**Priority:** Critical (P0)  
**Sprint:** 6  
**Epic:** BE-003  
**Dependencies:** BE-003-04 (Operating Hours)

---

## 📖 User Story

**As a** branch manager  
**I want** to create, update, and delete menu items for my branch  
**So that** I can manage my branch's product catalog independently

---

## ✅ Acceptance Criteria

1. **Create Menu Item**
   - [ ] Add new menu items with name, description, price, category
   - [ ] Image upload support for menu item photos (primary + gallery)
   - [ ] Preparation time in minutes can be specified
   - [ ] Item is available by default (is_available = true)
   - [ ] Price must be positive decimal value
   - [ ] Category validation against predefined list
   - [ ] Metadata support for nutritional info, customizations, allergens

2. **Read Menu Items**
   - [ ] Get all menu items for a branch
   - [ ] Get single menu item by ID
   - [ ] Filter by category, availability, price range
   - [ ] Sort by name, price, category, popularity
   - [ ] Pagination support for large menus (50 items/page)
   - [ ] Soft-deleted items excluded from results

3. **Update Menu Item**
   - [ ] Update any field (name, price, description, category, etc.)
   - [ ] Price changes tracked for audit
   - [ ] Availability toggle (mark as out of stock)
   - [ ] Image can be updated/removed
   - [ ] Menu version incremented on any change
   - [ ] Updated timestamp tracked

4. **Delete Menu Item**
   - [ ] Soft delete (mark as deleted, don't remove from DB)
   - [ ] Cannot delete items in active orders
   - [ ] Confirmation required for deletion
   - [ ] Deleted items excluded from menu retrieval

5. **Validation & Business Rules**
   - [ ] Only branch owner/manager can modify their menu items
   - [ ] Price cannot be negative or zero
   - [ ] Name must be unique within branch's menu
   - [ ] Category must be from allowed list
   - [ ] Image URL validation
   - [ ] Preparation time between 1-240 minutes

6. **Event Publishing**
   - [ ] menu.item.created event on new item
   - [ ] menu.item.updated event on modification
   - [ ] menu.item.deleted event on deletion
   - [ ] menu.updated event to trigger cache invalidation

---

## 🔧 Technical Implementation

### **Service**
```java
@Service
@Slf4j
public class MenuService {
    
    private final MenuItemRepository menuItemRepository;
    private final BranchRepository branchRepository;
    private final MenuCacheService cacheService;
    private final MenuEventPublisher eventPublisher;
    private final S3Service s3Service;
    
    @Transactional
    public MenuItemResponse createMenuItem(UUID branchId, 
                                          MenuItemCreateRequest request,
                                          UUID requestingUserId) {
        log.info("Creating menu item for branch: {}", branchId);
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        // Authorization
        if (!branch.getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to modify this branch's menu");
        }
        
        // Validate category
        if (!MenuCategory.isValid(request.getCategory())) {
            throw new ValidationException("Invalid category: " + request.getCategory());
        }
        
        // Create menu item
        MenuItem menuItem = new MenuItem();
        menuItem.setBranch(branch);
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(request.getCategory());
        menuItem.setIsAvailable(true);
        menuItem.setPreparationTimeMinutes(request.getPreparationTimeMinutes());
        menuItem.setMetadata(request.getMetadata());
        menuItem.setTags(request.getTags());
        menuItem.setImages(new HashMap<>());
        menuItem.setIsDeleted(false);
        
        MenuItem savedItem = menuItemRepository.save(menuItem);
        
        // Increment branch menu version
        branch.incrementMenuVersion();
        branchRepository.save(branch);
        
        // Invalidate cache
        cacheService.evictBranchMenu(branchId);
        
        // Publish events
        eventPublisher.publishMenuItemCreated(savedItem);
        eventPublisher.publishMenuUpdated(branch);
        
        log.info("Menu item created: {} for branch: {}", savedItem.getMenuItemId(), branchId);
        return MenuMapper.toResponse(savedItem);
    }
    
    @Transactional(readOnly = true)
    public MenuResponse getBranchMenu(UUID branchId, 
                                     String category,
                                     Boolean availableOnly,
                                     Pageable pageable) {
        log.debug("Fetching menu for branch: {}", branchId);
        
        // Try cache first
        if (category == null && !Boolean.TRUE.equals(availableOnly)) {
            MenuResponse cached = cacheService.getBranchMenu(branchId);
            if (cached != null) {
                return cached;
            }
        }
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        Page<MenuItem> itemsPage;
        if (category != null) {
            itemsPage = menuItemRepository.findByBranchAndCategoryAndIsDeletedFalse(
                branch, category, pageable);
        } else if (Boolean.TRUE.equals(availableOnly)) {
            itemsPage = menuItemRepository.findByBranchAndIsAvailableTrueAndIsDeletedFalse(
                branch, pageable);
        } else {
            itemsPage = menuItemRepository.findByBranchAndIsDeletedFalse(branch, pageable);
        }
        
        // Group by category
        Map<String, List<MenuItem>> groupedItems = itemsPage.getContent().stream()
            .collect(Collectors.groupingBy(MenuItem::getCategory));
        
        List<MenuResponse.MenuCategoryGroup> categoryGroups = groupedItems.entrySet().stream()
            .map(entry -> MenuResponse.MenuCategoryGroup.builder()
                .category(entry.getKey())
                .items(entry.getValue().stream()
                    .map(MenuMapper::toResponse)
                    .collect(Collectors.toList()))
                .itemCount(entry.getValue().size())
                .build())
            .sorted(Comparator.comparing(MenuResponse.MenuCategoryGroup::getCategory))
            .collect(Collectors.toList());
        
        MenuResponse response = MenuResponse.builder()
            .branchId(branchId)
            .branchName(branch.getBranchName())
            .menuVersion(branch.getMenuVersion())
            .isOpen(branch.getIsOpen())
            .categories(categoryGroups)
            .totalItems((int) itemsPage.getTotalElements())
            .availableItems((int) menuItemRepository.countAvailableItems(branch))
            .build();
        
        // Cache full menu
        if (category == null && !Boolean.TRUE.equals(availableOnly)) {
            cacheService.cacheBranchMenu(branchId, response);
        }
        
        return response;
    }
    
    @Transactional
    public MenuItemResponse updateMenuItem(UUID menuItemId,
                                          MenuItemUpdateRequest request,
                                          UUID requestingUserId) {
        log.info("Updating menu item: {}", menuItemId);
        
        MenuItem menuItem = menuItemRepository.findByMenuItemIdAndIsDeletedFalse(menuItemId)
            .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));
        
        // Authorization
        if (!menuItem.getBranch().getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to modify this menu item");
        }
        
        // Track price change
        BigDecimal oldPrice = menuItem.getPrice();
        
        // Update fields
        if (request.getName() != null) {
            menuItem.setName(request.getName());
        }
        if (request.getDescription() != null) {
            menuItem.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            menuItem.setPrice(request.getPrice());
        }
        if (request.getCategory() != null) {
            if (!MenuCategory.isValid(request.getCategory())) {
                throw new ValidationException("Invalid category: " + request.getCategory());
            }
            menuItem.setCategory(request.getCategory());
        }
        if (request.getIsAvailable() != null) {
            menuItem.setIsAvailable(request.getIsAvailable());
        }
        if (request.getPreparationTimeMinutes() != null) {
            menuItem.setPreparationTimeMinutes(request.getPreparationTimeMinutes());
        }
        if (request.getMetadata() != null) {
            menuItem.setMetadata(request.getMetadata());
        }
        if (request.getTags() != null) {
            menuItem.setTags(request.getTags());
        }
        
        MenuItem updatedItem = menuItemRepository.save(menuItem);
        
        // Increment branch menu version
        VendorBranch branch = menuItem.getBranch();
        branch.incrementMenuVersion();
        branchRepository.save(branch);
        
        // Invalidate cache
        cacheService.evictBranchMenu(branch.getBranchId());
        
        // Publish events
        eventPublisher.publishMenuItemUpdated(updatedItem, oldPrice);
        eventPublisher.publishMenuUpdated(branch);
        
        log.info("Menu item updated: {}", menuItemId);
        return MenuMapper.toResponse(updatedItem);
    }
    
    @Transactional
    public void deleteMenuItem(UUID menuItemId, UUID requestingUserId) {
        log.info("Deleting menu item: {}", menuItemId);
        
        MenuItem menuItem = menuItemRepository.findByMenuItemIdAndIsDeletedFalse(menuItemId)
            .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));
        
        // Authorization
        if (!menuItem.getBranch().getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to delete this menu item");
        }
        
        // Soft delete
        menuItem.setIsDeleted(true);
        menuItem.setIsAvailable(false);
        menuItemRepository.save(menuItem);
        
        // Increment branch menu version
        VendorBranch branch = menuItem.getBranch();
        branch.incrementMenuVersion();
        branchRepository.save(branch);
        
        // Invalidate cache
        cacheService.evictBranchMenu(branch.getBranchId());
        
        // Publish events
        eventPublisher.publishMenuItemDeleted(menuItem);
        eventPublisher.publishMenuUpdated(branch);
        
        log.info("Menu item deleted: {}", menuItemId);
    }
    
    @Transactional
    public String uploadMenuItemImage(UUID menuItemId, 
                                     String imageType,
                                     MultipartFile file,
                                     UUID requestingUserId) {
        MenuItem menuItem = menuItemRepository.findByMenuItemIdAndIsDeletedFalse(menuItemId)
            .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));
        
        if (!menuItem.getBranch().getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized");
        }
        
        String imageUrl = s3Service.uploadFile(
            file, "menu-items/" + menuItemId + "/" + imageType);
        
        Map<String, Object> images = menuItem.getImages();
        
        if (imageType.equals("gallery")) {
            List<String> gallery = (List<String>) images.getOrDefault("gallery", new ArrayList<>());
            gallery.add(imageUrl);
            images.put("gallery", gallery);
        } else {
            images.put("primary", imageUrl);
        }
        
        menuItem.setImages(images);
        menuItemRepository.save(menuItem);
        
        // Invalidate cache
        cacheService.evictBranchMenu(menuItem.getBranch().getBranchId());
        
        return imageUrl;
    }
}
```

---

## 🧪 Testing Requirements

- [ ] Create menu item with valid data
- [ ] Duplicate name validation
- [ ] Category validation
- [ ] Update menu item fields
- [ ] Price change tracking
- [ ] Soft delete working
- [ ] Image upload to S3
- [ ] Menu version increment
- [ ] Cache invalidation
- [ ] Kafka events published

---

## 📋 Definition of Done

- [ ] MenuService implemented
- [ ] All CRUD endpoints working
- [ ] Image upload to S3
- [ ] Validation complete
- [ ] Menu version tracking
- [ ] Kafka events published
- [ ] Unit tests passing (>80%)
- [ ] Integration tests passing
- [ ] Code reviewed

---

## 📚 References

- [REST API Spec](/docs/architecture/8-rest-api-spec.md)
- [Coding Standards](/docs/architecture/13-coding-standards.md)
