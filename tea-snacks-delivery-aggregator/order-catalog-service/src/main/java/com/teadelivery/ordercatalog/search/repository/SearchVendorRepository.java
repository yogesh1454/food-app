package com.teadelivery.ordercatalog.search.repository;

import com.teadelivery.ordercatalog.search.model.SearchVendor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Search Vendor Repository
 * 
 * Custom queries for geospatial search, full-text search, and blended ranking
 */
@Repository
public interface SearchVendorRepository extends JpaRepository<SearchVendor, Long> {
    
    /**
     * Find nearby vendors within radius using PostGIS ST_DWithin
     * Ordered by distance
     */
    @Query(value = """
        SELECT v.*, 
               ST_Distance(v.location::geography, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography) / 1000.0 AS distance_km
        FROM search_vendors v
        WHERE ST_DWithin(
            v.location::geography,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
            :radiusMeters
        )
        AND v.is_active = true
        AND (:city IS NULL OR v.city = :city)
        ORDER BY distance_km ASC
        """, nativeQuery = true)
    List<SearchVendor> findNearbyVendors(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusMeters") Integer radiusMeters,
            @Param("city") String city,
            Pageable pageable
    );
    
    /**
     * Hybrid search with blended ranking
     * Combines FTS, Fuzzy search, proximity, and popularity
     */
    @Query(value = """
        WITH nearby_vendors AS (
            SELECT 
                v.branch_id,
                v.vendor_name,
                v.branch_name,
                v.display_name,
                v.rating,
                v.normalized_popularity,
                v.order_count,
                ST_Distance(
                    v.location::geography,
                    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
                ) / 1000.0 AS distance_km
            FROM search_vendors v
            WHERE 
                ST_DWithin(
                    v.location::geography,
                    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
                    :radiusMeters
                )
                AND v.is_active = true
                AND (:city IS NULL OR v.city = :city)
        ),
        scored_vendors AS (
            SELECT 
                nv.*,
                COALESCE(
                    ts_rank_cd(sv.search_vector, to_tsquery('english', :query || ':*')) /
                    NULLIF(
                        (SELECT MAX(ts_rank_cd(sv2.search_vector, to_tsquery('english', :query || ':*')))
                         FROM search_vendors sv2
                         WHERE sv2.branch_id IN (SELECT branch_id FROM nearby_vendors)),
                        0
                    ),
                    0
                ) AS fts_score,
                GREATEST(
                    COALESCE(similarity(sv.vendor_name, :query), 0),
                    COALESCE(similarity(sv.branch_name, :query), 0),
                    COALESCE(similarity(sv.display_name, :query), 0)
                ) AS fuzzy_score,
                1.0 / (1.0 + nv.distance_km) AS proximity_factor
            FROM nearby_vendors nv
            JOIN search_vendors sv ON sv.branch_id = nv.branch_id
        )
        SELECT sv.* FROM scored_vendors sc
        JOIN search_vendors sv ON sv.branch_id = sc.branch_id
        WHERE (sc.fts_score > 0 OR sc.fuzzy_score > 0.3)
        ORDER BY 
            ((0.50 * sc.fts_score) + 
             (0.30 * sc.fuzzy_score) + 
             (0.05 * sc.proximity_factor) + 
             (0.15 * COALESCE(sc.normalized_popularity, 0))) DESC,
            sc.distance_km ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<SearchVendor> hybridSearch(
            @Param("query") String query,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusMeters") Integer radiusMeters,
            @Param("city") String city,
            @Param("limit") Integer limit
    );
    
    /**
     * Find open vendors by city
     */
    List<SearchVendor> findByCityAndIsOpenTrueAndIsActiveTrueOrderByNormalizedPopularityDescRatingDesc(
            String city, 
            Pageable pageable
    );
    
    /**
     * Find by vendor ID
     */
    List<SearchVendor> findByVendorIdAndIsActiveTrue(Long vendorId);
}

