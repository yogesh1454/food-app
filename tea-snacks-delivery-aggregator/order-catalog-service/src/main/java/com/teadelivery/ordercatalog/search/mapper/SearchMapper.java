package com.teadelivery.ordercatalog.search.mapper;

import com.teadelivery.ordercatalog.search.dto.*;
import com.teadelivery.ordercatalog.search.model.SearchMenuItem;
import com.teadelivery.ordercatalog.search.model.SearchVendor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Search Mapper
 * 
 * Maps search entities to DTOs with image parsing and data transformation
 */
@Component
public class SearchMapper {
    
    /**
     * Map SearchVendor entity to VendorSearchResult DTO
     */
    public VendorSearchResult toVendorSearchResult(SearchVendor vendor) {
        if (vendor == null) {
            return null;
        }
        
        return VendorSearchResult.builder()
                .branchId(vendor.getBranchId())
                .vendorId(vendor.getVendorId())
                .branchName(vendor.getBranchName())
                .displayName(vendor.getDisplayName())
                .cuisine(vendor.getCuisine() != null ? Arrays.asList(vendor.getCuisine()) : Collections.emptyList())
                .rating(vendor.getRating())
                .totalRatings(vendor.getTotalRatings())
                .deliveryTime(formatDeliveryTime(vendor.getDeliveryTimeMin(), vendor.getDeliveryTimeMax()))
                .distance(calculateDistance(vendor.getLocation()))
                .distanceUnit("km")
                .deliveryFee(vendor.getDeliveryFee())
                .minOrderValue(vendor.getMinOrderValue())
                .isOpen(vendor.getIsOpen())
                .openingTime(vendor.getIsOpen() ? null : "Closed") // TODO: Add actual opening time logic
                .images(parseImages(vendor.getImages(), vendor.getPrimaryImage()))
                .tags(vendor.getTags() != null ? Arrays.asList(vendor.getTags()) : Collections.emptyList())
                .rankingScore(vendor.getNormalizedPopularity() != null ? vendor.getNormalizedPopularity().doubleValue() : 0.0)
                .build();
    }
    
    /**
     * Map SearchMenuItem entity to MenuItemSearchResult DTO
     */
    public MenuItemSearchResult toMenuItemSearchResult(SearchMenuItem item) {
        if (item == null) {
            return null;
        }
        
        return MenuItemSearchResult.builder()
                .menuItemId(item.getMenuItemId())
                .name(item.getItemName())
                .description(item.getDescription())
                .branchId(item.getBranchId())
                .branchName(item.getBranchName())
                .vendorName(item.getVendorName())
                .price(item.getPrice())
                .category(item.getCategory())
                .images(parseImages(item.getImages(), item.getPrimaryImage()))
                .rating(item.getRating())
                .preparationTime(item.getPreparationTimeMinutes())
                .dietaryInfo(item.getDietaryInfo() != null ? Arrays.asList(item.getDietaryInfo()) : Collections.emptyList())
                .nutrition(null) // TODO: Add nutrition info if available
                .isAvailable(item.getIsAvailable())
                .availabilityMessage(item.getIsAvailable() ? null : "Currently unavailable")
                .distance(calculateDistance(item.getBranchLocation()))
                .orderCount(item.getOrderCount())
                .rankingScore(item.getNormalizedPopularity() != null ? item.getNormalizedPopularity().doubleValue() : 0.0)
                .trendingScore(item.getTrendingScore() != null ? item.getTrendingScore().doubleValue() : 0.0)
                .build();
    }
    
    /**
     * Parse images from JSONB to ImagesResponse
     */
    private ImagesResponse parseImages(List<Map<String, Object>> images, String primaryImage) {
        ImagesResponse.ImagesResponseBuilder builder = ImagesResponse.builder()
                .primary(primaryImage);
        
        if (images == null || images.isEmpty()) {
            return builder.build();
        }
        
        // Find cover and logo images
        for (Map<String, Object> image : images) {
            String type = (String) image.get("type");
            @SuppressWarnings("unchecked")
            Map<String, String> urls = (Map<String, String>) image.get("urls");
            
            if (type != null && urls != null) {
                switch (type.toLowerCase()) {
                    case "cover":
                        builder.cover(urls);
                        break;
                    case "logo":
                        builder.logo(urls);
                        break;
                    case "gallery":
                        builder.gallery(urls);
                        break;
                }
            }
        }
        
        return builder.build();
    }
    
    /**
     * Format delivery time from min/max
     */
    private String formatDeliveryTime(Integer min, Integer max) {
        if (min == null && max == null) {
            return "N/A";
        }
        if (min == null) {
            return max + " min";
        }
        if (max == null) {
            return min + " min";
        }
        return min + "-" + max + " min";
    }
    
    /**
     * Calculate distance from Point (placeholder - actual distance would come from query)
     */
    private Double calculateDistance(Point location) {
        // This is a placeholder - actual distance is calculated in SQL queries
        return 0.0;
    }
    
    /**
     * Batch convert vendors
     */
    public List<VendorSearchResult> toVendorSearchResults(List<SearchVendor> vendors) {
        if (vendors == null) {
            return Collections.emptyList();
        }
        return vendors.stream()
                .map(this::toVendorSearchResult)
                .collect(Collectors.toList());
    }
    
    /**
     * Batch convert menu items
     */
    public List<MenuItemSearchResult> toMenuItemSearchResults(List<SearchMenuItem> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(this::toMenuItemSearchResult)
                .collect(Collectors.toList());
    }
}


