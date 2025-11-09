package com.teadelivery.ordercatalog.rider.repository;

import com.teadelivery.ordercatalog.rider.model.Rider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Rider Repository with PostGIS Geospatial Queries
 * As per BE-003-24
 */
@Repository
public interface RiderRepository extends JpaRepository<Rider, UUID> {
    
    /**
     * Find riders within radius using PostGIS ST_DWithin
     * @param longitude Restaurant longitude
     * @param latitude Restaurant latitude
     * @param radiusMeters Search radius in meters
     * @return List of riders within radius, ordered by distance
     */
    @Query(value = """
        SELECT r.* FROM riders r
        WHERE r.is_online = true
        AND r.is_on_break = false
        AND r.current_deliveries < 2
        AND (r.penalty_until IS NULL OR r.penalty_until < NOW())
        AND ST_DWithin(
            r.current_location,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
            :radiusMeters
        )
        ORDER BY ST_Distance(
            r.current_location,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
        )
        """, nativeQuery = true)
    List<Rider> findByLocationWithinRadius(
        @Param("longitude") double longitude,
        @Param("latitude") double latitude,
        @Param("radiusMeters") double radiusMeters
    );
    
    /**
     * Count available riders within radius
     */
    @Query(value = """
        SELECT COUNT(*) FROM riders r
        WHERE r.is_online = true
        AND r.is_on_break = false
        AND r.current_deliveries < 2
        AND (r.penalty_until IS NULL OR r.penalty_until < NOW())
        AND ST_DWithin(
            r.current_location,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
            :radiusMeters
        )
        """, nativeQuery = true)
    int countAvailableRiders(
        @Param("longitude") double longitude,
        @Param("latitude") double latitude,
        @Param("radiusMeters") double radiusMeters
    );
    
    List<Rider> findByIsOnlineTrue();
    
    List<Rider> findByIsOnlineTrueAndIsOnBreakFalse();
}
