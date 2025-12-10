package com.teadelivery.ordercatalog.search.repository;

import com.teadelivery.ordercatalog.search.model.SearchMenuItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Search Menu Item Repository
 * 
 * Custom queries for menu item search with geospatial filtering and blended
 * ranking
 */
@Repository
public interface SearchMenuItemRepository extends JpaRepository<SearchMenuItem, Long> {

    /**
     * Find items by branch ID
     */
    List<SearchMenuItem> findByBranchIdAndIsAvailableTrueAndIsDeletedFalseOrderByCategory(Long branchId);

    /**
     * Find popular items in area (last 30 days)
     */
    @Query(value = """
            SELECT mi.* FROM search_menu_items mi
            WHERE ST_DWithin(
                CAST(mi.branch_location AS geography),
                CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
                :radiusMeters
            )
            AND mi.is_available = true
            AND mi.is_deleted = false
            AND mi.order_count_30d >= 0
            ORDER BY mi.order_count_30d DESC, mi.rating DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<SearchMenuItem> findPopularItemsInArea(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusMeters") Integer radiusMeters,
            @Param("limit") Integer limit);

    /**
     * Find trending items (last 7 days)
     */
    @Query(value = """
            SELECT mi.* FROM search_menu_items mi
            WHERE ST_DWithin(
                CAST(mi.branch_location AS geography),
                CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
                :radiusMeters
            )
            AND mi.is_available = true
            AND mi.is_deleted = false
            AND mi.order_count_7d >= 0
            ORDER BY mi.trending_score DESC, mi.order_count_7d DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<SearchMenuItem> findTrendingItems(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusMeters") Integer radiusMeters,
            @Param("limit") Integer limit);

    /**
     * Hybrid search for menu items with blended ranking
     */
    @Query(value = """
            WITH nearby_items AS (
                SELECT
                    mi.menu_item_id,
                    mi.item_name,
                    mi.price,
                    mi.normalized_popularity,
                    ST_Distance(
                        CAST(mi.branch_location AS geography),
                        CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography)
                    ) / 1000.0 AS distance_km
                FROM search_menu_items mi
                WHERE
                    ST_DWithin(
                        CAST(mi.branch_location AS geography),
                        CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
                        :radiusMeters
                    )
                    AND mi.is_available = true
                    AND mi.is_deleted = false
            ),
            scored_items AS (
                SELECT
                    ni.*,
                    COALESCE(
                        ts_rank_cd(smi.search_vector, to_tsquery('english', :query || ':*')) /
                        NULLIF(
                            (SELECT MAX(ts_rank_cd(smi2.search_vector, to_tsquery('english', :query || ':*')))
                             FROM search_menu_items smi2
                             WHERE smi2.menu_item_id IN (SELECT menu_item_id FROM nearby_items)),
                            0
                        ),
                        0
                    ) AS fts_score,
                    COALESCE(similarity(smi.item_name, :query), 0) AS fuzzy_score,
                    1.0 / (1.0 + ni.distance_km) AS proximity_factor
                FROM nearby_items ni
                JOIN search_menu_items smi ON smi.menu_item_id = ni.menu_item_id
            )
            SELECT smi.* FROM scored_items si
            JOIN search_menu_items smi ON smi.menu_item_id = si.menu_item_id
            WHERE (si.fts_score > 0 OR si.fuzzy_score > 0.3)
            ORDER BY
                ((0.50 * si.fts_score) +
                 (0.30 * si.fuzzy_score) +
                 (0.05 * si.proximity_factor) +
                 (0.15 * COALESCE(si.normalized_popularity, 0))) DESC,
                si.distance_km ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<SearchMenuItem> hybridSearch(
            @Param("query") String query,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusMeters") Integer radiusMeters,
            @Param("limit") Integer limit);

    /**
     * Find items by category within area
     */
    @Query(value = """
            SELECT mi.* FROM search_menu_items mi
            WHERE mi.category = :category
            AND ST_DWithin(
                CAST(mi.branch_location AS geography),
                CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
                :radiusMeters
            )
            AND mi.is_available = true
            AND mi.is_deleted = false
            ORDER BY mi.normalized_popularity DESC, mi.rating DESC
            """, nativeQuery = true)
    List<SearchMenuItem> findByCategory(
            @Param("category") String category,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusMeters") Integer radiusMeters,
            Pageable pageable);
}
