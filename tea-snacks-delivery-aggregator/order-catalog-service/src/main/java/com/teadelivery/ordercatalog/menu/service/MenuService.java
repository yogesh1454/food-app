package com.teadelivery.ordercatalog.menu.service;

import com.teadelivery.ordercatalog.common.exception.BranchNotFoundException;
import com.teadelivery.ordercatalog.common.exception.MenuItemNotFoundException;
import com.teadelivery.ordercatalog.common.exception.UnauthorizedException;
import com.teadelivery.ordercatalog.common.service.S3StorageService;
import com.teadelivery.ordercatalog.menu.dto.MenuItemCreateRequest;
import com.teadelivery.ordercatalog.menu.dto.MenuItemResponse;
import com.teadelivery.ordercatalog.menu.dto.MenuItemUpdateRequest;
import com.teadelivery.ordercatalog.menu.mapper.MenuMapper;
import com.teadelivery.ordercatalog.menu.model.MenuItem;
import com.teadelivery.ordercatalog.menu.repository.MenuItemRepository;
import com.teadelivery.ordercatalog.vendor.dto.ImageUploadResponse;
import com.teadelivery.ordercatalog.vendor.model.VendorBranch;
import com.teadelivery.ordercatalog.vendor.repository.VendorBranchRepository;
import com.teadelivery.ordercatalog.search.sync.SearchIndexEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final VendorBranchRepository branchRepository;
    private final MenuCacheService cacheService;
    private final S3StorageService s3StorageService;

    @Autowired(required = false)
    private SearchIndexEventPublisher searchEventPublisher;

    @Transactional
    public MenuItemResponse createMenuItem(Long branchId, MenuItemCreateRequest request, UUID requestingUserId) {
        log.info("Creating menu item for branch: {}", branchId);

        VendorBranch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found"));

        if (!branch.getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to modify this branch's menu");
        }

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
        branch.setMenuVersion(branch.getMenuVersion() + 1);
        branchRepository.save(branch);

        // Publish search index event
        if (searchEventPublisher != null) {
            searchEventPublisher.publishMenuItemCreated(savedItem);
        }

        log.info("Menu item created: {} for branch: {}", savedItem.getMenuItemId(), branchId);
        return MenuMapper.toResponse(savedItem);
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItem(Long menuItemId) {
        log.info("Fetching menu item: {}", menuItemId);

        MenuItem menuItem = menuItemRepository.findByMenuItemIdAndIsDeletedFalse(menuItemId)
                .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));

        return MenuMapper.toResponse(menuItem);
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getBranchMenu(Long branchId, String category, Pageable pageable) {
        log.info("Fetching menu for branch: {}", branchId);

        VendorBranch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found"));

        Page<MenuItem> itemsPage;
        if (category != null) {
            itemsPage = menuItemRepository.findByBranchAndCategoryAndIsDeletedFalse(branch, category, pageable);
        } else {
            itemsPage = menuItemRepository.findByBranchAndIsDeletedFalse(branch, pageable);
        }

        return itemsPage.getContent().stream()
                .map(MenuMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long menuItemId, MenuItemUpdateRequest request, UUID requestingUserId) {
        log.info("Updating menu item: {}", menuItemId);

        MenuItem menuItem = menuItemRepository.findByMenuItemIdAndIsDeletedFalse(menuItemId)
                .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));

        if (!menuItem.getBranch().getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to modify this menu item");
        }

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
        branch.setMenuVersion(branch.getMenuVersion() + 1);
        branchRepository.save(branch);

        // Invalidate cache
        cacheService.evictBranchMenu(branch.getBranchId());
        cacheService.evictPopularItems(branch.getBranchId());

        // Publish search index event
        if (searchEventPublisher != null) {
            searchEventPublisher.publishMenuItemUpdated(updatedItem);
        }

        log.info("Menu item updated: {}", menuItemId);
        return MenuMapper.toResponse(updatedItem);
    }

    @Transactional
    public void deleteMenuItem(Long menuItemId, UUID requestingUserId) {
        log.info("Deleting menu item: {}", menuItemId);

        MenuItem menuItem = menuItemRepository.findByMenuItemIdAndIsDeletedFalse(menuItemId)
                .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));

        if (!menuItem.getBranch().getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to delete this menu item");
        }

        // Soft delete
        menuItem.setIsDeleted(true);
        menuItem.setIsAvailable(false);
        menuItemRepository.save(menuItem);

        // Increment branch menu version
        VendorBranch branch = menuItem.getBranch();
        branch.setMenuVersion(branch.getMenuVersion() + 1);
        branchRepository.save(branch);

        // Invalidate cache
        cacheService.evictBranchMenu(branch.getBranchId());
        cacheService.evictPopularItems(branch.getBranchId());

        // Publish search index event
        if (searchEventPublisher != null) {
            searchEventPublisher.publishMenuItemDeleted(menuItemId);
        }

        log.info("Menu item deleted: {}", menuItemId);
    }

    // ========== Inventory/Stock Management ==========

    /**
     * Check if menu item has sufficient stock
     * 
     * @param menuItemId        Menu item ID
     * @param requestedQuantity Quantity requested
     * @return true if sufficient stock available
     */
    @Transactional(readOnly = true)
    public boolean checkMenuItemStock(Long menuItemId, int requestedQuantity) {
        log.debug("Checking stock for menu item: {}, quantity: {}", menuItemId, requestedQuantity);

        MenuItem menuItem = menuItemRepository.findByMenuItemIdAndIsDeletedFalse(menuItemId)
                .orElse(null);

        if (menuItem == null) {
            log.warn("Menu item not found: {}", menuItemId);
            return false;
        }

        // Check if item is available
        if (!menuItem.getIsAvailable()) {
            log.warn("Menu item not available: {}", menuItemId);
            return false;
        }

        // TODO: Add actual stock tracking in MenuItem model
        // For now, assume all available items are in stock
        log.debug("Stock check passed for menu item: {}", menuItemId);
        return true;
    }

    /**
     * Check if multiple menu items have sufficient stock
     * 
     * @param itemQuantities Map of menuItemId -> quantity
     * @return true if all items have sufficient stock
     */
    @Transactional(readOnly = true)
    public boolean checkMultipleItemsStock(java.util.Map<Long, Integer> itemQuantities) {
        log.debug("Checking stock for {} items", itemQuantities.size());

        for (java.util.Map.Entry<Long, Integer> entry : itemQuantities.entrySet()) {
            if (!checkMenuItemStock(entry.getKey(), entry.getValue())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Reserve stock for menu items (for order placement)
     * 
     * @param itemQuantities Map of menuItemId -> quantity
     * @return true if reservation successful
     */
    @Transactional
    public boolean reserveStock(java.util.Map<Long, Integer> itemQuantities) {
        log.info("Reserving stock for {} items", itemQuantities.size());

        // First check if all items are available
        if (!checkMultipleItemsStock(itemQuantities)) {
            log.warn("Stock check failed, cannot reserve");
            return false;
        }

        // TODO: Implement actual stock reservation logic
        // This would decrement available quantity in MenuItem model
        // For now, just return true if items are available

        log.info("Stock reserved successfully for {} items", itemQuantities.size());
        return true;
    }

    /**
     * Release reserved stock (on order cancellation/rejection)
     * 
     * @param itemQuantities Map of menuItemId -> quantity
     */
    @Transactional
    public void releaseStock(java.util.Map<Long, Integer> itemQuantities) {
        log.info("Releasing stock for {} items", itemQuantities.size());

        // TODO: Implement actual stock release logic
        // This would increment available quantity in MenuItem model

        log.info("Stock released successfully for {} items", itemQuantities.size());
    }

    // ==================== Phase 1: S3 Upload ====================

    /**
     * Upload menu item image to S3 (Phase 1).
     * Stores original in S3 and updates database with PENDING status.
     * Processing will be done asynchronously via SQS/Lambda.
     * 
     * For gallery images, the index is auto-incremented based on existing gallery
     * images.
     */
    @Transactional
    public ImageUploadResponse uploadMenuItemImageToS3(Long menuItemId, String imageType,
            MultipartFile file, UUID requestingUserId) {
        log.info("Uploading menu item image to S3: menuItemId={}, imageType={}, size={}",
                menuItemId, imageType, file.getSize());

        MenuItem menuItem = menuItemRepository.findByMenuItemIdAndIsDeletedFalse(menuItemId)
                .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found: " + menuItemId));

        // Authorization - check if user owns the branch
        if (!menuItem.getBranch().getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized to update this menu item");
        }

        // Auto-increment gallery index if imageType is "gallery"
        Integer galleryIndex = null;
        if ("gallery".equals(imageType)) {
            galleryIndex = menuItemRepository.findNextGalleryIndex(menuItemId);
            log.info("Auto-incremented gallery index for menuItemId={}: {}", menuItemId, galleryIndex);
        }

        // Upload to S3
        String s3Key = s3StorageService.uploadMenuItemImage(menuItemId, imageType, file, galleryIndex);

        // Build image URLs map with placeholder URLs
        // For gallery images, extract the imageType from S3 key to match processed
        // files (gallery_1, gallery_2)
        String placeholderImageType = imageType;
        if ("gallery".equals(imageType) && s3Key != null) {
            // Extract gallery_N from key like
            // "originals/menu-items/9/gallery_1_original.webp"
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(gallery_\\d+)_original");
            java.util.regex.Matcher matcher = pattern.matcher(s3Key);
            if (matcher.find()) {
                placeholderImageType = matcher.group(1); // e.g., "gallery_1"
            }
        }

        Map<String, Object> imageData = s3StorageService.buildImageUrlsMap(
                "menu-items", menuItemId.toString(),
                placeholderImageType,
                s3Key);

        // Update menu item images
        if (menuItem.getImages() == null) {
            menuItem.setImages(new HashMap<>());
        }

        // Store images directly with their keys (gallery_1, gallery_2, primary)
        menuItem.getImages().put(placeholderImageType, imageData);
        log.info("Stored image with key: {}", placeholderImageType);

        menuItemRepository.save(menuItem);

        log.info("Menu item image uploaded to S3: menuItemId={}, imageType={}, s3Key={}",
                menuItemId, imageType, s3Key);

        return ImageUploadResponse.accepted(menuItemId, "menu-item", imageType, s3Key);
    }
}
