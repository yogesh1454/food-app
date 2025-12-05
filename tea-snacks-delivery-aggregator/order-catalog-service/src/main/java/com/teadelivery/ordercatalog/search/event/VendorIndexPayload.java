package com.teadelivery.ordercatalog.search.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Payload for vendor/branch index events.
 * Contains all data needed to populate search_vendors table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorIndexPayload {

    /**
     * Primary key - branch ID
     */
    private Long branchId;

    /**
     * Vendor ID (parent)
     */
    private Long vendorId;

    /**
     * Vendor name (from vendors table)
     */
    private String vendorName;

    /**
     * Branch name
     */
    private String branchName;

    /**
     * Display name (for UI)
     */
    private String displayName;

    /**
     * City for location filtering
     */
    private String city;

    /**
     * Area within city
     */
    private String area;

    /**
     * Latitude for geospatial queries
     */
    private BigDecimal latitude;

    /**
     * Longitude for geospatial queries
     */
    private BigDecimal longitude;

    /**
     * Full address as JSONB
     */
    private Map<String, Object> address;

    /**
     * Cuisine types (max 20)
     */
    private List<String> cuisine;

    /**
     * Tags for filtering (max 20)
     */
    private List<String> tags;

    /**
     * Average rating (0-5)
     */
    private BigDecimal rating;

    /**
     * Total number of ratings/reviews
     */
    private Integer totalRatings;

    /**
     * Whether branch is currently open
     */
    private Boolean isOpen;

    /**
     * Whether branch is active
     */
    private Boolean isActive;

    /**
     * Minimum delivery time in minutes
     */
    private Integer deliveryTimeMin;

    /**
     * Maximum delivery time in minutes
     */
    private Integer deliveryTimeMax;

    /**
     * Delivery fee
     */
    private BigDecimal deliveryFee;

    /**
     * Minimum order value
     */
    private BigDecimal minOrderValue;

    /**
     * Total order count for popularity
     */
    private Integer orderCount;

    /**
     * Images with multiple sizes (JSONB structure)
     */
    private List<Map<String, Object>> images;

    /**
     * Primary image URL for quick access
     */
    private String primaryImage;
}
