package com.teadelivery.ordercatalog.search.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Search Analytics Entity
 * 
 * Tracks search queries for analytics, optimization, and insights.
 */
@Entity
@Table(name = "search_analytics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchAnalytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Query Info
    @Column(name = "query_text", nullable = false, length = 500)
    private String queryText;
    
    @Column(name = "query_type", nullable = false, length = 50)
    private String queryType;
    
    @Column(name = "search_context", length = 50)
    private String searchContext;
    
    // User Context
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    // Location Context
    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;
    
    @Column(name = "city", length = 100)
    private String city;
    
    // Filters Applied
    @Type(JsonBinaryType.class)
    @Column(name = "filters", columnDefinition = "jsonb")
    private Map<String, Object> filters;
    
    // Results
    @Column(name = "result_count", nullable = false)
    private Integer resultCount;
    
    @Column(name = "zero_results", insertable = false, updatable = false)
    private Boolean zeroResults;
    
    // Performance
    @Column(name = "response_time_ms", nullable = false)
    private Integer responseTimeMs;
    
    @Column(name = "cache_hit")
    private Boolean cacheHit;
    
    // Ranking Metrics
    @Column(name = "avg_fts_score", precision = 5, scale = 4)
    private BigDecimal avgFtsScore;
    
    @Column(name = "avg_fuzzy_score", precision = 5, scale = 4)
    private BigDecimal avgFuzzyScore;
    
    @Column(name = "ranking_strategy", length = 50)
    private String rankingStrategy;
    
    // Timestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (cacheHit == null) {
            cacheHit = false;
        }
    }
}

