package com.teadelivery.ordercatalog.search.model;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Search Vendor Entity
 * 
 * Denormalized vendor/branch index for fast location-based and text search.
 * Uses PostGIS for geospatial queries and pg_trgm for fuzzy search.
 */
@Entity
@Table(name = "search_vendors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchVendor {
    
    @Id
    @Column(name = "branch_id")
    private Long branchId;
    
    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;
    
    // Basic Info
    @Column(name = "vendor_name", nullable = false)
    private String vendorName;
    
    @Column(name = "branch_name", nullable = false)
    private String branchName;
    
    @Column(name = "display_name", nullable = false)
    private String displayName;
    
    // Location (PostGIS Point)
    @Column(name = "location", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point location;
    
    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;
    
    @Column(name = "city", nullable = false, length = 100)
    private String city;
    
    @Column(name = "area", length = 100)
    private String area;
    
    @Type(JsonBinaryType.class)
    @Column(name = "address", columnDefinition = "jsonb")
    private Map<String, Object> address;
    
    // Searchable Attributes
    @Type(StringArrayType.class)
    @Column(name = "cuisine", columnDefinition = "text[]")
    private String[] cuisine;
    
    @Type(StringArrayType.class)
    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;
    
    // Metrics & Filters
    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;
    
    @Column(name = "total_ratings")
    private Integer totalRatings;
    
    @Column(name = "is_open", nullable = false)
    private Boolean isOpen;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    // Delivery Info
    @Column(name = "delivery_time_min")
    private Integer deliveryTimeMin;
    
    @Column(name = "delivery_time_max")
    private Integer deliveryTimeMax;
    
    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;
    
    @Column(name = "min_order_value", precision = 10, scale = 2)
    private BigDecimal minOrderValue;
    
    // Popularity Metrics
    @Column(name = "order_count")
    private Integer orderCount;
    
    @Column(name = "popularity_score", precision = 5, scale = 2)
    private BigDecimal popularityScore;
    
    @Column(name = "normalized_popularity", precision = 5, scale = 4, insertable = false, updatable = false)
    private BigDecimal normalizedPopularity;
    
    // Images (JSONB with multiple sizes)
    @Type(JsonBinaryType.class)
    @Column(name = "images", columnDefinition = "jsonb")
    private List<Map<String, Object>> images;
    
    @Column(name = "primary_image", length = 500)
    private String primaryImage;
    
    // Sync Metadata
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "last_synced_at", nullable = false)
    private Instant lastSyncedAt;
    
    @Column(name = "sync_version")
    private Integer syncVersion;
    
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        lastSyncedAt = now;
        if (syncVersion == null) {
            syncVersion = 1;
        }
        if (orderCount == null) {
            orderCount = 0;
        }
        if (totalRatings == null) {
            totalRatings = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        lastSyncedAt = Instant.now();
        if (syncVersion != null) {
            syncVersion++;
        }
    }
}

