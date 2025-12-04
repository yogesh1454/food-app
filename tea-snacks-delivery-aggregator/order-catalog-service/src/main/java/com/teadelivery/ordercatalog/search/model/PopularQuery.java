package com.teadelivery.ordercatalog.search.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Popular Query Entity
 * 
 * Caches popular searches for auto-complete and search suggestions.
 */
@Entity
@Table(name = "search_popular_queries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularQuery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "query_text", nullable = false, length = 500)
    private String queryText;
    
    @Column(name = "query_type", nullable = false, length = 50)
    private String queryType;
    
    // Popularity Metrics
    @Column(name = "search_count", nullable = false)
    private Integer searchCount;
    
    @Column(name = "click_through_rate", precision = 5, scale = 4)
    private BigDecimal clickThroughRate;
    
    // Time Period
    @Column(name = "period", nullable = false, length = 20)
    private String period;
    
    @Column(name = "period_start", nullable = false)
    private Instant periodStart;
    
    @Column(name = "period_end", nullable = false)
    private Instant periodEnd;
    
    // Context
    @Column(name = "city", length = 100)
    private String city;
    
    // Display
    @Column(name = "display_text", length = 500)
    private String displayText;
    
    @Column(name = "suggestion_order")
    private Integer suggestionOrder;
    
    // Timestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        updatedAt = Instant.now();
        if (searchCount == null) {
            searchCount = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}


