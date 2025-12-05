package com.teadelivery.ordercatalog.search.repository;

import com.teadelivery.ordercatalog.search.model.PopularQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Popular Queries Repository
 * 
 * Manage popular search queries for auto-complete
 */
@Repository
public interface PopularQueriesRepository extends JpaRepository<PopularQuery, Integer> {
    
    /**
     * Find by query text and period
     */
    Optional<PopularQuery> findByQueryTextAndPeriodAndCity(String queryText, String period, String city);
    
    /**
     * Find popular queries for suggestions
     */
    @Query("SELECT pq FROM PopularQuery pq " +
           "WHERE pq.period = :period " +
           "AND (:city IS NULL OR pq.city = :city) " +
           "ORDER BY pq.searchCount DESC, pq.clickThroughRate DESC")
    List<PopularQuery> findPopularForSuggestions(
            @Param("period") String period,
            @Param("city") String city,
            Pageable pageable
    );
    
    /**
     * Find queries starting with prefix (for auto-complete)
     */
    @Query("SELECT pq FROM PopularQuery pq " +
           "WHERE pq.queryText LIKE :prefix% " +
           "AND pq.period = :period " +
           "AND (:city IS NULL OR pq.city = :city) " +
           "ORDER BY pq.searchCount DESC")
    List<PopularQuery> findByQueryTextStartingWith(
            @Param("prefix") String prefix,
            @Param("period") String period,
            @Param("city") String city,
            Pageable pageable
    );
}


