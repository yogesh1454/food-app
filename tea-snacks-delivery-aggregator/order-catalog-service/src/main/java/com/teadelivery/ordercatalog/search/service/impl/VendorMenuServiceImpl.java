package com.teadelivery.ordercatalog.search.service.impl;

import com.teadelivery.ordercatalog.search.dto.MenuCategoryDto;
import com.teadelivery.ordercatalog.search.dto.MenuItemSearchResult;
import com.teadelivery.ordercatalog.search.dto.VendorMenuResponse;
import com.teadelivery.ordercatalog.search.mapper.SearchMapper;
import com.teadelivery.ordercatalog.search.model.SearchMenuItem;
import com.teadelivery.ordercatalog.search.model.SearchVendor;
import com.teadelivery.ordercatalog.search.repository.SearchMenuItemRepository;
import com.teadelivery.ordercatalog.search.repository.SearchVendorRepository;
import com.teadelivery.ordercatalog.search.service.VendorMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Vendor Menu Service Implementation
 * 
 * Provides complete vendor menu with categories and recommendations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VendorMenuServiceImpl implements VendorMenuService {
    
    private final SearchVendorRepository vendorRepository;
    private final SearchMenuItemRepository menuItemRepository;
    private final SearchMapper mapper;
    
    @Override
    public VendorMenuResponse getVendorMenu(
            Long branchId,
            UUID userId,
            Double latitude,
            Double longitude
    ) {
        log.info("Getting vendor menu for branchId: {}, userId: {}", branchId, userId);
        
        // Get vendor info
        SearchVendor vendor = vendorRepository.findById(branchId).orElse(null);
        if (vendor == null) {
            log.warn("Vendor branch not found: {}", branchId);
            return null;
        }
        
        // Get all menu items for this branch
        List<SearchMenuItem> menuItems = menuItemRepository.findByBranchIdAndIsAvailableTrueAndIsDeletedFalseOrderByCategory(branchId);
        
        // Group by category
        Map<String, List<SearchMenuItem>> itemsByCategory = menuItems.stream()
                .collect(Collectors.groupingBy(SearchMenuItem::getCategory));
        
        // Build category DTOs
        List<MenuCategoryDto> categories = itemsByCategory.entrySet().stream()
                .map(entry -> MenuCategoryDto.builder()
                        .categoryName(entry.getKey())
                        .displayOrder(getCategoryOrder(entry.getKey()))
                        .items(mapper.toMenuItemSearchResults(entry.getValue()))
                        .build())
                .sorted(Comparator.comparing(MenuCategoryDto::getDisplayOrder))
                .collect(Collectors.toList());
        
        // Get recommendations (top rated items from this vendor)
        List<MenuItemSearchResult> recommendations = menuItems.stream()
                .filter(item -> item.getRating() != null && item.getRating().doubleValue() >= 4.0)
                .sorted(Comparator.comparing(SearchMenuItem::getRating).reversed())
                .limit(5)
                .map(mapper::toMenuItemSearchResult)
                .collect(Collectors.toList());
        
        // Get popular items (high order count)
        List<MenuItemSearchResult> popularItems = menuItems.stream()
                .filter(item -> item.getOrderCount() != null && item.getOrderCount() > 10)
                .sorted(Comparator.comparing(SearchMenuItem::getOrderCount).reversed())
                .limit(5)
                .map(mapper::toMenuItemSearchResult)
                .collect(Collectors.toList());
        
        return VendorMenuResponse.builder()
                .vendor(mapper.toVendorSearchResult(vendor))
                .categories(categories)
                .recommendations(recommendations)
                .popularItems(popularItems)
                .build();
    }
    
    /**
     * Get display order for category (customize as needed)
     */
    private Integer getCategoryOrder(String category) {
        Map<String, Integer> order = Map.of(
                "Beverages", 1,
                "Snacks", 2,
                "Main Course", 3,
                "Desserts", 4
        );
        return order.getOrDefault(category, 99);
    }
}

