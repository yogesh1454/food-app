package com.teadelivery.ordercatalog.search.repository;

import com.teadelivery.ordercatalog.search.model.SearchAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Search Analytics Repository
 * 
 * Track and analyze search queries
 */
@Repository
public interface SearchAnalyticsRepository extends JpaRepository<SearchAnalytics, Long> {
    
    /**
     * Find zero-result queries for optimization
     */
    List<SearchAnalytics> findByZeroResultsTrueOrderByCreatedAtDesc();
    
    /**
     * Find popular queries in time range
     */
    @Query("SELECT sa.queryText, COUNT(sa) as count FROM SearchAnalytics sa " +
           "WHERE sa.createdAt BETWEEN :startTime AND :endTime " +
           "AND sa.city = :city " +
           "GROUP BY sa.queryText " +
           "ORDER BY count DESC")
    List<Object[]> findPopularQueries(
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("city") String city
    );
}


