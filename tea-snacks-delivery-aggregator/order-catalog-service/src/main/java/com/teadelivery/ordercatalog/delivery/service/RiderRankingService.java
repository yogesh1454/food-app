package com.teadelivery.ordercatalog.delivery.service;

import com.teadelivery.ordercatalog.delivery.rider.model.Rider;
import com.teadelivery.ordercatalog.delivery.rider.repository.RiderRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Rider Ranking Service
 * Ranks riders by weighted scoring algorithm
 * As per BE-003-23
 */
@Service
@Slf4j
public class RiderRankingService {
    
    private final RiderRepository riderRepository;
    private final GeometryFactory geometryFactory;
    
    public RiderRankingService(RiderRepository riderRepository) {
        this.riderRepository = riderRepository;
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    }
    
    /**
     * Find available riders within radius
     */
    public List<Rider> findAvailableRiders(double longitude, double latitude, double radiusKm) {
        // Convert km to meters for PostGIS
        double radiusMeters = radiusKm * 1000;
        
        return riderRepository.findByLocationWithinRadius(longitude, latitude, radiusMeters);
    }
    
    /**
     * Rank riders by weighted scoring
     * Scoring formula:
     * - Distance (35% weight)
     * - Rating (25% weight)
     * - Acceptance rate (20% weight)
     * - Current load (10% weight)
     * - Activity today (10% weight)
     */
    public List<Rider> rankRiders(List<Rider> availableRiders, double longitude, double latitude) {
        Point restaurantLocation = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        
        return availableRiders.stream()
            .map(rider -> {
                double distance = calculateDistance(rider.getCurrentLocation(), restaurantLocation);
                double rating = rider.getRating().doubleValue();
                double acceptanceRate = rider.getAcceptanceRate().doubleValue() / 100.0;
                int currentLoad = rider.getCurrentDeliveries();
                int completedToday = rider.getCompletedDeliveriesToday();
                
                // Scoring formula (weighted)
                double score = 
                    (1.0 / (distance + 1)) * 0.35 +     // Distance (35%)
                    (rating / 5.0) * 0.25 +              // Rating (25%)
                    acceptanceRate * 0.20 +              // Acceptance (20%)
                    (1.0 / (currentLoad + 1)) * 0.10 +   // Load (10%)
                    (completedToday / 20.0) * 0.10;      // Activity (10%)
                
                log.debug("Rider score: riderId={}, distance={}, rating={}, " +
                         "acceptance={}, load={}, completed={}, score={}", 
                         rider.getRiderId(), distance, rating, acceptanceRate, 
                         currentLoad, completedToday, score);
                
                return new ScoredRider(rider, score, distance);
            })
            .sorted(Comparator.comparing(ScoredRider::getScore).reversed())
            .map(ScoredRider::getRider)
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate distance between two points in kilometers
     * Uses Haversine formula for accuracy
     */
    private double calculateDistance(Point point1, Point point2) {
        if (point1 == null || point2 == null) {
            return Double.MAX_VALUE;
        }
        
        double lat1 = point1.getY();
        double lon1 = point1.getX();
        double lat2 = point2.getY();
        double lon2 = point2.getX();
        
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance in km
    }
    
    @Data
    @AllArgsConstructor
    private static class ScoredRider {
        private Rider rider;
        private double score;
        private double distance;
    }
}
