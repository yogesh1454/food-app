package com.teadelivery.ordercatalog.menu.mapper;

import com.teadelivery.ordercatalog.menu.dto.ImageVariant;
import com.teadelivery.ordercatalog.menu.dto.MenuItemImages;
import com.teadelivery.ordercatalog.menu.dto.MenuItemResponse;
import com.teadelivery.ordercatalog.menu.model.MenuItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MenuMapper {

    private MenuMapper() {
        // Utility class
    }

    public static MenuItemResponse toResponse(MenuItem menuItem) {
        if (menuItem == null) {
            return null;
        }

        return MenuItemResponse.builder()
                .menuItemId(menuItem.getMenuItemId())
                .branchId(menuItem.getBranch().getBranchId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .category(menuItem.getCategory())
                .isAvailable(menuItem.getIsAvailable())
                .preparationTimeMinutes(menuItem.getPreparationTimeMinutes())
                .images(transformImages(menuItem.getImages()))
                .metadata(menuItem.getMetadata())
                .tags(menuItem.getTags())
                .createdAt(menuItem.getCreatedAt())
                .updatedAt(menuItem.getUpdatedAt())
                .build();
    }

    /**
     * Transform flat JSONB structure to grouped MenuItemImages structure.
     * 
     * Input (JSONB):
     * {
     * "primary": { "thumbnail": "url", "small": "url", ... },
     * "gallery_1": { "thumbnail": "url", "small": "url", ... },
     * "gallery_2": { "thumbnail": "url", "small": "url", ... }
     * }
     * 
     * Output:
     * {
     * "primary": { "thumbnail": "url", "small": "url", ... },
     * "gallery": [
     * { "index": 1, "thumbnail": "url", "small": "url", ... },
     * { "index": 2, "thumbnail": "url", "small": "url", ... }
     * ]
     * }
     */
    @SuppressWarnings("unchecked")
    private static MenuItemImages transformImages(Map<String, Object> rawImages) {
        if (rawImages == null || rawImages.isEmpty()) {
            return MenuItemImages.builder().build();
        }

        Map<String, String> primary = null;
        List<ImageVariant> gallery = new ArrayList<>();

        for (Map.Entry<String, Object> entry : rawImages.entrySet()) {
            String key = entry.getKey();

            if ("primary".equals(key)) {
                // Primary image
                primary = (Map<String, String>) entry.getValue();
            } else if (key.startsWith("gallery_")) {
                // Gallery image - extract index and create ImageVariant
                try {
                    int index = Integer.parseInt(key.substring("gallery_".length()));
                    Map<String, String> variants = (Map<String, String>) entry.getValue();

                    ImageVariant imageVariant = ImageVariant.builder()
                            .index(index)
                            .thumbnail(variants.get("thumbnail"))
                            .small(variants.get("small"))
                            .medium(variants.get("medium"))
                            .large(variants.get("large"))
                            .build();

                    gallery.add(imageVariant);
                } catch (NumberFormatException e) {
                    // Skip invalid gallery keys
                }
            }
        }

        // Sort gallery by index
        gallery.sort(Comparator.comparing(ImageVariant::getIndex));

        return MenuItemImages.builder()
                .primary(primary)
                .gallery(gallery.isEmpty() ? null : gallery)
                .build();
    }
}
