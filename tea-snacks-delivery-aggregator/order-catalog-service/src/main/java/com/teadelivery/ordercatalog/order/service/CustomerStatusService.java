package com.teadelivery.ordercatalog.order.service;

import com.teadelivery.ordercatalog.delivery.dto.LocationDTO;
import com.teadelivery.ordercatalog.delivery.model.Delivery;
import com.teadelivery.ordercatalog.delivery.repository.DeliveryRepository;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.repository.OrderRepository;
import com.teadelivery.ordercatalog.delivery.model.Rider;
import com.teadelivery.ordercatalog.delivery.repository.RiderRepository;
import com.teadelivery.ordercatalog.order.dto.CustomerStatusResponseDTO;
import com.teadelivery.ordercatalog.order.model.CustomerStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Customer Status Service
 * Provides customer-facing order status information
 * As per BE-004-28
 */
@Service
@Slf4j
public class CustomerStatusService {
    
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final RiderRepository riderRepository;
    private final StatusMapperService statusMapper;
    
    public CustomerStatusService(
        OrderRepository orderRepository,
        DeliveryRepository deliveryRepository,
        RiderRepository riderRepository,
        StatusMapperService statusMapper
    ) {
        this.orderRepository = orderRepository;
        this.deliveryRepository = deliveryRepository;
        this.riderRepository = riderRepository;
        this.statusMapper = statusMapper;
    }
    
    /**
     * Get order status for customer
     */
    public CustomerStatusResponseDTO getOrderStatus(UUID customerId, UUID orderId) {
        // Get order
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        // Verify customer owns this order
        if (!customerId.equals(order.getCustomerId())) {
            throw new IllegalArgumentException("Order does not belong to customer");
        }
        
        // Map to customer status
        CustomerStatus status = statusMapper.mapToCustomerStatus(order);
        
        // Build response
        CustomerStatusResponseDTO.CustomerStatusResponseDTOBuilder builder = 
            CustomerStatusResponseDTO.builder()
                .orderId(orderId)
                .status(status)
                .primaryMessage(status.getPrimaryMessage())
                .secondaryMessage(status.getSecondaryMessage())
                .progressPercentage(status.getProgressPercentage())
                .canCancel(status.canCancel())
                .orderPlacedAt(order.getCreatedAt() != null ? 
                    order.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null)
                .lastUpdatedAt(order.getUpdatedAt() != null ? 
                    order.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        
        // Add delivery and rider info if available
        Optional<Delivery> deliveryOpt = deliveryRepository.findByOrderId(orderId);
        if (deliveryOpt.isPresent()) {
            Delivery delivery = deliveryOpt.get();
            
            // Calculate ETA
            Instant estimatedArrival = calculateETA(order, delivery);
            if (estimatedArrival != null) {
                builder.estimatedArrival(estimatedArrival);
                long minutesRemaining = Duration.between(Instant.now(), estimatedArrival).toMinutes();
                builder.estimatedMinutesRemaining((int) Math.max(0, minutesRemaining));
            }
            
            // Add rider info if assigned
            if (delivery.getRiderId() != null) {
                Optional<Rider> riderOpt = riderRepository.findById(delivery.getRiderId());
                if (riderOpt.isPresent()) {
                    Rider rider = riderOpt.get();
                    
                    CustomerStatusResponseDTO.RiderInfoDTO riderInfo = 
                        CustomerStatusResponseDTO.RiderInfoDTO.builder()
                            .riderId(rider.getRiderId())
                            .name(rider.getName())
                            .phone(rider.getPhone())
                            .rating(rider.getRating().doubleValue())
                            .build();
                    
                    // Add rider location if available
                    if (rider.getCurrentLocation() != null) {
                        riderInfo.setCurrentLocation(LocationDTO.builder()
                            .latitude(rider.getCurrentLocation().getY())
                            .longitude(rider.getCurrentLocation().getX())
                            .build());
                    }
                    
                    builder.riderInfo(riderInfo);
                }
            }
        }
        
        return builder.build();
    }
    
    /**
     * Calculate estimated arrival time
     */
    private Instant calculateETA(Order order, Delivery delivery) {
        CustomerStatus status = statusMapper.mapToCustomerStatus(order);
        
        switch (status) {
            case ORDER_PLACED:
                // 2 min for confirmation + 20 min prep + 15 min delivery
                return Instant.now().plus(Duration.ofMinutes(37));
                
            case ORDER_CONFIRMED:
                // 20 min prep + 15 min delivery
                return Instant.now().plus(Duration.ofMinutes(35));
                
            case PREPARING:
                // 15 min remaining prep + 15 min delivery
                return Instant.now().plus(Duration.ofMinutes(30));
                
            case RIDER_ASSIGNED:
            case READY_FOR_PICKUP:
                // 5 min for pickup + 15 min delivery
                return Instant.now().plus(Duration.ofMinutes(20));
                
            case OUT_FOR_DELIVERY:
                // 15 min delivery (TODO: calculate based on distance)
                return Instant.now().plus(Duration.ofMinutes(15));
                
            default:
                return null;
        }
    }
}
