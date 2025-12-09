package com.teadelivery.ordercatalog.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Notification Service
 * Handles push notifications to riders, customers, and restaurants
 * As per BE-003-24
 * 
 * TODO: Integrate with Firebase Cloud Messaging (FCM) or similar service
 */
@Service
@Slf4j
public class NotificationService {

    /**
     * Send push notification to rider
     */
    public void notifyRider(UUID riderId, String title, Map<String, Object> data) {
        // TODO: Implement FCM push notification
        log.info("Sending notification to rider: riderId={}, title={}, data={}",
                riderId, title, data);

        // For now, just log
        // In production, send via FCM:
        // fcmService.send(rider.getDeviceToken(), title, data);
    }

    /**
     * Send notification to customer
     */
    public void notifyCustomer(UUID orderId, String message) {
        log.info("Sending notification to customer: orderId={}, message={}",
                orderId, message);

        // TODO: Implement customer notification
    }

    /**
     * Send notification to restaurant
     */
    public void notifyRestaurant(UUID restaurantId, String message) {
        log.info("Sending notification to restaurant: restaurantId={}, message={}",
                restaurantId, message);

        // TODO: Implement restaurant notification
    }

    /**
     * Notify rider of new delivery request
     */
    public void notifyRiderOfDeliveryRequest(
            UUID riderId,
            UUID deliveryId,
            UUID orderId,
            String pickupAddress,
            String deliveryAddress,
            BigDecimal deliveryFee) {
        // Use HashMap to safely handle null values (Map.of doesn't allow nulls)
        Map<String, Object> data = new HashMap<>();
        data.put("type", "DELIVERY_REQUEST");
        data.put("deliveryId", deliveryId != null ? deliveryId.toString() : "");
        data.put("orderId", orderId != null ? orderId.toString() : "");
        data.put("pickupAddress", pickupAddress != null ? pickupAddress : "Pickup location");
        data.put("deliveryAddress", deliveryAddress != null ? deliveryAddress : "Delivery location");
        data.put("deliveryFee", deliveryFee != null ? deliveryFee.toString() : "0.00");
        data.put("expiresIn", 30); // seconds

        notifyRider(riderId, "New Delivery Request", data);
    }

    /**
     * Notify customer of rider assignment
     */
    public void notifyCustomerOfRiderAssignment(
            UUID orderId,
            String riderName,
            String riderPhone,
            double riderRating,
            int estimatedArrivalMinutes) {
        String message = String.format(
                "Delivery partner %s (%.1f★) is on the way to restaurant. " +
                        "Estimated arrival in %d minutes.",
                riderName, riderRating, estimatedArrivalMinutes);

        notifyCustomer(orderId, message);
    }
}