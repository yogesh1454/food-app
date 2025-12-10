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
 * Search Menu Item Entity
 * 
 * Denormalized menu item index with vendor context for fast search.
 * Includes trending and popularity metrics for recommendations.
 */
@Entity
@Table(name = "search_menu_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchMenuItem {
    
    @Id
    @Column(name = "menu_item_id")
    private Long menuItemId;
    
    // Menu Item Info
    @Column(name = "item_name", nullable = false)
    private String itemName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "category", nullable = false, length = 100)
    private String category;
    
    // Vendor Context
    @Column(name = "branch_id", nullable = false)
    private Long branchId;
    
    @Column(name = "branch_name", nullable = false)
    private String branchName;
    
    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;
    
    @Column(name = "vendor_name", nullable = false)
    private String vendorName;
    
    // Location (for proximity filtering)
    @Column(name = "branch_location", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point branchLocation;
    
    @Column(name = "branch_latitude", precision = 10, scale = 8)
    private BigDecimal branchLatitude;
    
    @Column(name = "branch_longitude", precision = 11, scale = 8)
    private BigDecimal branchLongitude;
    
    @Column(name = "city", nullable = false, length = 100)
    private String city;
    
    // Availability & Attributes
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;
    
    @Type(StringArrayType.class)
    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;
    
    @Type(StringArrayType.class)
    @Column(name = "dietary_info", columnDefinition = "text[]")
    private String[] dietaryInfo;
    
    @Column(name = "preparation_time_minutes")
    private Integer preparationTimeMinutes;
    
    // Popularity Metrics
    @Column(name = "order_count")
    private Integer orderCount;
    
    @Column(name = "order_count_7d")
    private Integer orderCount7d;
    
    @Column(name = "order_count_30d")
    private Integer orderCount30d;
    
    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;
    
    @Column(name = "popularity_score", precision = 5, scale = 2)
    private BigDecimal popularityScore;
    
    @Column(name = "normalized_popularity", precision = 5, scale = 4, insertable = false, updatable = false)
    private BigDecimal normalizedPopularity;
    
    @Column(name = "trending_score", precision = 5, scale = 4, insertable = false, updatable = false)
    private BigDecimal trendingScore;
    
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
        if (orderCount7d == null) {
            orderCount7d = 0;
        }
        if (orderCount30d == null) {
            orderCount30d = 0;
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

