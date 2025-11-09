package com.teadelivery.ordercatalog.rider.service;

import com.teadelivery.ordercatalog.delivery.dto.LocationDTO;
import com.teadelivery.ordercatalog.rider.dto.EarningsDTO;
import com.teadelivery.ordercatalog.rider.dto.RiderResponseDTO;
import com.teadelivery.ordercatalog.rider.dto.UpdateRiderRequestDTO;
import com.teadelivery.ordercatalog.rider.model.Rider;
import com.teadelivery.ordercatalog.rider.repository.RiderRepository;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Rider Service
 * Handles rider operations
 */
@Service
@Slf4j
@Transactional
public class RiderService {
    
    private final RiderRepository riderRepository;
    private final GeometryFactory geometryFactory;
    
    public RiderService(RiderRepository riderRepository) {
        this.riderRepository = riderRepository;
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    }
    
    /**
     * Update rider (status and/or location)
     */
    public RiderResponseDTO updateRider(UUID riderId, UpdateRiderRequestDTO request) {
        Rider rider = riderRepository.findById(riderId)
            .orElseThrow(() -> new IllegalArgumentException("Rider not found: " + riderId));
        
        // Update status if provided
        if (request.getStatus() != null) {
            switch (request.getStatus()) {
                case "ONLINE":
                    rider.setIsOnline(true);
                    rider.setIsOnBreak(false);
                    break;
                case "OFFLINE":
                    rider.setIsOnline(false);
                    rider.setIsOnBreak(false);
                    break;
                case "ON_BREAK":
                    rider.setIsOnline(true);
                    rider.setIsOnBreak(true);
                    break;
            }
            log.info("Updated rider status: riderId={}, status={}", riderId, request.getStatus());
        }
        
        // Update location if provided
        if (request.getLocation() != null) {
            updateLocation(rider, request.getLocation());
        }
        
        rider = riderRepository.save(rider);
        
        return toDTO(rider, "STATUS");
    }
    
    /**
     * Update rider location
     */
    public void updateRiderLocation(UUID riderId, LocationDTO location) {
        Rider rider = riderRepository.findById(riderId)
            .orElseThrow(() -> new IllegalArgumentException("Rider not found: " + riderId));
        
        updateLocation(rider, location);
        riderRepository.save(rider);
        
        log.debug("Updated rider location: riderId={}", riderId);
    }
    
    /**
     * Get rider info
     */
    public RiderResponseDTO getRider(UUID riderId, String include) {
        Rider rider = riderRepository.findById(riderId)
            .orElseThrow(() -> new IllegalArgumentException("Rider not found: " + riderId));
        
        return toDTO(rider, include);
    }
    
    /**
     * Helper: Update location
     */
    private void updateLocation(Rider rider, LocationDTO location) {
        Point point = geometryFactory.createPoint(
            new Coordinate(location.getLongitude(), location.getLatitude())
        );
        rider.setCurrentLocation(point);
        rider.setLastLocationUpdate(Instant.now());
    }
    
    /**
     * Helper: Convert to DTO
     */
    private RiderResponseDTO toDTO(Rider rider, String include) {
        RiderResponseDTO.RiderResponseDTOBuilder builder = RiderResponseDTO.builder()
            .riderId(rider.getRiderId())
            .name(rider.getName())
            .phone(rider.getPhone())
            .email(rider.getEmail())
            .isOnline(rider.getIsOnline())
            .isOnBreak(rider.getIsOnBreak())
            .currentDeliveries(rider.getCurrentDeliveries())
            .createdAt(rider.getCreatedAt());
        
        // Add location
        if (rider.getCurrentLocation() != null) {
            builder.currentLocation(LocationDTO.builder()
                .latitude(rider.getCurrentLocation().getY())
                .longitude(rider.getCurrentLocation().getX())
                .build());
            builder.lastLocationUpdate(rider.getLastLocationUpdate());
        }
        
        // Add stats if requested
        if (include.contains("STATS") || include.contains("ALL")) {
            builder.rating(rider.getRating())
                   .totalDeliveries(rider.getTotalDeliveries())
                   .completedDeliveriesToday(rider.getCompletedDeliveriesToday())
                   .acceptanceRate(rider.getAcceptanceRate());
        }
        
        // Add earnings if requested
        if (include.contains("EARNINGS") || include.contains("ALL")) {
            // TODO: Calculate actual earnings from delivery records
            builder.earnings(EarningsDTO.builder()
                .today(BigDecimal.ZERO)
                .thisWeek(BigDecimal.ZERO)
                .thisMonth(BigDecimal.ZERO)
                .deliveriesToday(rider.getCompletedDeliveriesToday())
                .deliveriesThisWeek(0)
                .deliveriesThisMonth(rider.getTotalDeliveries())
                .build());
        }
        
        return builder.build();
    }
}
