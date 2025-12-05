package com.teadelivery.ordercatalog.search.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Payload for menu item index events.
 * Contains all data needed to populate search_menu_items table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemIndexPayload {

    /**
     * Primary key - menu item ID
     */
    private Long menuItemId;

    /**
     * Branch ID (foreign key)
     */
    private Long branchId;

    /**
     * Vendor ID (denormalized)
     */
    private Long vendorId;

    /**
     * Item name
     */
    private String itemName;

    /**
     * Item description
     */
    private String description;

    /**
     * Price
     */
    private BigDecimal price;

    /**
     * Category (e.g., Beverages, Snacks)
     */
    private String category;

    /**
     * Branch name (denormalized for search results)
     */
    private String branchName;

    /**
     * Vendor name (denormalized for search results)
     */
    private String vendorName;

    /**
     * City for location filtering
     */
    private String city;

    /**
     * Branch latitude (for proximity)
     */
    private BigDecimal branchLatitude;

    /**
     * Branch longitude (for proximity)
     */
    private BigDecimal branchLongitude;

    /**
     * Whether item is available
     */
    private Boolean isAvailable;

    /**
     * Tags for filtering (max 20)
     */
    private List<String> tags;

    /**
     * Dietary info (max 10) - e.g., Vegetarian, Vegan, Gluten-Free
     */
    private List<String> dietaryInfo;

    /**
     * Preparation time in minutes
     */
    private Integer preparationTimeMinutes;

    /**
     * Total order count for popularity
     */
    private Integer orderCount;

    /**
     * Average rating (0-5)
     */
    private BigDecimal rating;

    /**
     * Images with multiple sizes (JSONB structure)
     */
    private List<Map<String, Object>> images;

    /**
     * Primary image URL for quick access
     */
    private String primaryImage;
}
