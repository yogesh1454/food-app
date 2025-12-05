package com.teadelivery.ordercatalog.search.service;

import com.teadelivery.ordercatalog.search.dto.VendorMenuResponse;

import java.util.UUID;

/**
 * Vendor Menu Service - Complete menu with categories, items, and recommendations
 */
public interface VendorMenuService {
    
    /**
     * Get complete vendor menu with categories, items, and recommendations
     * 
     * @param branchId Branch ID
     * @param userId User ID for personalized recommendations
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @return Complete menu with recommendations
     */
    VendorMenuResponse getVendorMenu(
            Long branchId,
            UUID userId,
            Double latitude,
            Double longitude
    );
}

