package com.teadelivery.ordercatalog.delivery.service;

import com.teadelivery.ordercatalog.order.model.SubOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Delivery Batching Service
 * Implements intelligent batching algorithm for multi-restaurant deliveries
 * As per Multi-Restaurant Design (05_MULTI_RESTAURANT_DESIGN.md)
 * 
 * Strategy 3: Intelligent Batching (RECOMMENDED)
 * - Group restaurants by proximity (< 2 km)
 * - Align by preparation time (±10 min)
 * - Optimize route for minimal distance
 */
@Service
@Slf4j
public class DeliveryBatchingService {
    
    private static final double MAX_BATCH_DISTANCE_KM = 2.0;
    private static final int MAX_TIME_DIFF_MINUTES = 10;
    private static final int MAX_BATCH_SIZE = 3;
    
    private final GeometryFactory geometryFactory = new GeometryFactory();
    
    /**
     * Create delivery batches from sub-orders
     * Groups nearby restaurants with similar ready times
     */
    public List<DeliveryBatch> createBatches(
        List<SubOrder> subOrders,
        Point customerLocation
    ) {
        log.info("Creating delivery batches for {} sub-orders", subOrders.size());
        
        List<DeliveryBatch> batches = new ArrayList<>();
        List<SubOrder> remaining = new ArrayList<>(subOrders);
        
        while (!remaining.isEmpty()) {
            SubOrder anchor = remaining.remove(0);
            DeliveryBatch batch = new DeliveryBatch();
            batch.addSubOrder(anchor);
            
            // Find nearby restaurants with similar ready time
            Iterator<SubOrder> iterator = remaining.iterator();
            while (iterator.hasNext()) {
                SubOrder candidate = iterator.next();
                
                // Check if can be added to batch
                if (canAddToBatch(batch, candidate, anchor)) {
                    batch.addSubOrder(candidate);
                    iterator.remove();
                }
            }
            
            // Optimize route for this batch
            batch.setOptimizedRoute(optimizeRoute(batch, customerLocation));
            
            batches.add(batch);
            log.info("Created batch with {} sub-orders", batch.getSubOrders().size());
        }
        
        log.info("Created {} delivery batches", batches.size());
        return batches;
    }
    
    /**
     * Check if a candidate sub-order can be added to a batch
     */
    private boolean canAddToBatch(
        DeliveryBatch batch,
        SubOrder candidate,
        SubOrder anchor
    ) {
        // Check max batch size
        if (batch.getSubOrders().size() >= MAX_BATCH_SIZE) {
            return false;
        }
        
        // Check proximity (< 2 km)
        double distance = calculateDistance(
            getRestaurantLocation(anchor),
            getRestaurantLocation(candidate)
        );
        
        if (distance > MAX_BATCH_DISTANCE_KM) {
            log.debug("Sub-order {} too far from anchor: {} km", 
                     candidate.getSubOrderId(), distance);
            return false;
        }
        
        // Check time alignment (±10 min)
        int timeDiff = Math.abs(
            getEstimatedReadyTime(anchor) - 
            getEstimatedReadyTime(candidate)
        );
        
        if (timeDiff > MAX_TIME_DIFF_MINUTES) {
            log.debug("Sub-order {} time diff too large: {} min", 
                     candidate.getSubOrderId(), timeDiff);
            return false;
        }
        
        return true;
    }
    
    /**
     * Optimize pickup route for a batch
     * Uses greedy nearest-neighbor algorithm (can be improved with TSP solver)
     */
    private Route optimizeRoute(DeliveryBatch batch, Point customerLocation) {
        Route route = new Route();
        List<Point> pickupLocations = new ArrayList<>();
        
        // Get all restaurant locations
        for (SubOrder subOrder : batch.getSubOrders()) {
            pickupLocations.add(getRestaurantLocation(subOrder));
        }
        
        // Start from first location (can be optimized to start from rider's current location)
        Point current = pickupLocations.get(0);
        route.addStop(current, 0.0);
        pickupLocations.remove(0);
        
        // Greedy nearest neighbor
        while (!pickupLocations.isEmpty()) {
            Point nearest = findNearest(current, pickupLocations);
            double distance = calculateDistance(current, nearest);
            route.addStop(nearest, distance);
            pickupLocations.remove(nearest);
            current = nearest;
        }
        
        // Add customer location as final stop
        double finalDistance = calculateDistance(current, customerLocation);
        route.addStop(customerLocation, finalDistance);
        
        log.info("Optimized route with {} stops, total distance: {} km", 
                route.getStops().size(), route.getTotalDistance());
        
        return route;
    }
    
    /**
     * Find nearest location from current point
     */
    private Point findNearest(Point current, List<Point> locations) {
        return locations.stream()
            .min(Comparator.comparingDouble(loc -> calculateDistance(current, loc)))
            .orElseThrow(() -> new IllegalStateException("No locations to find nearest"));
    }
    
    /**
     * Calculate distance between two points in kilometers
     * Uses Haversine formula for geographic coordinates
     */
    private double calculateDistance(Point p1, Point p2) {
        double lat1 = p1.getY();
        double lon1 = p1.getX();
        double lat2 = p2.getY();
        double lon2 = p2.getX();
        
        final int R = 6371; // Earth radius in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Get restaurant location from sub-order metadata
     * TODO: Replace with actual restaurant service lookup
     */
    private Point getRestaurantLocation(SubOrder subOrder) {
        // For now, create a dummy location
        // In production, this should fetch from restaurant service
        Map<String, Object> metadata = subOrder.getMetadata();
        
        if (metadata != null && metadata.containsKey("restaurant_location")) {
            @SuppressWarnings("unchecked")
            Map<String, Double> location = (Map<String, Double>) metadata.get("restaurant_location");
            return geometryFactory.createPoint(
                new Coordinate(location.get("longitude"), location.get("latitude"))
            );
        }
        
        // Default location if not found
        return geometryFactory.createPoint(new Coordinate(77.5946, 12.9716)); // Bangalore
    }
    
    /**
     * Get estimated ready time in minutes from now
     * TODO: Replace with actual calculation based on prep time
     */
    private int getEstimatedReadyTime(SubOrder subOrder) {
        Integer prepTime = subOrder.getEstimatedPrepTimeMinutes();
        return prepTime != null ? prepTime : 20; // Default 20 minutes
    }
    
    /**
     * Delivery Batch
     * Represents a group of sub-orders to be delivered together
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryBatch {
        private List<SubOrder> subOrders = new ArrayList<>();
        private Route optimizedRoute;
        
        public void addSubOrder(SubOrder subOrder) {
            this.subOrders.add(subOrder);
        }
        
        public int size() {
            return subOrders.size();
        }
        
        public List<UUID> getSubOrderIds() {
            return subOrders.stream()
                .map(SubOrder::getSubOrderId)
                .toList();
        }
    }
    
    /**
     * Route
     * Represents an optimized delivery route
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Route {
        private List<RouteStop> stops = new ArrayList<>();
        private double totalDistance = 0.0;
        
        public void addStop(Point location, double distanceFromPrevious) {
            stops.add(new RouteStop(location, distanceFromPrevious));
            totalDistance += distanceFromPrevious;
        }
        
        public int getStopCount() {
            return stops.size();
        }
    }
    
    /**
     * Route Stop
     * Represents a single stop in the route
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteStop {
        private Point location;
        private double distanceFromPrevious;
    }
}
