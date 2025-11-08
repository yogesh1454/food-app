package com.teadelivery.ordercatalog.menu.mapper;

import com.teadelivery.ordercatalog.menu.dto.MenuItemResponse;
import com.teadelivery.ordercatalog.menu.model.MenuItem;

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
            .images(menuItem.getImages())
            .metadata(menuItem.getMetadata())
            .tags(menuItem.getTags())
            .createdAt(menuItem.getCreatedAt())
            .updatedAt(menuItem.getUpdatedAt())
            .build();
    }
}
