package com.teadelivery.ordercatalog.common.service;

import com.teadelivery.ordercatalog.common.dto.ImageProcessingCallback;
import com.teadelivery.ordercatalog.common.exception.BranchNotFoundException;
import com.teadelivery.ordercatalog.common.exception.VendorNotFoundException;
import com.teadelivery.ordercatalog.menu.model.MenuItem;
import com.teadelivery.ordercatalog.menu.repository.MenuItemRepository;
import com.teadelivery.ordercatalog.search.sync.SearchIndexEventPublisher;
import com.teadelivery.ordercatalog.vendor.model.Vendor;
import com.teadelivery.ordercatalog.vendor.model.VendorBranch;
import com.teadelivery.ordercatalog.vendor.repository.VendorBranchRepository;
import com.teadelivery.ordercatalog.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service to handle image processing completion callbacks from Lambda.
 * Updates database with final CDN URLs and triggers search index sync.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImageProcessingService {

    private final VendorRepository vendorRepository;
    private final VendorBranchRepository branchRepository;
    private final MenuItemRepository menuItemRepository;
    private final SearchIndexEventPublisher searchEventPublisher;

    /**
     * Process callback from Lambda after image variants are generated.
     * Updates the entity's images field with READY status and CDN URLs.
     *
     * @param callback Callback data from Lambda
     */
    @Transactional
    public void handleProcessingComplete(ImageProcessingCallback callback) {
        log.info("Handling image processing callback: entityType={}, entityId={}, imageType={}, success={}",
                callback.getEntityType(), callback.getEntityId(), callback.getImageType(), callback.isSuccess());

        if (!callback.isSuccess()) {
            log.error("Image processing failed for {}/{}: {}",
                    callback.getEntityType(), callback.getEntityId(), callback.getError());
            markImageAsFailed(callback);
            return;
        }

        // Build the image data structure with CDN URLs
        Map<String, Object> imageData = buildImageData(callback);

        // Update the appropriate entity
        switch (callback.getEntityType().toLowerCase()) {
            case "vendors" -> updateVendorImage(callback, imageData);
            case "branches" -> updateBranchImage(callback, imageData);
            case "menu-items" -> updateMenuItemImage(callback, imageData);
            default -> log.warn("Unknown entity type: {}", callback.getEntityType());
        }
    }

    /**
     * Process image processing result from Lambda via SQS.
     * This is the new SQS-based approach replacing HTTP callbacks.
     *
     * @param result Image processing result from Lambda
     */
    @Transactional
    public void handleProcessingResult(com.teadelivery.ordercatalog.common.dto.ImageProcessingResult result) {
        log.info("Handling image processing result: entityType={}, entityId={}, imageType={}, success={}",
                result.getEntityType(), result.getEntityId(), result.getImageType(), result.isSuccess());

        if (!result.isSuccess()) {
            log.error("Image processing failed for {}/{}/{}",
                    result.getEntityType(), result.getEntityId(), result.getImageType());
            markImageAsFailedFromResult(result);
            return;
        }

        // Build the image data structure with CDN URLs
        Map<String, Object> imageData = buildImageDataFromResult(result);

        // Update the appropriate entity
        switch (result.getEntityType().toLowerCase()) {
            case "vendors" -> updateVendorImageFromResult(result, imageData);
            case "vendor-branches" -> updateBranchImageFromResult(result, imageData);
            case "menu-items" -> updateMenuItemImageFromResult(result, imageData);
            default -> log.warn("Unknown entity type: {}", result.getEntityType());
        }
    }

    /**
     * Update vendor image with processed URLs
     */
    private void updateVendorImage(ImageProcessingCallback callback, Map<String, Object> imageData) {
        Long vendorId = Long.parseLong(callback.getEntityId());

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new VendorNotFoundException("Vendor not found: " + vendorId));

        if (vendor.getImages() == null) {
            vendor.setImages(new HashMap<>());
        }

        vendor.getImages().put(callback.getImageType(), imageData);
        vendorRepository.save(vendor);

        log.info("Updated vendor {} with processed image: {}", vendorId, callback.getImageType());

        // Trigger search index sync
        triggerSearchSync("VENDOR", vendorId);
    }

    /**
     * Update branch image with processed URLs
     */
    private void updateBranchImage(ImageProcessingCallback callback, Map<String, Object> imageData) {
        Long branchId = Long.parseLong(callback.getEntityId());

        VendorBranch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found: " + branchId));

        if (branch.getImages() == null) {
            branch.setImages(new HashMap<>());
        }

        branch.getImages().put(callback.getImageType(), imageData);
        branchRepository.save(branch);

        log.info("Updated branch {} with processed image: {}", branchId, callback.getImageType());

        // Trigger search index sync
        triggerSearchSync("BRANCH", branchId);
    }

    /**
     * Update menu item image with processed URLs
     */
    private void updateMenuItemImage(ImageProcessingCallback callback, Map<String, Object> imageData) {
        Long menuItemId = Long.parseLong(callback.getEntityId());

        MenuItem menuItem = menuItemRepository.findByMenuItemIdAndIsDeletedFalse(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found: +" + menuItemId));

        if (menuItem.getImages() == null) {
            menuItem.setImages(new HashMap<>());
        }

        // Store images: primary directly, gallery images inside "gallery" object
        String imageType = callback.getImageType();
        if (imageType.startsWith("gallery_")) {
            // Extract the unique name (e.g., "gallery_1" -> "1", "gallery_maggi" ->
            // "maggi")
            String galleryKey = imageType.substring("gallery_".length());

            // Get or create gallery object
            @SuppressWarnings("unchecked")
            Map<String, Object> galleryObj = (Map<String, Object>) menuItem.getImages()
                    .computeIfAbsent("gallery", k -> new HashMap<String, Object>());

            // Store inside gallery object
            galleryObj.put(galleryKey, imageData);
            log.info("Updated gallery image with key: {}", galleryKey);
        } else {
            // Store primary and other image types directly
            menuItem.getImages().put(imageType, imageData);
            log.info("Updated image with key: {}", imageType);
        }

        menuItemRepository.save(menuItem);

        log.info("Updated menu item {} with processed image: {}", menuItemId, callback.getImageType());

        // Trigger search index sync
        triggerSearchSync("MENU_ITEM", menuItemId);
    }

    /**
     * Mark image as failed in the database
     */
    private void markImageAsFailed(ImageProcessingCallback callback) {
        Map<String, Object> failedData = new HashMap<>();
        failedData.put("status", "FAILED");
        failedData.put("error", callback.getError());
        failedData.put("originalKey", callback.getOriginalKey());
        failedData.put("failedAt", java.time.Instant.now().toString());

        try {
            switch (callback.getEntityType().toLowerCase()) {
                case "vendors" -> {
                    Long vendorId = Long.parseLong(callback.getEntityId());
                    Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
                    if (vendor != null) {
                        if (vendor.getImages() == null)
                            vendor.setImages(new HashMap<>());
                        vendor.getImages().put(callback.getImageType(), failedData);
                        vendorRepository.save(vendor);
                    }
                }
                case "branches" -> {
                    Long branchId = Long.parseLong(callback.getEntityId());
                    VendorBranch branch = branchRepository.findById(branchId).orElse(null);
                    if (branch != null) {
                        if (branch.getImages() == null)
                            branch.setImages(new HashMap<>());
                        branch.getImages().put(callback.getImageType(), failedData);
                        branchRepository.save(branch);
                    }
                }
                case "menu-items" -> {
                    Long menuItemId = Long.parseLong(callback.getEntityId());
                    MenuItem menuItem = menuItemRepository.findByMenuItemIdAndIsDeletedFalse(menuItemId).orElse(null);
                    if (menuItem != null) {
                        if (menuItem.getImages() == null)
                            menuItem.setImages(new HashMap<>());
                        menuItem.getImages().put(callback.getImageType(), failedData);
                        menuItemRepository.save(menuItem);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error marking image as failed: {}", e.getMessage());
        }
    }

    /**
     * Build simplified image data structure with processed URLs.
     * Returns only WebP URLs by size for mobile app consumption.
     * Format: { "thumbnail": "url", "small": "url", "medium": "url", "large": "url"
     * }
     */
    private Map<String, Object> buildImageData(ImageProcessingCallback callback) {
        Map<String, Object> imageData = new LinkedHashMap<>();

        // Extract only WebP URLs by size (simplified structure for mobile)
        if (callback.getVariants() != null) {
            for (Map.Entry<String, ImageProcessingCallback.VariantUrls> entry : callback.getVariants().entrySet()) {
                String size = entry.getKey();
                ImageProcessingCallback.VariantUrls variantUrls = entry.getValue();

                // Store only WebP URL (primary format for mobile)
                if (variantUrls.getWebp() != null) {
                    imageData.put(size, variantUrls.getWebp());
                }
            }
        }

        return imageData;
    }

    /**
     * Trigger search index synchronization
     */
    private void triggerSearchSync(String entityType, Long entityId) {
        try {
            switch (entityType) {
                case "VENDOR" -> {
                    // For vendor image updates, we need to sync all branches
                    vendorRepository.findById(entityId).ifPresent(vendor -> {
                        for (var branch : vendor.getBranches()) {
                            if (branch.getIsActive()) {
                                searchEventPublisher.publishVendorUpdated(branch);
                            }
                        }
                    });
                }
                case "BRANCH" -> {
                    branchRepository.findById(entityId)
                            .ifPresent(branch -> searchEventPublisher.publishVendorUpdated(branch));
                }
                case "MENU_ITEM" -> {
                    menuItemRepository.findByMenuItemIdAndIsDeletedFalse(entityId)
                            .ifPresent(item -> searchEventPublisher.publishMenuItemUpdated(item));
                }
            }
            log.info("Published search sync event for {} {}", entityType, entityId);
        } catch (Exception e) {
            log.error("Error publishing search sync event: {}", e.getMessage());
        }
    }

    // ========== Helper methods for ImageProcessingResult (SQS-based) ==========

    /**
     * Build simplified image data structure from ImageProcessingResult.
     * Returns only WebP URLs by size for mobile app consumption.
     * Format: { "thumbnail": "url", "small": "url", "medium": "url", "large": "url"
     * }
     */
    private Map<String, Object> buildImageDataFromResult(
            com.teadelivery.ordercatalog.common.dto.ImageProcessingResult result) {
        Map<String, Object> imageData = new LinkedHashMap<>();

        // Extract only WebP URLs by size (simplified structure for mobile)
        if (result.getVariants() != null) {
            for (Map.Entry<String, com.teadelivery.ordercatalog.common.dto.ImageProcessingResult.ImageVariant> entry : result
                    .getVariants().entrySet()) {
                String size = entry.getKey();
                var variant = entry.getValue();

                // Store only WebP URL (primary format for mobile)
                if (variant.getWebp() != null) {
                    imageData.put(size, variant.getWebp());
                }
            }
        }

        return imageData;
    }

    /**
     * Update vendor image from ImageProcessingResult
     */
    private void updateVendorImageFromResult(com.teadelivery.ordercatalog.common.dto.ImageProcessingResult result,
            Map<String, Object> imageData) {
        Long vendorId = Long.parseLong(result.getEntityId());

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new VendorNotFoundException("Vendor not found: " + vendorId));

        if (vendor.getImages() == null) {
            vendor.setImages(new HashMap<>());
        }

        vendor.getImages().put(result.getImageType(), imageData);
        vendorRepository.save(vendor);

        log.info("Updated vendor {} with processed image: {}", vendorId, result.getImageType());

        // Trigger search index sync
        triggerSearchSync("VENDOR", vendorId);
    }

    /**
     * Update branch image from ImageProcessingResult
     */
    private void updateBranchImageFromResult(com.teadelivery.ordercatalog.common.dto.ImageProcessingResult result,
            Map<String, Object> imageData) {
        Long branchId = Long.parseLong(result.getEntityId());

        VendorBranch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found: " + branchId));

        if (branch.getImages() == null) {
            branch.setImages(new HashMap<>());
        }

        branch.getImages().put(result.getImageType(), imageData);
        branchRepository.save(branch);

        log.info("Updated branch {} with processed image: {}", branchId, result.getImageType());

        // Trigger search index sync
        triggerSearchSync("BRANCH", branchId);
    }

    /**
     * Update menu item image from ImageProcessingResult
     */
    private void updateMenuItemImageFromResult(com.teadelivery.ordercatalog.common.dto.ImageProcessingResult result,
            Map<String, Object> imageData) {
        Long menuItemId = Long.parseLong(result.getEntityId());

        MenuItem menuItem = menuItemRepository.findByMenuItemIdAndIsDeletedFalse(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found: " + menuItemId));

        if (menuItem.getImages() == null) {
            menuItem.setImages(new HashMap<>());
        }

        // For menu items, handle primary vs gallery images differently
        if ("gallery".equals(result.getImageType())) {
            // Append to gallery array
            @SuppressWarnings("unchecked")
            var gallery = (java.util.List<Map<String, Object>>) menuItem.getImages()
                    .getOrDefault("gallery", new java.util.ArrayList<>());
            gallery.add(imageData);
            menuItem.getImages().put("gallery", gallery);
        } else {
            menuItem.getImages().put(result.getImageType(), imageData);
        }

        menuItemRepository.save(menuItem);

        log.info("Updated menu item {} with processed image: {}", menuItemId, result.getImageType());

        // Trigger search index sync
        triggerSearchSync("MENU_ITEM", menuItemId);
    }

    /**
     * Mark image as failed from ImageProcessingResult
     */
    private void markImageAsFailedFromResult(com.teadelivery.ordercatalog.common.dto.ImageProcessingResult result) {
        Map<String, Object> failedData = new HashMap<>();
        failedData.put("status", "FAILED");
        failedData.put("error", "Image processing failed");
        failedData.put("originalKey", result.getOriginalKey());
        failedData.put("failedAt", java.time.Instant.now().toString());

        try {
            switch (result.getEntityType().toLowerCase()) {
                case "vendors" -> {
                    Long vendorId = Long.parseLong(result.getEntityId());
                    Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
                    if (vendor != null) {
                        if (vendor.getImages() == null)
                            vendor.setImages(new HashMap<>());
                        vendor.getImages().put(result.getImageType(), failedData);
                        vendorRepository.save(vendor);
                    }
                }
                case "vendor-branches" -> {
                    Long branchId = Long.parseLong(result.getEntityId());
                    VendorBranch branch = branchRepository.findById(branchId).orElse(null);
                    if (branch != null) {
                        if (branch.getImages() == null)
                            branch.setImages(new HashMap<>());
                        branch.getImages().put(result.getImageType(), failedData);
                        branchRepository.save(branch);
                    }
                }
                case "menu-items" -> {
                    Long menuItemId = Long.parseLong(result.getEntityId());
                    MenuItem menuItem = menuItemRepository.findByMenuItemIdAndIsDeletedFalse(menuItemId).orElse(null);
                    if (menuItem != null) {
                        if (menuItem.getImages() == null)
                            menuItem.setImages(new HashMap<>());
                        menuItem.getImages().put(result.getImageType(), failedData);
                        menuItemRepository.save(menuItem);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error marking image as failed: {}", e.getMessage());
        }
    }

}
